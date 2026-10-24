package com.example.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Locale
import kotlin.random.Random

data class LanPeerInfo(
    val username: String,
    val characterId: String,
    val upgrades: CharacterUpgradeState
)

data class LanDiscoveredHost(
    val serviceName: String,
    val displayLabel: String,
    val hostIp: String,
    val port: Int
) {
    val addressText: String get() = "$hostIp:$port"
}

sealed class LanSessionState {
    data object Idle : LanSessionState()
    data class Hosting(val ipPort: String, val isNsdRegistered: Boolean) : LanSessionState()
    data class Connecting(val targetAddress: String) : LanSessionState()
    data class Connected(val isHost: Boolean, val peer: LanPeerInfo, val address: String) : LanSessionState()
    data class Error(val message: String) : LanSessionState()
}

sealed class LanPacket {
    data class StartClash(val dirX: Float, val dirY: Float) : LanPacket()
    data class RedirectDash(val dirX: Float, val dirY: Float) : LanPacket()
    data object TriggerUlt : LanPacket()
    data object TriggerCounter : LanPacket()
    data class HostStateSync(
        val hostX: Float,
        val hostY: Float,
        val hostVx: Float,
        val hostVy: Float,
        val guestX: Float,
        val guestY: Float,
        val guestVx: Float,
        val guestVy: Float,
        val hostHp: Float,
        val guestHp: Float,
        val hostUlt: Float,
        val guestUlt: Float
    ) : LanPacket()
    data class Knockout(
        val hostWon: Boolean,
        val hostFinalHp: Float = 0f,
        val guestFinalHp: Float = 0f
    ) : LanPacket()
    data object PeerDisconnected : LanPacket()
}

/**
 * Real LAN Wi-Fi multiplayer manager using Android NsdManager (_animeball._tcp.)
 * for zero-config local discovery + low-latency TCP sockets (TCP_NODELAY) + direct IP:Port join.
 */
class LanMultiplayerManager(context: Context) {

    private val appContext = context.applicationContext
    private val nsdManager: NsdManager? =
        appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionState = MutableStateFlow<LanSessionState>(LanSessionState.Idle)
    val sessionState: StateFlow<LanSessionState> = _sessionState.asStateFlow()

    private val _discoveredHosts = MutableStateFlow<List<LanDiscoveredHost>>(emptyList())
    val discoveredHosts: StateFlow<List<LanDiscoveredHost>> = _discoveredHosts.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<LanPacket>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<LanPacket> = _incomingPackets.asSharedFlow()

    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var activeSocket: Socket? = null

    @Volatile
    private var socketWriter: BufferedWriter? = null

