package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CharacterData
import com.example.model.PlayerProfile
import com.example.network.LanDiscoveredHost
import com.example.network.LanSessionState
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AppTheme

@Composable
fun MultiplayerLobbyScreen(
    profile: PlayerProfile,
    playerChar: CharacterData,
    localIpAddress: String,
    lanSessionState: LanSessionState,
    discoveredLanHosts: List<LanDiscoveredHost>,
    activeRoomCode: String?,
    isOnlineRoomBusy: Boolean,
    lobbyStatusMessage: String?,
    onStartLanDiscovery: () -> Unit,
    onHostLanRoom: () -> Unit,
    onJoinLanAddress: (String) -> Unit,
    onCancelLanSession: () -> Unit,
    onCreateOnlineRoom: () -> Unit,
    onJoinOnlineRoom: (String) -> Unit,
    onCancelOnlineRoom: () -> Unit,
    onChangeFighter: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val palette = AppTheme.palette
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = LAN Wi-Fi, 1 = Online Room Code
    var manualIpPort by remember { mutableStateOf("") }
    var roomCodeInput by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            onStartLanDiscovery()
        }
    }

    Scaffold(
        containerColor = palette.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.cardSurface)
                            .border(1.dp, palette.border, CircleShape)
                            .testTag("multiplayer_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VS Friend",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Fight over LAN Wi-Fi or Internet Room Code",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }

                Surface(
                    onClick = onChangeFighter,
                    shape = RoundedCornerShape(12.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("multiplayer_change_fighter_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Canvas(modifier = Modifier.size(30.dp)) {
                            AnimeBallRenderer.drawPreviewAvatar(
                                scope = this,
                                character = playerChar,
                                weaponAngle = 0.7f
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = playerChar.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("multiplayer_lobby_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Connection Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LobbyTabButton(
                        title = "LAN Wi-Fi",
                        subtitle = "Same Router / Hotspot",
                        icon = Icons.Default.Wifi,
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_lan_wifi")
                    )
                    LobbyTabButton(
                        title = "Room Code",
                        subtitle = "Online Anywhere",
                        icon = Icons.Default.Public,
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_room_code")
                    )
                }

                if (!lobbyStatusMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.blueAccentSoft,
                        border = BorderStroke(1.dp, palette.blueAccent.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = lobbyStatusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.blueAccent,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                if (selectedTab == 0) {
                    // ==================== LAN WI-FI TAB ====================
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = palette.cardSurface,
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Host Local Wi-Fi Room",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = "Your Wi-Fi IP: $localIpAddress",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary
                                    )
                                }
                            }

                            when (lanSessionState) {
                                is LanSessionState.Hosting -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = palette.stageSurface,
                                        border = BorderStroke(1.dp, palette.goldAccent),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "ROOM READY • WAITING FOR FRIEND",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.goldAccent
                                                )
                                                Text(
                                                    text = lanSessionState.ipPort,
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    color = palette.stageOnSurface
                                                )
                                                Text(
                                                    text = if (lanSessionState.isNsdRegistered) {
                                                        "Visible automatically on nearby phones"
                                                    } else {
                                                        "Share this IP:Port with your friend"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = palette.stageMuted
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboard.setText(AnnotatedString(lanSessionState.ipPort))
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy IP and Port",
                                                    tint = palette.goldAccent
                                                )
                                            }
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onCancelLanSession,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, palette.primaryAccent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text("Stop Hosting", color = palette.primaryAccent)
                                    }
                                }

                                is LanSessionState.Connecting -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.4.dp,
                                            color = palette.blueAccent
                                        )
                                        Text(
                                            text = "Connecting to ${lanSessionState.targetAddress}...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = palette.textPrimary
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onCancelLanSession,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text("Cancel", color = palette.textSecondary)
                                    }
                                }

                                else -> {
                                    if (lanSessionState is LanSessionState.Error) {
                                        Text(
                                            text = lanSessionState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.primaryAccent
                                        )
                                    }
                                    Button(
                                        onClick = onHostLanRoom,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.primaryAccent,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("host_lan_button")
                                    ) {
                                        Icon(Icons.Default.Wifi, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Host LAN Room",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = palette.border)

                            // Auto-discovered nearby LAN rooms
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nearby LAN Rooms",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary
                                )
                                IconButton(
                                    onClick = onStartLanDiscovery,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("scan_lan_button")
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Scan LAN Rooms",
                                        tint = palette.blueAccent
                                    )
                                }
                            }

                            if (discoveredLanHosts.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = palette.elevatedSurface,
                                    border = BorderStroke(1.dp, palette.border),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No nearby Wi-Fi rooms found yet. Make sure both devices share the same Wi-Fi or mobile hotspot, or join directly by IP:Port below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                discoveredLanHosts.forEach { host ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = palette.elevatedSurface,
                                        border = BorderStroke(1.dp, palette.blueAccent.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = host.displayLabel,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = palette.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = host.addressText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.textSecondary
                                                )
                                            }
                                            Button(
                                                onClick = { onJoinLanAddress(host.addressText) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = palette.blueAccent,
                                                    contentColor = Color.White
                                                ),
                                                modifier = Modifier.height(48.dp)
                                            ) {
                                                Text("Join", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = palette.border)

                            // Manual IP:Port Fallback
                            Text(
                                text = "Direct IP:Port Join",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualIpPort,
                                    onValueChange = { manualIpPort = it.take(32) },
                                    singleLine = true,
                                    placeholder = { Text("192.168.1.24:41235") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lan_ip_input")
                                )
                                Button(
                                    onClick = { onJoinLanAddress(manualIpPort) },
                                    enabled = manualIpPort.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.blueAccent,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    modifier = Modifier
                                        .height(54.dp)
                                        .testTag("join_lan_button")
                                ) {
                                    Text("Join IP", color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    // ==================== INTERNET ROOM CODE TAB ====================
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = palette.cardSurface,
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Online Friend Room",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Create a 6-letter Room Code and send it to your friend anywhere on the internet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )

                            if (!activeRoomCode.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = palette.stageSurface,
                                    border = BorderStroke(1.5.dp, palette.goldAccent),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "SHARE THIS ROOM CODE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.goldAccent
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = activeRoomCode,
                                                style = MaterialTheme.typography.displayMedium,
                                                color = palette.stageOnSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    clipboard.setText(AnnotatedString(activeRoomCode))
                                                },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("copy_room_code_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Room Code",
                                                    tint = palette.goldAccent
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = palette.goldAccent
                                            )
                                            Text(
                                                text = "Waiting for friend to join...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.stageMuted
                                            )
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = onCancelOnlineRoom,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, palette.primaryAccent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("Cancel Room", color = palette.primaryAccent)
                                }
                            } else {
                                Button(
                                    onClick = onCreateOnlineRoom,
                                    enabled = !isOnlineRoomBusy,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.primaryAccent,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("create_room_code_button")
                                ) {
                                    Icon(Icons.Default.Public, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isOnlineRoomBusy) "Creating Room..." else "Create Room Code",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                }
                            }

                            HorizontalDivider(color = palette.border)

                            Text(
                                text = "Have a Friend's Code?",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = roomCodeInput,
                                    onValueChange = { raw ->
                                        roomCodeInput = raw.uppercase()
                                            .replace(Regex("[^A-Z0-9]"), "")
                                            .take(8)
                                    },
                                    singleLine = true,
                                    placeholder = { Text("Enter 6-letter code") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("room_code_input")
                                )
                                Button(
                                    onClick = { onJoinOnlineRoom(roomCodeInput) },
                                    enabled = roomCodeInput.length >= 4 && !isOnlineRoomBusy,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.blueAccent,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp),
                                    modifier = Modifier
                                        .height(54.dp)
                                        .testTag("join_room_code_button")
                                ) {
                                    Text("Join Room", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbyTabButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = AppTheme.palette
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) palette.blueAccentSoft else palette.cardSurface,
        border = BorderStroke(if (selected) 1.6.dp else 1.dp, if (selected) palette.blueAccent else palette.border),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) palette.blueAccent else palette.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) palette.blueAccent else palette.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