    private var connectionJob: Job? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun detectLocalIpv4(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            var fallbackIp: String? = null
            for (nif in interfaces) {
                if (!nif.isUp || nif.isLoopback) continue
                val addrs = nif.inetAddresses?.toList().orEmpty()
                for (addr in addrs) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val hostAddr = addr.hostAddress ?: continue
                        if (addr.isSiteLocalAddress) {
                            return hostAddr
                        }
                        if (fallbackIp == null) {
                            fallbackIp = hostAddr
                        }
                    }
                }
            }
            fallbackIp ?: "127.0.0.1"
        } catch (_: Exception) {
            "127.0.0.1"
        }
    }

    fun startDiscovery() {
        stopDiscovery()
        val manager = nsdManager ?: return
        _discoveredHosts.value = emptyList()

        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) = Unit

            override fun onServiceFound(service: NsdServiceInfo) {
                if (!service.serviceType.contains("_animeball._tcp")) return
                if (!service.serviceName.startsWith("AnimeBall_")) return
                resolveServiceSafely(manager, service)
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                _discoveredHosts.value = _discoveredHosts.value.filterNot {
                    it.serviceName == service.serviceName
                }
            }

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "NSD start discovery failed: $errorCode")
                stopDiscovery()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "NSD stop discovery failed: $errorCode")
            }
        }
        discoveryListener = listener
        try {
            manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to start NSD discovery: ${e.message}")
            discoveryListener = null
        }
    }

    private fun resolveServiceSafely(manager: NsdManager, service: NsdServiceInfo) {
        try {
            @Suppress("DEPRECATION")
            manager.resolveService(service, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "NSD resolve failed ($errorCode) for ${serviceInfo.serviceName}")
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    @Suppress("DEPRECATION")
                    val hostIp = serviceInfo.host?.hostAddress ?: return
                    val port = serviceInfo.port
                    if (port <= 0) return

                    val cleanLabel = serviceInfo.serviceName
                        .removePrefix("AnimeBall_")
                        .substringBeforeLast("_")
                        .ifBlank { "LAN Host" }

                    val item = LanDiscoveredHost(
                        serviceName = serviceInfo.serviceName,
                        displayLabel = cleanLabel,
                        hostIp = hostIp,
                        port = port
                    )
                    val updated = (_discoveredHosts.value.filterNot {
                        it.addressText == item.addressText || it.serviceName == item.serviceName
                    } + item).take(12)
                    _discoveredHosts.value = updated
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "resolveService exception: ${e.message}")
        }
    }

    fun stopDiscovery() {
        val listener = discoveryListener ?: return
        discoveryListener = null
        try {
            nsdManager?.stopServiceDiscovery(listener)
        } catch (_: Exception) {
        }
    }

    fun startHosting(localPeer: LanPeerInfo) {
        closeSession()
        connectionJob = ioScope.launch {
            try {
                val server = ServerSocket(0).apply { reuseAddress = true }
                serverSocket = server
                val port = server.localPort
                val localIp = detectLocalIpv4()
                val ipPort = "$localIp:$port"
                _sessionState.value = LanSessionState.Hosting(ipPort = ipPort, isNsdRegistered = false)

                registerNsdHost(localPeer.username, port, ipPort)

                val client = server.accept()
                client.tcpNoDelay = true
                client.keepAlive = true
                activeSocket = client
                unregisterNsdHost()
                try {
                    server.close()
                } catch (_: Exception) {
                }
                serverSocket = null

                runSocketLoop(socket = client, localPeer = localPeer, isHost = true, addressLabel = ipPort)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_sessionState.value is LanSessionState.Hosting) {
                    _sessionState.value = LanSessionState.Error(e.localizedMessage ?: "Failed to host LAN room.")
                }
            }
        }
    }

    fun joinByAddress(rawIpPort: String, localPeer: LanPeerInfo) {
        val trimmed = rawIpPort.trim()
        val parts = trimmed.split(':')
        val host = parts.getOrNull(0)?.trim().orEmpty()
        val port = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: DEFAULT_FALLBACK_PORT
        if (host.isBlank() || port !in 1..65535) {
            _sessionState.value = LanSessionState.Error("Enter a valid IP:Port (for example 192.168.1.24:41235).")
            return
        }

        closeSession()
        val targetLabel = "$host:$port"
        _sessionState.value = LanSessionState.Connecting(targetLabel)

        connectionJob = ioScope.launch {
            try {
                val socket = Socket()
                socket.tcpNoDelay = true
                socket.keepAlive = true
                socket.connect(InetSocketAddress(host, port), 5_500)
                activeSocket = socket
                stopDiscovery()

                runSocketLoop(socket = socket, localPeer = localPeer, isHost = false, addressLabel = targetLabel)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sessionState.value = LanSessionState.Error(
                    "Could not connect to $targetLabel (${e.localizedMessage ?: "timeout"})."
                )
            }
        }
    }

    private suspend fun runSocketLoop(
        socket: Socket,
        localPeer: LanPeerInfo,
        isHost: Boolean,
        addressLabel: String
    ) = withContext(Dispatchers.IO) {
        val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8))
        val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
        socketWriter = writer

        // Exchange initial HELLO handshake
        val safeName = localPeer.username.replace('|', ' ').trim().ifEmpty { "Player" }.take(18)
        val helloLine = "HELLO|$safeName|${localPeer.characterId}|${localPeer.upgrades.healthLevel}|${localPeer.upgrades.powerLevel}|${localPeer.upgrades.cooldownLevel}"
        writeLineInternal(writer, helloLine)

        try {
            while (isActive && !socket.isClosed) {
                val line = reader.readLine() ?: break
                handleIncomingLine(line, isHost, addressLabel)
            }
        } catch (_: Exception) {
        } finally {
            _incomingPackets.tryEmit(LanPacket.PeerDisconnected)
        }
    }

    private fun handleIncomingLine(line: String, isHost: Boolean, addressLabel: String) {
        val tokens = line.split('|')
        when (tokens.firstOrNull()) {
            "HELLO" -> {
                if (tokens.size >= 6) {
                    val peerName = tokens[1].ifBlank { "Friend" }.take(18)
                    val charId = CharacterRoster.getById(tokens[2]).id
                    val hpLv = tokens[3].toIntOrNull()?.coerceIn(0, 10) ?: 0
                    val pwrLv = tokens[4].toIntOrNull()?.coerceIn(0, 10) ?: 0
                    val cdLv = tokens[5].toIntOrNull()?.coerceIn(0, 10) ?: 0
                    val peer = LanPeerInfo(
                        username = peerName,
                        characterId = charId,
                        upgrades = CharacterUpgradeState(hpLv, pwrLv, cdLv)
                    )
                    _sessionState.value = LanSessionState.Connected(
                        isHost = isHost,
                        peer = peer,
                        address = addressLabel
                    )
                }
            }

            "START" -> {
                if (tokens.size >= 3) {
                    val dx = tokens[1].toFloatOrNull() ?: return
                    val dy = tokens[2].toFloatOrNull() ?: return
                    _incomingPackets.tryEmit(LanPacket.StartClash(dx, dy))
                }
            }

            "DASH" -> {
                if (tokens.size >= 3) {
                    val dx = tokens[1].toFloatOrNull() ?: return
                    val dy = tokens[2].toFloatOrNull() ?: return
                    _incomingPackets.tryEmit(LanPacket.RedirectDash(dx, dy))
                }
            }

            "ULT" -> {
                _incomingPackets.tryEmit(LanPacket.TriggerUlt)
            }

            "COUNTER" -> {
                _incomingPackets.tryEmit(LanPacket.TriggerCounter)
            }

            "SYNC" -> {
                if (tokens.size >= 13) {
                    val hx = tokens[1].toFloatOrNull() ?: return
                    val hy = tokens[2].toFloatOrNull() ?: return
                    val hvx = tokens[3].toFloatOrNull() ?: return
                    val hvy = tokens[4].toFloatOrNull() ?: return
                    val gx = tokens[5].toFloatOrNull() ?: return
                    val gy = tokens[6].toFloatOrNull() ?: return
                    val gvx = tokens[7].toFloatOrNull() ?: return
                    val gvy = tokens[8].toFloatOrNull() ?: return
                    val hHp = tokens[9].toFloatOrNull() ?: return
                    val gHp = tokens[10].toFloatOrNull() ?: return
                    val hUlt = tokens[11].toFloatOrNull() ?: return
                    val gUlt = tokens[12].toFloatOrNull() ?: return
                    _incomingPackets.tryEmit(
                        LanPacket.HostStateSync(
                            hostX = hx,
                            hostY = hy,
                            hostVx = hvx,
                            hostVy = hvy,
                            guestX = gx,
                            guestY = gy,
                            guestVx = gvx,
                            guestVy = gvy,
                            hostHp = hHp,
                            guestHp = gHp,
                            hostUlt = hUlt,
                            guestUlt = gUlt
                        )
                    )
                }
            }

            "KO" -> {
                if (tokens.size >= 2) {
                    val hostWon = tokens[1] == "1"
                    val hHp = tokens.getOrNull(2)?.toFloatOrNull() ?: 0f
                    val gHp = tokens.getOrNull(3)?.toFloatOrNull() ?: 0f
                    _incomingPackets.tryEmit(LanPacket.Knockout(hostWon, hHp, gHp))
                }
            }
        }
    }

    fun sendStartClash(dirX: Float, dirY: Float) {
        sendRawLine(String.format(Locale.US, "START|%.4f|%.4f", dirX, dirY))
    }

    fun sendRedirectDash(dirX: Float, dirY: Float) {
        sendRawLine(String.format(Locale.US, "DASH|%.4f|%.4f", dirX, dirY))
    }

    fun sendUltimate() {
        sendRawLine("ULT")
    }

    fun sendCounter() {
        sendRawLine("COUNTER")
    }

    fun sendHostStateSync(
        hostX: Float,
        hostY: Float,
        hostVx: Float,
        hostVy: Float,
        guestX: Float,
        guestY: Float,
        guestVx: Float,
        guestVy: Float,
        hostHp: Float,
        guestHp: Float,
        hostUlt: Float,
        guestUlt: Float
    ) {
        sendRawLine(
            String.format(
                Locale.US,
                "SYNC|%.4f|%.4f|%.4f|%.4f|%.4f|%.4f|%.4f|%.4f|%.1f|%.1f|%.1f|%.1f",
                hostX,
                hostY,
                hostVx,
                hostVy,
                guestX,
                guestY,
                guestVx,
                guestVy,
                hostHp,
                guestHp,
                hostUlt,
                guestUlt
            )
        )
    }

    fun sendKnockout(hostWon: Boolean, hostFinalHp: Float = 0f, guestFinalHp: Float = 0f) {
        sendRawLine(String.format(Locale.US, "KO|%d|%.1f|%.1f", if (hostWon) 1 else 0, hostFinalHp, guestFinalHp))
    }

    private fun sendRawLine(line: String) {
        val writer = socketWriter ?: return
        ioScope.launch {
            try {
                writeLineInternal(writer, line)
            } catch (_: Exception) {
            }
        }
    }

    @Synchronized
    private fun writeLineInternal(writer: BufferedWriter, line: String) {
        writer.write(line)
        writer.newLine()
        writer.flush()
    }

    private fun registerNsdHost(username: String, port: Int, ipPort: String) {
        val manager = nsdManager ?: return
        val cleanUser = username.replace(Regex("[^A-Za-z0-9]"), "").ifBlank { "Host" }.take(10)
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "AnimeBall_${cleanUser}_${Random.nextInt(10, 99)}"
            serviceType = SERVICE_TYPE
            setPort(port)
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                val cur = _sessionState.value
                if (cur is LanSessionState.Hosting) {
                    _sessionState.value = cur.copy(ipPort = ipPort, isNsdRegistered = true)
                }
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.w(TAG, "NSD registration failed: $errorCode")
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) = Unit
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
        }
        registrationListener = listener
        try {
            manager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.w(TAG, "NSD register exception: ${e.message}")
            registrationListener = null
        }
    }

    private fun unregisterNsdHost() {
        val listener = registrationListener ?: return
        registrationListener = null
        try {
            nsdManager?.unregisterService(listener)
        } catch (_: Exception) {
        }
    }

    fun closeSession() {
        stopDiscovery()
        unregisterNsdHost()
        connectionJob?.cancel()
        connectionJob = null
        socketWriter = null
        try {
            activeSocket?.close()
        } catch (_: Exception) {
        }
        activeSocket = null
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
        _sessionState.value = LanSessionState.Idle
    }

    companion object {
        private const val TAG = "LanMultiplayer"
        private const val SERVICE_TYPE = "_animeball._tcp."
        private const val DEFAULT_FALLBACK_PORT = 41235
    }
}
