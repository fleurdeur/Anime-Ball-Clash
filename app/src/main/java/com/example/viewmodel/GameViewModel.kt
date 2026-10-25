package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BattleAudioDirector
import com.example.audio.SoundManager
import com.example.model.AppScreen
import com.example.model.AppThemeMode
import com.example.model.BattleSimulation
import com.example.model.BotDifficulty
import com.example.model.CharacterData
import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import com.example.model.CombatBall
import com.example.model.EngineSettings
import com.example.model.GameMode
import com.example.model.InitialGameData
import com.example.model.LeaderboardEntry
import com.example.model.MatchStatistics
import com.example.model.PhysicsMesh
import com.example.model.PlayerProfile
import com.example.model.QuestItem
import com.example.model.RenderMode
import com.example.model.TargetFPS
import com.example.model.TurnState
import com.example.model.UpgradeStatType
import com.example.model.Vector2
import com.example.network.LanDiscoveredHost
import com.example.network.LanMultiplayerManager
import com.example.network.LanPacket
import com.example.network.LanPeerInfo
import com.example.network.LanSessionState
import com.example.network.MatchRoomDoc
import com.example.network.OnlineArenaRepository
import com.example.network.RankedQueueResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class BattleHudState(
    val playerHp: Int = 360,
    val playerMaxHp: Int = 360,
    val opponentHp: Int = 360,
    val opponentMaxHp: Int = 360,
    val playerUltPercent: Int = 0,
    val opponentUltPercent: Int = 0,
    val isPlayerUltActive: Boolean = false,
    val isOpponentUltActive: Boolean = false,
    val playerUltActiveHalfSecs: Int = 0,
    val opponentUltActiveHalfSecs: Int = 0,
    val playerUltCooldownHalfSecs: Int = 0,
    val opponentUltCooldownHalfSecs: Int = 0,
    val playerUltMaxDurationSec: Float = 5f,
    val opponentUltMaxDurationSec: Float = 5f,
    val playerUltMaxCooldownSec: Float = 8f,
    val opponentUltMaxCooldownSec: Float = 8f,
    val redirectReady: Boolean = true,
    val redirectHalfSecsLeft: Int = 0,
    val isMultiDashActive: Boolean = false,
    val multiDashCharges: Int = 0,
    val canTriggerCounter: Boolean = true,
    val isPlayerCounterActive: Boolean = false,
    val playerCounterActiveHalfSecs: Int = 0,
    val playerCounterCooldownHalfSecs: Int = 0,
    val playerCounterMaxDurationSec: Float = 1.15f,
    val playerCounterMaxCooldownSec: Float = 2.8f,
    val counterLabel: String = "Block",
    val autoCounterPercent: Int = 30,
    val isAutoCounterShielded: Boolean = false,
    val lastImpactBanner: String = ""
)

private enum class ActiveTransport {
    NONE,
    LAN,
    FIRESTORE
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("anime_ball_clash_clean_v1", Context.MODE_PRIVATE)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val soundManager = SoundManager(application)
    val battleAudioDirector = BattleAudioDirector(application)
    private val lanManager = LanMultiplayerManager(application)

    private var onlineRepository: OnlineArenaRepository? = null
    private var boundUserId: String? = null
    private var cachedOnlineLeaderboard: List<LeaderboardEntry> = emptyList()

    private val _currentScreen = MutableStateFlow(AppScreen.LOADING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _lastNavWasPop = MutableStateFlow(false)
    val lastNavWasPop: StateFlow<Boolean> = _lastNavWasPop.asStateFlow()
    private val navBackStack = ArrayDeque<AppScreen>()
    private var battleOriginScreen: AppScreen = AppScreen.HOME_HUB

    private val _engineSettings = MutableStateFlow(loadEngineSettings())
    val engineSettings: StateFlow<EngineSettings> = _engineSettings.asStateFlow()

    private val _playerProfile = MutableStateFlow(loadPlayerProfile())
    val playerProfile: StateFlow<PlayerProfile> = _playerProfile.asStateFlow()

    private val _unlockedCharacterIds = MutableStateFlow(loadUnlockedCharacters())
    val unlockedCharacterIds: StateFlow<Set<String>> = _unlockedCharacterIds.asStateFlow()

    private val _characterUpgrades = MutableStateFlow(loadCharacterUpgrades())
    val characterUpgrades: StateFlow<Map<String, CharacterUpgradeState>> = _characterUpgrades.asStateFlow()

    private val _quests = MutableStateFlow(loadQuests())
    val quests: StateFlow<List<QuestItem>> = _quests.asStateFlow()

    private val _dailyQuests = MutableStateFlow(loadDailyQuests())
    val dailyQuests: StateFlow<List<QuestItem>> = _dailyQuests.asStateFlow()

    private val _leaderboard = MutableStateFlow(InitialGameData.buildLeaderboard(_playerProfile.value))
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.PRACTICE_BOT)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _botDifficulty = MutableStateFlow(BotDifficulty.MEDIUM)
    val botDifficulty: StateFlow<BotDifficulty> = _botDifficulty.asStateFlow()

    private val _customOpponentCharacter = MutableStateFlow(CharacterRoster.getById("guts_berserk"))
    val customOpponentCharacter: StateFlow<CharacterData> = _customOpponentCharacter.asStateFlow()

    private val _matchmakingCountdown = MutableStateFlow(2)
    val matchmakingCountdown: StateFlow<Int> = _matchmakingCountdown.asStateFlow()

    private val _canStartRankedChallenger = MutableStateFlow(false)
    val canStartRankedChallenger: StateFlow<Boolean> = _canStartRankedChallenger.asStateFlow()

    private val _opponentName = MutableStateFlow("Rival Sukuna")
    val opponentName: StateFlow<String> = _opponentName.asStateFlow()

    private val _opponentCharacter = MutableStateFlow(CharacterRoster.getById("sukuna_shrine"))
    val opponentCharacter: StateFlow<CharacterData> = _opponentCharacter.asStateFlow()

    private val _localIpAddress = MutableStateFlow(lanManager.detectLocalIpv4())
    val localIpAddress: StateFlow<String> = _localIpAddress.asStateFlow()

    val lanSessionState: StateFlow<LanSessionState> = lanManager.sessionState
    val discoveredLanHosts: StateFlow<List<LanDiscoveredHost>> = lanManager.discoveredHosts

    private val _activeRoomCode = MutableStateFlow<String?>(null)
    val activeRoomCode: StateFlow<String?> = _activeRoomCode.asStateFlow()

    private val _isOnlineRoomBusy = MutableStateFlow(false)
    val isOnlineRoomBusy: StateFlow<Boolean> = _isOnlineRoomBusy.asStateFlow()

    private val _lobbyStatusMessage = MutableStateFlow<String?>(null)
    val lobbyStatusMessage: StateFlow<String?> = _lobbyStatusMessage.asStateFlow()

    private val _battleSim = MutableStateFlow<BattleSimulation?>(null)
    val battleSim: StateFlow<BattleSimulation?> = _battleSim.asStateFlow()

    private val _turnState = MutableStateFlow(TurnState.AIM_OPENING)
    val turnState: StateFlow<TurnState> = _turnState.asStateFlow()

    private val _hudState = MutableStateFlow(BattleHudState())
    val hudState: StateFlow<BattleHudState> = _hudState.asStateFlow()

    private val _matchStats = MutableStateFlow<MatchStatistics?>(null)
    val matchStats: StateFlow<MatchStatistics?> = _matchStats.asStateFlow()

    private var damageDealtInMatch = 0
    private var damageTakenInMatch = 0
    private var maxComboInMatch = 0
    private var wallBouncesInMatch = 0
    private var ultimatesUsedInMatch = 0
    private var countersUsedInMatch = 0
    private var matchStartMillis = 0L
    private var latestBannerText = ""

    private var isBattlePaused = false
    private var isAppInForeground = true

    private var matchmakingJob: Job? = null
    private var leaderboardObserverJob: Job? = null
    private var roomObserverJob: Job? = null

    private var activeTransport = ActiveTransport.NONE
    private var isLanHost = false
    private var lanSyncAccumulator = 0f

    private var activeFirestoreRoomId: String? = null
    private var isFirestoreHost = false
    private var activeHostUid: String = ""
    private var activeGuestUid: String = ""
    private var activeMatchSeed: Int = 12345
    private var localCmdSeq = 0
    private var localLastDirX = 0f
    private var localLastDirY = -1f
    private var localUltSeq = 0
    private var localCounterSeq = 0
    private var lastAppliedRemoteCmdSeq = 0
    private var lastAppliedRemoteUltSeq = 0
    private var lastAppliedRemoteCounterSeq = 0
    private var lastAppliedPackedSync = ""
    private var firestoreSyncAccumulator = 0f

    private var lastDashStretchRatio = 0.75f

    init {
        soundManager.isEnabled = _engineSettings.value.sfxEnabled
        battleAudioDirector.voiceLinesEnabled = _engineSettings.value.voiceLinesEnabled
        battleAudioDirector.musicEnabled = _engineSettings.value.musicEnabled
        battleAudioDirector.prefetchCharacterVoices(
            CharacterRoster.getById(_playerProfile.value.selectedCharacterId),
            _customOpponentCharacter.value
        )
        observeLanEvents()
        viewModelScope.launch {
            delay(650)
            if (_currentScreen.value == AppScreen.LOADING) {
                _currentScreen.value = AppScreen.HOME_HUB
            }
        }
    }

    override fun onCleared() {
        cleanupActiveNetworkSession()
        leaderboardObserverJob?.cancel()
        battleAudioDirector.release()
        soundManager.release()
        super.onCleared()
    }

    /**
     * Auth-gated initialization: called strictly once `FirebaseAuth.currentUser != null`.
     */
    fun bindAuthenticatedUser(userId: String, displayName: String?) {
        if (userId.isBlank() || boundUserId == userId) return
        boundUserId = userId

        val repo = OnlineArenaRepository(getApplication())
        onlineRepository = repo

        if (!displayName.isNullOrBlank() && _playerProfile.value.username == "Player") {
            val cleanDisplay = displayName.trim().take(16)
            if (cleanDisplay.isNotEmpty()) {
                val updated = _playerProfile.value.copy(username = cleanDisplay)
                _playerProfile.value = updated
                prefs.edit().putString("username", cleanDisplay).apply()
            }
        }

        viewModelScope.launch {
            try {
                val cloud = repo.fetchOrCreateUserProfile(
                    userId = userId,
                    fallbackProfile = _playerProfile.value,
                    fallbackUnlocked = _unlockedCharacterIds.value,
                    fallbackUpgrades = _characterUpgrades.value
                )
                val local = _playerProfile.value
                val mergedWins = max(local.wins, cloud.profile.wins)
                val mergedLosses = max(local.losses, cloud.profile.losses)
                val mergedRating = max(local.rankedRating, cloud.profile.rankedRating)
                val mergedLevel = max(local.level, cloud.profile.level)
                val mergedProfile = cloud.profile.copy(
                    username = if (local.username != "Player") local.username else cloud.profile.username,
                    level = mergedLevel,
                    exp = if (mergedLevel == cloud.profile.level) max(local.exp, cloud.profile.exp) else cloud.profile.exp,
                    maxExp = 200 + (mergedLevel - 1) * 100,
                    gold = max(local.gold, cloud.profile.gold),
                    gems = max(local.gems, cloud.profile.gems),
                    wins = mergedWins,
                    losses = mergedLosses,
                    rankedRating = mergedRating,
                    rankTier = PlayerProfile.computeTier(mergedRating, mergedWins + mergedLosses)
                )
                val mergedUnlocked = _unlockedCharacterIds.value + cloud.unlockedIds + CharacterRoster.starterIds
                val mergedUpgrades = mergeUpgradeMaps(_characterUpgrades.value, cloud.upgrades)

                _playerProfile.value = mergedProfile
                _unlockedCharacterIds.value = mergedUnlocked
                _characterUpgrades.value = mergedUpgrades
                persistFullLocalState(mergedProfile, mergedUnlocked, mergedUpgrades)
                refreshLeaderboardState()

                repo.saveUserProfile(userId, mergedProfile, mergedUnlocked, mergedUpgrades)
                repo.syncLeaderboardEntry(userId, mergedProfile)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep local state playable if network is temporarily unreachable
            }
        }

        leaderboardObserverJob?.cancel()
        leaderboardObserverJob = viewModelScope.launch {
            repo.observeOnlineLeaderboard()
                .catch { /* logged by FirestoreErrorHandler */ }
                .collect { docs ->
                    cachedOnlineLeaderboard = docs.mapIndexed { idx, doc ->
                        doc.toLeaderboardEntry(rank = idx + 1, currentUserId = userId)
                    }
                    refreshLeaderboardState()
                }
        }
    }

    private fun refreshLeaderboardState() {
        _leaderboard.value = InitialGameData.buildLeaderboard(
            profile = _playerProfile.value,
            onlineEntries = cachedOnlineLeaderboard
        )
    }

    private fun pushCloudSyncAsync() {
        val uid = boundUserId ?: return
        val repo = onlineRepository ?: return
        val profile = _playerProfile.value
        val unlocked = _unlockedCharacterIds.value
        val upgrades = _characterUpgrades.value
        viewModelScope.launch {
            try {
                repo.saveUserProfile(uid, profile, unlocked, upgrades)
                repo.syncLeaderboardEntry(uid, profile)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    fun navigateTo(screen: AppScreen, recordHistory: Boolean = true) {
        val previous = _currentScreen.value
        if (previous == screen) return

        refreshDailyQuestsIfNeeded()

        if (screen != AppScreen.MATCHMAKING && screen != AppScreen.BATTLE_ARENA && screen != AppScreen.MULTIPLAYER_LOBBY) {
            cleanupActiveNetworkSession()
        }
        if (screen != AppScreen.MATCHMAKING) {
            matchmakingJob?.cancel()
            _canStartRankedChallenger.value = false
        }
        if (screen == AppScreen.MULTIPLAYER_LOBBY) {
            _localIpAddress.value = lanManager.detectLocalIpv4()
            _lobbyStatusMessage.value = null
        }

        if (recordHistory &&
            previous != AppScreen.LOADING &&
            previous != AppScreen.MATCHMAKING &&
            previous != AppScreen.BATTLE_ARENA &&
            previous != AppScreen.RESULT
        ) {
            if (navBackStack.lastOrNull() != previous) {
                navBackStack.addLast(previous)
                while (navBackStack.size > 16) {
                    navBackStack.removeFirst()
                }
            }
        }

        _lastNavWasPop.value = false
        isBattlePaused = false
        soundManager.playUiTap()
        _currentScreen.value = screen
        syncAudioDirectorWithState()
    }

    fun navigateToHomeReset() {
        navBackStack.clear()
        battleOriginScreen = AppScreen.HOME_HUB
        cleanupActiveNetworkSession()
        matchmakingJob?.cancel()
        _canStartRankedChallenger.value = false
        _lastNavWasPop.value = true
        isBattlePaused = false
        soundManager.playUiTap()
        _currentScreen.value = AppScreen.HOME_HUB
        syncAudioDirectorWithState()
    }

    /**
     * Steps back one action/screen in history rather than jumping straight to HomeHub.
     * Returns true if handled internally, or false if already at root so Activity can exit naturally.
     */
    fun navigateBack(): Boolean {
        val current = _currentScreen.value
        if (current == AppScreen.LOADING) return true

        if (current == AppScreen.MATCHMAKING) {
            cleanupActiveNetworkSession()
            matchmakingJob?.cancel()
            _canStartRankedChallenger.value = false
            _lastNavWasPop.value = true
            isBattlePaused = false
            soundManager.playUiTap()
            _currentScreen.value = battleOriginScreen
            syncAudioDirectorWithState()
            return true
        }

        if (current == AppScreen.BATTLE_ARENA) {
            forfeitAndExitArena(toOrigin = true)
            return true
        }

        if (current == AppScreen.RESULT) {
            cleanupActiveNetworkSession()
            _lastNavWasPop.value = true
            isBattlePaused = false
            soundManager.playUiTap()
            _currentScreen.value = battleOriginScreen
            syncAudioDirectorWithState()
            return true
        }

        while (navBackStack.isNotEmpty() && navBackStack.last() == current) {
            navBackStack.removeLast()
        }

        if (navBackStack.isNotEmpty()) {
            val target = navBackStack.removeLast()
            if (target != AppScreen.MULTIPLAYER_LOBBY) {
                cleanupActiveNetworkSession()
            }
            _lastNavWasPop.value = true
            soundManager.playUiTap()
            _currentScreen.value = target
            syncAudioDirectorWithState()
            return true
        }

        if (current != AppScreen.HOME_HUB) {
            cleanupActiveNetworkSession()
            _lastNavWasPop.value = true
            soundManager.playUiTap()
            _currentScreen.value = AppScreen.HOME_HUB
            syncAudioDirectorWithState()
            return true
        }

        return false
    }

    fun forfeitAndExitArena(toOrigin: Boolean = true) {
        val roomId = activeFirestoreRoomId
        val repo = onlineRepository
        val sim = _battleSim.value
        if (activeTransport == ActiveTransport.FIRESTORE && roomId != null && repo != null && sim?.isRoundActive == true) {
            if (isFirestoreHost) {
                val winner = activeGuestUid.ifBlank { boundUserId.orEmpty() }
                if (winner.isNotBlank()) {
                    pushFirestoreCombatStateAsync(status = "finished", winnerUid = winner)
                }
            } else if (activeHostUid.isNotBlank()) {
                viewModelScope.launch {
                    repo.sendGuestForfeit(roomId, activeHostUid)
                }
            }
        }
        sim?.isRoundActive = false
        cleanupActiveNetworkSession()
        isBattlePaused = false
        _lastNavWasPop.value = true
        soundManager.playUiTap()
        _currentScreen.value = if (toOrigin) battleOriginScreen else AppScreen.HOME_HUB
        syncAudioDirectorWithState()
    }

    fun setBattlePaused(paused: Boolean) {
        isBattlePaused = paused
        syncAudioDirectorWithState()
    }

    fun onAppPause() {
        isAppInForeground = false
        battleAudioDirector.stopBattleMusic()
    }

    fun onAppResume() {
        isAppInForeground = true
        refreshDailyQuestsIfNeeded()
        syncAudioDirectorWithState()
    }

    private fun syncAudioDirectorWithState() {
        val sim = _battleSim.value
        val inLiveArena = isAppInForeground &&
            !isBattlePaused &&
            _currentScreen.value == AppScreen.BATTLE_ARENA &&
            sim != null &&
            sim.isRoundActive &&
            _turnState.value != TurnState.KNOCKOUT_SLOWMO

        if (inLiveArena && sim != null) {
            val pRatio = (sim.playerBall.currentHp / sim.playerBall.maxHp.coerceAtLeast(1f)).coerceIn(0f, 1f)
            val oRatio = (sim.opponentBall.currentHp / sim.opponentBall.maxHp.coerceAtLeast(1f)).coerceIn(0f, 1f)
            battleAudioDirector.updateBattleSituation(
                inBattleArena = true,
                isClashStarted = sim.isClashStarted,
                eitherUltimateActive = sim.playerBall.isUltimateActive || sim.opponentBall.isUltimateActive,
                lowestHpRatio = min(pRatio, oRatio)
            )
        } else {
            battleAudioDirector.stopBattleMusic()
        }
    }

    fun setBotDifficulty(difficulty: BotDifficulty) {
        _botDifficulty.value = difficulty
        soundManager.playUiTap()
    }

    fun selectCustomOpponent(character: CharacterData) {
        _customOpponentCharacter.value = character
        battleAudioDirector.prefetchCharacterVoices(character)
        soundManager.playUiTap()
    }

    fun startCustomMatch() {
        cleanupActiveNetworkSession()
        battleOriginScreen = AppScreen.CUSTOM_MATCH
        activeMatchSeed = Random.nextInt(10_000, 999_999)
        _selectedMode.value = GameMode.CUSTOM_MATCH
        val rival = _customOpponentCharacter.value
        _opponentCharacter.value = rival
        _opponentName.value = "${_botDifficulty.value.shortLabel} ${rival.name}"
        _lastNavWasPop.value = false
        soundManager.playUiTap()
        _currentScreen.value = AppScreen.MATCHMAKING
        _canStartRankedChallenger.value = false
        syncAudioDirectorWithState()

        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            _matchmakingCountdown.value = 2
            delay(400)
            _matchmakingCountdown.value = 1
            delay(400)
            setupBattle(isRemote = false, remoteUpgrades = null)
            _currentScreen.value = AppScreen.BATTLE_ARENA
            syncAudioDirectorWithState()
        }
    }

    // ==================== LAN WI-FI MULTIPLAYER ====================

    fun startLanDiscovery() {
        _localIpAddress.value = lanManager.detectLocalIpv4()
        lanManager.startDiscovery()
    }

    fun hostLanRoom() {
        cleanupFirestoreRoomSession()
        _localIpAddress.value = lanManager.detectLocalIpv4()
        _lobbyStatusMessage.value = null
        val profile = _playerProfile.value
        val peer = LanPeerInfo(
            username = profile.username,
            characterId = profile.selectedCharacterId,
            upgrades = getUpgradesFor(profile.selectedCharacterId)
        )
        soundManager.playUiTap()
        lanManager.startHosting(peer)
    }

    fun joinLanAddress(rawAddress: String) {
        cleanupFirestoreRoomSession()
        _lobbyStatusMessage.value = null
        val profile = _playerProfile.value
        val peer = LanPeerInfo(
            username = profile.username,
            characterId = profile.selectedCharacterId,
            upgrades = getUpgradesFor(profile.selectedCharacterId)
        )
        soundManager.playUiTap()
        lanManager.joinByAddress(rawAddress, peer)
    }

    fun cancelLanSession() {
        lanManager.closeSession()
        if (activeTransport == ActiveTransport.LAN) {
            activeTransport = ActiveTransport.NONE
        }
    }

    private fun observeLanEvents() {
        viewModelScope.launch {
            lanManager.sessionState.collect { state ->
                if (state is LanSessionState.Connected && _currentScreen.value == AppScreen.MULTIPLAYER_LOBBY) {
                    startLanConnectedBattle(isHost = state.isHost, peer = state.peer)
                }
            }
        }

        viewModelScope.launch {
            lanManager.incomingPackets.collect { pkt ->
                if (activeTransport != ActiveTransport.LAN) return@collect
                val sim = _battleSim.value ?: return@collect
                when (pkt) {
                    is LanPacket.StartClash -> {
                        sim.applyRemoteOpponentCommand(Vector2(pkt.dirX, -pkt.dirY), isOpening = true)
                        if (_turnState.value == TurnState.AIM_OPENING) {
                            _turnState.value = TurnState.NONSTOP_CLASH
                        }
                        syncHudState(sim, force = true)
                    }

                    is LanPacket.RedirectDash -> {
                        sim.applyRemoteOpponentCommand(Vector2(pkt.dirX, -pkt.dirY), isOpening = false)
                        syncHudState(sim, force = true)
                    }

                    LanPacket.TriggerUlt -> {
                        if (!sim.opponentBall.isUltimateActive) {
                            sim.opponentBall.ultimateCooldownRemaining = 0f
                            sim.opponentBall.ultimateGauge = 100f
                            sim.triggerUltimate(sim.opponentBall)
                            syncHudState(sim, force = true)
                        }
                    }

                    LanPacket.TriggerCounter -> {
                        if (!sim.opponentBall.isCounterActive) {
                            sim.opponentBall.counterCooldownRemaining = 0f
                            sim.triggerCounter(sim.opponentBall)
                            syncHudState(sim, force = true)
                        }
                    }

                    is LanPacket.HostStateSync -> {
                        if (!isLanHost && sim.isRoundActive) {
                            if (_turnState.value == TurnState.AIM_OPENING) {
                                _turnState.value = TurnState.NONSTOP_CLASH
                            }
                            sim.applyMirroredLanSync(
                                localNormX = pkt.guestX,
                                localNormY = 1f - pkt.guestY,
                                localNormVx = pkt.guestVx,
                                localNormVy = -pkt.guestVy,
                                remoteNormX = pkt.hostX,
                                remoteNormY = 1f - pkt.hostY,
                                remoteNormVx = pkt.hostVx,
                                remoteNormVy = -pkt.hostVy,
                                localHp = pkt.guestHp,
                                remoteHp = pkt.hostHp,
                                localUlt = pkt.guestUlt,
                                remoteUlt = pkt.hostUlt
                            )
                            syncHudState(sim, force = false)
                        }
                    }

                    is LanPacket.Knockout -> {
                        if (sim.isRoundActive) {
                            val didLocalWin = if (isLanHost) pkt.hostWon else !pkt.hostWon
                            val localFinalHp = if (isLanHost) pkt.hostFinalHp else pkt.guestFinalHp
                            val oppFinalHp = if (isLanHost) pkt.guestFinalHp else pkt.hostFinalHp
                            sim.forceAuthoritativeFinish(
                                isPlayerWinner = didLocalWin,
                                finalPlayerHp = localFinalHp,
                                finalOpponentHp = oppFinalHp
                            )
                        }
                    }

                    LanPacket.PeerDisconnected -> {
                        if (_currentScreen.value == AppScreen.BATTLE_ARENA && sim.isRoundActive) {
                            latestBannerText = "FRIEND LEFT • YOU WIN"
                            sim.forceAuthoritativeFinish(
                                isPlayerWinner = true,
                                finalPlayerHp = max(1f, sim.playerBall.currentHp),
                                finalOpponentHp = 0f
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startLanConnectedBattle(isHost: Boolean, peer: LanPeerInfo) {
        activeTransport = ActiveTransport.LAN
        isLanHost = isHost
        lanSyncAccumulator = 0f
        battleOriginScreen = AppScreen.MULTIPLAYER_LOBBY
        activeMatchSeed = (peer.username.hashCode() xor _playerProfile.value.username.hashCode()) and 0x7FFFFFFF
        _selectedMode.value = GameMode.MULTIPLAYER
        _opponentName.value = peer.username
        _opponentCharacter.value = CharacterRoster.getById(peer.characterId)
        _lastNavWasPop.value = false
        setupBattle(isRemote = true, remoteUpgrades = peer.upgrades)
        _currentScreen.value = AppScreen.BATTLE_ARENA
        syncAudioDirectorWithState()
    }

    // ==================== INTERNET ROOM CODE & ONLINE RANKED ====================

    fun createOnlineFriendRoom() {
        val uid = boundUserId ?: return
        val repo = onlineRepository ?: return
        cancelLanSession()
        cleanupFirestoreRoomSession()
        _isOnlineRoomBusy.value = true
        _lobbyStatusMessage.value = null
        soundManager.playUiTap()

        viewModelScope.launch {
            try {
                val profile = _playerProfile.value
                val upg = getUpgradesFor(profile.selectedCharacterId)
                val created = repo.createFriendRoom(uid, profile, upg)
                activeFirestoreRoomId = created.roomId
                isFirestoreHost = true
                _activeRoomCode.value = created.roomCode
                _isOnlineRoomBusy.value = false
                observeFirestoreMatchRoom(created.roomId, GameMode.MULTIPLAYER)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isOnlineRoomBusy.value = false
                _lobbyStatusMessage.value = "Unable to create online room. Check internet connection."
            }
        }
    }

    fun joinOnlineFriendRoom(rawCode: String) {
        val uid = boundUserId ?: return
        val repo = onlineRepository ?: return
        cancelLanSession()
        cleanupFirestoreRoomSession()
        _isOnlineRoomBusy.value = true
        _lobbyStatusMessage.value = null
        soundManager.playUiTap()

        viewModelScope.launch {
            try {
                val profile = _playerProfile.value
                val upg = getUpgradesFor(profile.selectedCharacterId)
                val joined = repo.joinFriendRoomByCode(rawCode, uid, profile, upg)
                _isOnlineRoomBusy.value = false
                if (joined == null) {
                    _lobbyStatusMessage.value = "Room '$rawCode' not found or already full."
                    return@launch
                }
                activeFirestoreRoomId = joined.roomId
                isFirestoreHost = false
                _activeRoomCode.value = null
                startFirestoreMultiplayerBattle(joined, GameMode.MULTIPLAYER)
                observeFirestoreMatchRoom(joined.roomId, GameMode.MULTIPLAYER)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _isOnlineRoomBusy.value = false
                _lobbyStatusMessage.value = "Could not join room '$rawCode'. Check the code and try again."
            }
        }
    }

    fun cancelOnlineRoom() {
        cleanupFirestoreRoomSession()
        _lobbyStatusMessage.value = null
    }

    private fun observeFirestoreMatchRoom(roomId: String, mode: GameMode) {
        val repo = onlineRepository ?: return
        roomObserverJob?.cancel()
        roomObserverJob = viewModelScope.launch {
            repo.observeMatchRoom(roomId)
                .catch { /* logged by FirestoreErrorHandler */ }
                .collect { room ->
                    if (room == null) return@collect
                    handleIncomingFirestoreRoomSnapshot(room, mode)
                }
        }
    }

    private fun handleIncomingFirestoreRoomSnapshot(room: MatchRoomDoc, mode: GameMode) {
        val uid = boundUserId ?: return
        activeHostUid = room.hostUid
        activeGuestUid = room.guestUid

        // Host waiting in lobby or ranked queue -> guest just joined!
        if (activeTransport != ActiveTransport.FIRESTORE &&
            room.status == "active" &&
            room.guestUid.isNotEmpty()
        ) {
            matchmakingJob?.cancel()
            _canStartRankedChallenger.value = false
            _activeRoomCode.value = null
            startFirestoreMultiplayerBattle(room, mode)
            return
        }

        if (activeTransport != ActiveTransport.FIRESTORE) return
        val sim = _battleSim.value ?: return

        val remoteCmdSeq = if (isFirestoreHost) room.guestCmdSeq else room.hostCmdSeq
        val remoteDirX = if (isFirestoreHost) room.guestDirX else room.hostDirX
        val remoteDirY = if (isFirestoreHost) room.guestDirY else room.hostDirY
        val remoteUltSeq = if (isFirestoreHost) room.guestUltSeq else room.hostUltSeq
        val remoteCounterSeq = if (isFirestoreHost) room.guestCounterSeq else room.hostCounterSeq

        if (remoteCmdSeq > lastAppliedRemoteCmdSeq) {
            val isFirstMove = lastAppliedRemoteCmdSeq == 0 && !sim.isClashStarted
            lastAppliedRemoteCmdSeq = remoteCmdSeq
            sim.applyRemoteOpponentCommand(Vector2(remoteDirX, -remoteDirY), isOpening = isFirstMove)
            if (_turnState.value == TurnState.AIM_OPENING) {
                _turnState.value = TurnState.NONSTOP_CLASH
            }
            syncHudState(sim, force = true)
        }

        if (remoteUltSeq > lastAppliedRemoteUltSeq) {
            lastAppliedRemoteUltSeq = remoteUltSeq
            if (!sim.opponentBall.isUltimateActive) {
                sim.opponentBall.ultimateCooldownRemaining = 0f
                sim.opponentBall.ultimateGauge = 100f
                sim.triggerUltimate(sim.opponentBall)
                syncHudState(sim, force = true)
            }
        }

        if (remoteCounterSeq > lastAppliedRemoteCounterSeq) {
            lastAppliedRemoteCounterSeq = remoteCounterSeq
            if (!sim.opponentBall.isCounterActive) {
                sim.opponentBall.counterCooldownRemaining = 0f
                sim.triggerCounter(sim.opponentBall)
                syncHudState(sim, force = true)
            }
        }

        // Zero-desync Host-Authoritative State Reconciliation on Guest
        if (!isFirestoreHost && sim.isRoundActive && room.syncStatePacked.isNotBlank() && room.syncStatePacked != lastAppliedPackedSync) {
            lastAppliedPackedSync = room.syncStatePacked
            if (_turnState.value == TurnState.AIM_OPENING) {
                _turnState.value = TurnState.NONSTOP_CLASH
            }
            sim.applyPackedHostSyncOnGuest(room.syncStatePacked)
            syncHudState(sim, force = false)
        } else if (!isFirestoreHost && sim.isRoundActive && sim.isClashStarted) {
            // Ensure HP stays locked to Host's authoritative HP even before packed state arrives
            if (room.guestSyncHp >= 0) {
                sim.playerBall.currentHp = room.guestSyncHp.toFloat().coerceIn(0f, sim.playerBall.maxHp)
            }
            if (room.hostSyncHp >= 0) {
                sim.opponentBall.currentHp = room.hostSyncHp.toFloat().coerceIn(0f, sim.opponentBall.maxHp)
            }
        }

        if (room.status == "finished" && room.winnerUid.isNotEmpty() && sim.isRoundActive) {
            val didWin = room.winnerUid == uid
            val myFinalHp = if (isFirestoreHost) room.hostSyncHp.toFloat() else room.guestSyncHp.toFloat()
            val oppFinalHp = if (isFirestoreHost) room.guestSyncHp.toFloat() else room.hostSyncHp.toFloat()
            sim.forceAuthoritativeFinish(
                isPlayerWinner = didWin,
                finalPlayerHp = myFinalHp,
                finalOpponentHp = oppFinalHp
            )
        }
    }

    private fun startFirestoreMultiplayerBattle(room: MatchRoomDoc, mode: GameMode) {
        activeTransport = ActiveTransport.FIRESTORE
        activeFirestoreRoomId = room.roomId
        activeHostUid = room.hostUid
        activeGuestUid = room.guestUid
        activeMatchSeed = room.seed
        localCmdSeq = 0
        localUltSeq = 0
        localCounterSeq = 0
        lastAppliedRemoteCmdSeq = 0
        lastAppliedRemoteUltSeq = 0
        lastAppliedRemoteCounterSeq = 0
        lastAppliedPackedSync = ""
        firestoreSyncAccumulator = 0f
        battleOriginScreen = if (mode == GameMode.MULTIPLAYER) AppScreen.MULTIPLAYER_LOBBY else AppScreen.HOME_HUB
        _selectedMode.value = mode

        val oppName = if (isFirestoreHost) room.guestName.ifBlank { "Online Rival" } else room.hostName.ifBlank { "Online Rival" }
        val oppCharId = if (isFirestoreHost) room.guestCharId else room.hostCharId
        val oppUpg = if (isFirestoreHost) {
            CharacterUpgradeState(room.guestHpLvl, room.guestPwrLvl, room.guestCdLvl)
        } else {
            CharacterUpgradeState(room.hostHpLvl, room.hostPwrLvl, room.hostCdLvl)
        }

        _opponentName.value = oppName
        _opponentCharacter.value = CharacterRoster.getById(oppCharId)
        _lastNavWasPop.value = false
        setupBattle(isRemote = true, remoteUpgrades = oppUpg)
        _currentScreen.value = AppScreen.BATTLE_ARENA
        syncAudioDirectorWithState()
    }

    private fun pushFirestoreCombatStateAsync(status: String = "active", winnerUid: String = "") {
        val roomId = activeFirestoreRoomId ?: return
        val repo = onlineRepository ?: return
        val sim = _battleSim.value ?: return
        val myHp = sim.playerBall.currentHp.toInt().coerceAtLeast(0)
        val oppHp = sim.opponentBall.currentHp.toInt().coerceAtLeast(0)
        val cmd = localCmdSeq
        val dx = localLastDirX
        val dy = localLastDirY
        val ult = localUltSeq
        val ctr = localCounterSeq
        val hostFlag = isFirestoreHost
        val packed = if (hostFlag && sim.isArenaInitialized) sim.packHostSyncState() else ""

        viewModelScope.launch {
            try {
                if (hostFlag) {
                    repo.sendHostCombatUpdate(
                        roomId = roomId,
                        cmdSeq = cmd,
                        dirX = dx,
                        dirY = dy,
                        ultSeq = ult,
                        hostSyncHp = myHp,
                        guestSyncHp = oppHp,
                        status = status,
                        winnerUid = winnerUid,
                        counterSeq = ctr,
                        syncStatePacked = packed
                    )
                } else {
                    repo.sendGuestCombatUpdate(
                        roomId = roomId,
                        cmdSeq = cmd,
                        dirX = dx,
                        dirY = dy,
                        ultSeq = ult,
                        guestSyncHp = null,
                        status = status,
                        winnerUid = winnerUid,
                        counterSeq = ctr
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    private fun cleanupFirestoreRoomSession() {
        roomObserverJob?.cancel()
        roomObserverJob = null
        val oldRoomId = activeFirestoreRoomId
        val wasHost = isFirestoreHost
        activeFirestoreRoomId = null
        _activeRoomCode.value = null
        _isOnlineRoomBusy.value = false
        if (activeTransport == ActiveTransport.FIRESTORE) {
            activeTransport = ActiveTransport.NONE
        }
        if (oldRoomId != null && wasHost) {
            val repo = onlineRepository
            if (repo != null) {
                viewModelScope.launch {
                    repo.deleteRoomSafely(oldRoomId)
                }
            }
        }
    }

    private fun cleanupActiveNetworkSession() {
        cancelLanSession()
        cleanupFirestoreRoomSession()
    }

    // ==================== SETTINGS & PROGRESSION ====================

    fun updateEngineSettings(newSettings: EngineSettings) {
        val synced = newSettings.copy(bloomPostFx = newSettings.renderMode.bloomGlow)
        _engineSettings.value = synced
        soundManager.isEnabled = synced.sfxEnabled
        battleAudioDirector.voiceLinesEnabled = synced.voiceLinesEnabled
        battleAudioDirector.musicEnabled = synced.musicEnabled
        _battleSim.value?.engineSettings = synced
        persistEngineSettings(synced)
        syncAudioDirectorWithState()
    }

    fun toggleSfx() {
        val next = !_engineSettings.value.sfxEnabled
        val updated = _engineSettings.value.copy(sfxEnabled = next)
        updateEngineSettings(updated)
        if (next) {
            soundManager.playUiTap()
        }
    }

    fun toggleMusic() {
        val next = !_engineSettings.value.musicEnabled
        val updated = _engineSettings.value.copy(musicEnabled = next)
        updateEngineSettings(updated)
        soundManager.playUiTap()
    }

    fun toggleVoiceLines() {
        val next = !_engineSettings.value.voiceLinesEnabled
        val updated = _engineSettings.value.copy(voiceLinesEnabled = next)
        updateEngineSettings(updated)
        soundManager.playUiTap()
    }

    fun setThemeMode(mode: AppThemeMode) {
        val updated = _engineSettings.value.copy(themeMode = mode)
        updateEngineSettings(updated)
        soundManager.playUiTap()
        triggerHaptic(14L)
    }

    fun updateUsername(newName: String) {
        val trimmed = newName.take(16).trim()
        if (trimmed.isNotEmpty()) {
            val updated = _playerProfile.value.copy(username = trimmed)
            _playerProfile.value = updated
            refreshLeaderboardState()
            prefs.edit().putString("username", trimmed).apply()
            pushCloudSyncAsync()
            soundManager.playUiTap()
        }
    }

    fun resetAccountToZero() {
        val savedSettings = _engineSettings.value
        prefs.edit().clear().apply()
        persistEngineSettings(savedSettings)
        val freshProfile = PlayerProfile()
        _playerProfile.value = freshProfile
        _unlockedCharacterIds.value = CharacterRoster.starterIds
        _characterUpgrades.value = emptyMap()
        _quests.value = InitialGameData.defaultQuests
        _dailyQuests.value = loadDailyQuests()
        refreshLeaderboardState()
        pushCloudSyncAsync()
        triggerHaptic(35L)
    }

    fun getUpgradesFor(charId: String): CharacterUpgradeState {
        return _characterUpgrades.value[charId] ?: CharacterUpgradeState()
    }

    fun upgradeCharacterStat(charId: String, statType: UpgradeStatType): Boolean {
        if (!_unlockedCharacterIds.value.contains(charId)) return false
        val current = getUpgradesFor(charId)
        if (current.isMaxed(statType)) return false

        val cost = current.nextCostGold(statType)
        val profile = _playerProfile.value
        if (profile.gold < cost) return false

        val updatedGold = profile.gold - cost
        val updatedProfile = profile.copy(gold = updatedGold)
        _playerProfile.value = updatedProfile

        val updatedUpgrade = when (statType) {
            UpgradeStatType.HEALTH -> current.copy(healthLevel = current.healthLevel + 1)
            UpgradeStatType.POWER -> current.copy(powerLevel = current.powerLevel + 1)
            UpgradeStatType.COOLDOWN -> current.copy(cooldownLevel = current.cooldownLevel + 1)
        }

        val updatedMap = _characterUpgrades.value.toMutableMap()
        updatedMap[charId] = updatedUpgrade
        _characterUpgrades.value = updatedMap

        prefs.edit()
            .putInt("gold", updatedGold)
            .putInt("upg_hp_$charId", updatedUpgrade.healthLevel)
            .putInt("upg_pwr_$charId", updatedUpgrade.powerLevel)
            .putInt("upg_cd_$charId", updatedUpgrade.cooldownLevel)
            .apply()

        val updatedQuests = _quests.value.map { q ->
            if (q.id == "q_upgrade_forge") {
                q.copy(currentProgress = (q.currentProgress + 1).coerceAtMost(q.targetProgress))
            } else q
        }
        _quests.value = updatedQuests
        persistQuests(updatedQuests)
        pushCloudSyncAsync()

        soundManager.playUpgradeForge()
        triggerHaptic(45L)
        return true
    }

    fun selectCharacter(charId: String) {
        if (!_unlockedCharacterIds.value.contains(charId)) return
        val updated = _playerProfile.value.copy(selectedCharacterId = charId)
        _playerProfile.value = updated
        battleAudioDirector.prefetchCharacterVoices(CharacterRoster.getById(charId))
        refreshLeaderboardState()
        prefs.edit().putString("selected_char", charId).apply()
        pushCloudSyncAsync()
        soundManager.playUiTap()
    }

    fun cycleShowcaseCharacter(delta: Int) {
        val unlockedList = CharacterRoster.characters.filter { _unlockedCharacterIds.value.contains(it.id) }
        if (unlockedList.isEmpty()) return
        val currentIdx = unlockedList.indexOfFirst { it.id == _playerProfile.value.selectedCharacterId }.coerceAtLeast(0)
        val nextIdx = (currentIdx + delta + unlockedList.size) % unlockedList.size
        selectCharacter(unlockedList[nextIdx].id)
        triggerHaptic(14L)
    }

    fun unlockCharacter(character: CharacterData): Boolean {
        if (_unlockedCharacterIds.value.contains(character.id)) return true
        val profile = _playerProfile.value
        if (profile.gold >= character.unlockCostGold && profile.gems >= character.unlockCostGems) {
            val updatedGold = profile.gold - character.unlockCostGold
            val updatedGems = profile.gems - character.unlockCostGems
            val updatedProfile = profile.copy(
                gold = updatedGold,
                gems = updatedGems,
                selectedCharacterId = character.id
            )
            _playerProfile.value = updatedProfile
            refreshLeaderboardState()

            val currentSet = _unlockedCharacterIds.value.toMutableSet()
            currentSet.add(character.id)
            _unlockedCharacterIds.value = currentSet

            prefs.edit()
                .putInt("gold", updatedGold)
                .putInt("gems", updatedGems)
                .putString("selected_char", character.id)
                .putStringSet("unlocked_chars", currentSet)
                .apply()
            pushCloudSyncAsync()
            soundManager.playUpgradeForge()
            triggerHaptic(45L)
            return true
        }
        return false
    }

    fun claimQuest(questId: String) {
        val mainList = _quests.value.toMutableList()
        val mainIndex = mainList.indexOfFirst { it.id == questId }
        if (mainIndex != -1 && !mainList[mainIndex].isClaimed && mainList[mainIndex].currentProgress >= mainList[mainIndex].targetProgress) {
            val q = mainList[mainIndex]
            mainList[mainIndex] = q.copy(isClaimed = true)
            _quests.value = mainList
            persistQuests(mainList)
            awardQuestRewards(q.rewardGold, q.rewardGems)
            return
        }

        val dailyList = _dailyQuests.value.toMutableList()
        val dailyIndex = dailyList.indexOfFirst { it.id == questId }
        if (dailyIndex != -1 && !dailyList[dailyIndex].isClaimed && dailyList[dailyIndex].currentProgress >= dailyList[dailyIndex].targetProgress) {
            val q = dailyList[dailyIndex]
            dailyList[dailyIndex] = q.copy(isClaimed = true)
            _dailyQuests.value = dailyList
            persistDailyQuests(dailyList)
            awardQuestRewards(q.rewardGold, q.rewardGems)
        }
    }

    fun claimAllReadyQuests() {
        var totalGold = 0
        var totalGems = 0

        val updatedMain = _quests.value.map { q ->
            if (!q.isClaimed && q.currentProgress >= q.targetProgress) {
                totalGold += q.rewardGold
                totalGems += q.rewardGems
                q.copy(isClaimed = true)
            } else q
        }
        val updatedDaily = _dailyQuests.value.map { q ->
            if (!q.isClaimed && q.currentProgress >= q.targetProgress) {
                totalGold += q.rewardGold
                totalGems += q.rewardGems
                q.copy(isClaimed = true)
            } else q
        }

        if (totalGold > 0 || totalGems > 0) {
            _quests.value = updatedMain
            _dailyQuests.value = updatedDaily
            persistQuests(updatedMain)
            persistDailyQuests(updatedDaily)
            awardQuestRewards(totalGold, totalGems)
        }
    }

    private fun awardQuestRewards(goldDelta: Int, gemsDelta: Int) {
        val profile = _playerProfile.value
        val updated = profile.copy(
            gold = profile.gold + goldDelta,
            gems = profile.gems + gemsDelta
        )
        _playerProfile.value = updated
        prefs.edit().putInt("gold", updated.gold).putInt("gems", updated.gems).apply()
        pushCloudSyncAsync()
        soundManager.playUpgradeForge()
        triggerHaptic(35L)
    }

    // ==================== MATCHMAKING & ARENA LIFECYCLE ====================

    fun startMatchmaking(mode: GameMode, difficulty: BotDifficulty = _botDifficulty.value) {
        refreshDailyQuestsIfNeeded()
        if (mode == GameMode.CUSTOM_MATCH) {
            _botDifficulty.value = difficulty
            startCustomMatch()
            return
        }
        if (mode == GameMode.MULTIPLAYER) {
            navigateTo(AppScreen.MULTIPLAYER_LOBBY)
            return
        }

        val current = _currentScreen.value
        if (current != AppScreen.MATCHMAKING && current != AppScreen.BATTLE_ARENA && current != AppScreen.RESULT && current != AppScreen.LOADING) {
            battleOriginScreen = current
        }

        cleanupActiveNetworkSession()
        activeMatchSeed = Random.nextInt(10_000, 999_999)
        _selectedMode.value = mode
        _botDifficulty.value = difficulty
        _canStartRankedChallenger.value = false
        _lastNavWasPop.value = false
        soundManager.playUiTap()
        _currentScreen.value = AppScreen.MATCHMAKING
        syncAudioDirectorWithState()

        val playerChar = CharacterRoster.getById(_playerProfile.value.selectedCharacterId)
        val candidates = CharacterRoster.characters.filter { it.id != playerChar.id }
        val oppChar = candidates.random()
        _opponentCharacter.value = oppChar

        if (mode == GameMode.ONLINE_MATCH) {
            _opponentName.value = "Searching Online..."
            _matchmakingCountdown.value = -1

            val uid = boundUserId
            val repo = onlineRepository
            matchmakingJob?.cancel()
            matchmakingJob = viewModelScope.launch {
                if (uid != null && repo != null) {
                    try {
                        val upg = getUpgradesFor(playerChar.id)
                        when (val result = repo.findOrCreateRankedRoom(uid, _playerProfile.value, upg)) {
                            is RankedQueueResult.JoinedExisting -> {
                                activeFirestoreRoomId = result.room.roomId
                                isFirestoreHost = false
                                _opponentName.value = result.room.hostName.ifBlank { "Online Challenger" }
                                _opponentCharacter.value = CharacterRoster.getById(result.room.hostCharId)
                                _matchmakingCountdown.value = 2
                                delay(450)
                                _matchmakingCountdown.value = 1
                                delay(450)
                                startFirestoreMultiplayerBattle(result.room, GameMode.ONLINE_MATCH)
                                observeFirestoreMatchRoom(result.room.roomId, GameMode.ONLINE_MATCH)
                                return@launch
                            }

                            is RankedQueueResult.HostingWaiting -> {
                                activeFirestoreRoomId = result.room.roomId
                                isFirestoreHost = true
                                activeHostUid = result.room.hostUid
                                activeMatchSeed = result.room.seed
                                observeFirestoreMatchRoom(result.room.roomId, GameMode.ONLINE_MATCH)
                                delay(2600)
                                if (_currentScreen.value == AppScreen.MATCHMAKING && activeTransport != ActiveTransport.FIRESTORE) {
                                    _canStartRankedChallenger.value = true
                                }
                                return@launch
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // Fall through to Challenger fallback option
                    }
                }
                delay(1200)
                startRankedChallengerFallback()
            }
            return
        }

        // Practice Bot mode (Easy = truly easy, Normal = Hard, Hard = Extreme)
        _opponentName.value = "${difficulty.shortLabel} ${oppChar.name}"
        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            _matchmakingCountdown.value = 2
            delay(460)
            _matchmakingCountdown.value = 1
            delay(460)
            setupBattle(isRemote = false, remoteUpgrades = null)
            _currentScreen.value = AppScreen.BATTLE_ARENA
            syncAudioDirectorWithState()
        }
    }

    fun startRankedChallengerFallback() {
        cleanupFirestoreRoomSession()
        _canStartRankedChallenger.value = false
        val playerChar = CharacterRoster.getById(_playerProfile.value.selectedCharacterId)
        val oppChar = CharacterRoster.characters.filter { it.id != playerChar.id }.random()
        _opponentCharacter.value = oppChar
        _opponentName.value = listOf(
            "KuroBlade", "RaidenZero", "SoraVortex", "Akira_JP",
            "AbyssWalker", "ZenithStrike", "CrimsonRift", "ValkyrieNine"
        ).random()

        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            _matchmakingCountdown.value = 2
            delay(420)
            _matchmakingCountdown.value = 1
            delay(420)
            _botDifficulty.value = if (_playerProfile.value.rankedRating >= 400) BotDifficulty.HARD else BotDifficulty.MEDIUM
            setupBattle(isRemote = false, remoteUpgrades = null)
            _currentScreen.value = AppScreen.BATTLE_ARENA
            syncAudioDirectorWithState()
        }
    }

    private fun setupBattle(isRemote: Boolean, remoteUpgrades: CharacterUpgradeState?) {
        damageDealtInMatch = 0
        damageTakenInMatch = 0
        maxComboInMatch = 0
        wallBouncesInMatch = 0
        ultimatesUsedInMatch = 0
        countersUsedInMatch = 0
        latestBannerText = ""
        isBattlePaused = false
        matchStartMillis = System.currentTimeMillis()

        val playerChar = CharacterRoster.getById(_playerProfile.value.selectedCharacterId)
        val oppChar = _opponentCharacter.value
        battleAudioDirector.prefetchCharacterVoices(playerChar, oppChar)
        val playerUpg = getUpgradesFor(playerChar.id)

        val oppUpg = if (isRemote && remoteUpgrades != null) {
            remoteUpgrades
        } else {
            val aiTierLv = when (_selectedMode.value) {
                GameMode.ONLINE_MATCH -> (_playerProfile.value.level / 2 + 2).coerceIn(2, 7)
                GameMode.MULTIPLAYER -> 2
                GameMode.PRACTICE_BOT, GameMode.CUSTOM_MATCH -> when (_botDifficulty.value) {
                    BotDifficulty.EASY -> 0
                    BotDifficulty.MEDIUM -> 3
                    BotDifficulty.HARD -> 6
                }
            }
            CharacterUpgradeState(
                healthLevel = aiTierLv,
                powerLevel = aiTierLv,
                cooldownLevel = aiTierLv
            )
        }

        val pBall = CombatBall(
            id = "player",
            name = _playerProfile.value.username,
            position = Vector2(500f, 1100f),
            velocity = Vector2.Zero,
            radius = 52f,
            character = playerChar,
            upgrades = playerUpg
        )

        val oBall = CombatBall(
            id = "opponent",
            name = _opponentName.value,
            position = Vector2(500f, 340f),
            velocity = Vector2.Zero,
            radius = 52f,
            character = oppChar,
            upgrades = oppUpg
        )

        val rivalTag = if (isRemote) "RIVAL" else "BOT"
        val isGuest = isRemote && (
            (activeTransport == ActiveTransport.LAN && !isLanHost) ||
                (activeTransport == ActiveTransport.FIRESTORE && !isFirestoreHost)
            )

        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = _engineSettings.value,
            isRemoteMultiplayer = isRemote,
            isGuestClient = isGuest,
            randomSeed = activeMatchSeed,
            onCollisionImpact = { isPlayerAttacker, damage, isCrit, passiveLabel ->
                soundManager.playBallClash(isCrit)
                triggerHaptic(if (isCrit) 55L else 25L)
                if (isPlayerAttacker) {
                    damageDealtInMatch += damage
                } else {
                    damageTakenInMatch += damage
                }
                latestBannerText = "$passiveLabel • -$damage HP"
            },
            onWallBounce = { ball ->
                soundManager.playWallBounce(ball.wallBounceCombo)
                wallBouncesInMatch++
                if (ball.wallBounceCombo > maxComboInMatch) {
                    maxComboInMatch = ball.wallBounceCombo
                }
            },
            onKnockout = { isPlayerWinner ->
                _turnState.value = TurnState.KNOCKOUT_SLOWMO
                battleAudioDirector.stopBattleMusic()
                soundManager.playKnockout()
                triggerHaptic(140L)
                if (activeTransport == ActiveTransport.LAN && isLanHost) {
                    lanManager.sendKnockout(
                        hostWon = isPlayerWinner,
                        hostFinalHp = pBall.currentHp,
                        guestFinalHp = oBall.currentHp
                    )
                } else if (activeTransport == ActiveTransport.FIRESTORE && isFirestoreHost) {
                    val winnerUid = if (isPlayerWinner) activeHostUid else activeGuestUid
                    if (winnerUid.isNotEmpty()) {
                        pushFirestoreCombatStateAsync(status = "finished", winnerUid = winnerUid)
                    }
                }
                handleMatchEnd(isPlayerWinner)
            },
            onUltimateActivated = { isPlayer, ultName ->
                soundManager.playAwakening(isPlayer)
                val speakingChar = if (isPlayer) playerChar else oppChar
                battleAudioDirector.speakUltimateVoiceLine(speakingChar, isPlayer)
                triggerHaptic(80L)
                latestBannerText = if (isPlayer) {
                    "ULTIMATE: $ultName • ${pBall.multiDashChargesRemaining}x MULTI-DASH!"
                } else {
                    "$rivalTag USED $ultName!"
                }
            },
            onPassiveTriggered = { isPlayer, passiveName ->
                soundManager.playPassiveTrigger()
                val who = if (isPlayer) "YOU" else rivalTag
                latestBannerText = "$who • $passiveName"
            },
            onRedirectUsed = { isPlayer ->
                if (isPlayer) {
                    soundManager.playRedirectDash(lastDashStretchRatio)
                }
            },
            onCounterActivated = { isPlayer, counterName ->
                soundManager.playCounterParry()
                triggerHaptic(if (isPlayer) 42L else 18L)
                if (isPlayer) {
                    countersUsedInMatch++
                }
                val who = if (isPlayer) "YOU" else rivalTag
                latestBannerText = "$who COUNTER: $counterName!"
            }
        )
        sim.activeBotDifficulty = _botDifficulty.value

        _battleSim.value = sim
        _turnState.value = TurnState.AIM_OPENING
        syncHudState(sim, force = true)
    }

    fun stepBattleFrame(dt: Float) {
        if (isBattlePaused) return
        val sim = _battleSim.value ?: return
        if (!sim.isArenaInitialized) return

        sim.update(dt)

        if (sim.isRoundActive && _turnState.value == TurnState.NONSTOP_CLASH) {
            if (!sim.isRemoteMultiplayer) {
                sim.performAiTacticalStep(dt, _botDifficulty.value)
            } else if (activeTransport == ActiveTransport.LAN && isLanHost) {
                lanSyncAccumulator += dt
                if (lanSyncAccumulator >= 0.05f) {
                    lanSyncAccumulator = 0f
                    val w = sim.arenaWidth.coerceAtLeast(1f)
                    val h = sim.arenaHeight.coerceAtLeast(1f)
                    val spd = sim.baseArenaSpeed.coerceAtLeast(200f)
                    lanManager.sendHostStateSync(
                        hostX = sim.playerBall.position.x / w,
                        hostY = sim.playerBall.position.y / h,
                        hostVx = sim.playerBall.velocity.x / spd,
                        hostVy = sim.playerBall.velocity.y / spd,
                        guestX = sim.opponentBall.position.x / w,
                        guestY = sim.opponentBall.position.y / h,
                        guestVx = sim.opponentBall.velocity.x / spd,
                        guestVy = sim.opponentBall.velocity.y / spd,
                        hostHp = sim.playerBall.currentHp,
                        guestHp = sim.opponentBall.currentHp,
                        hostUlt = sim.playerBall.ultimateGauge,
                        guestUlt = sim.opponentBall.ultimateGauge
                    )
                }
            } else if (activeTransport == ActiveTransport.FIRESTORE && isFirestoreHost) {
                firestoreSyncAccumulator += dt
                if (firestoreSyncAccumulator >= 0.24f) {
                    firestoreSyncAccumulator = 0f
                    pushFirestoreCombatStateAsync()
                }
            }
        }

        syncHudState(sim, force = false)
    }

    fun onPlayerLaunch(dragVector: Vector2, maxPullPx: Float) {
        if (isBattlePaused) return
        val sim = _battleSim.value ?: return
        if (!sim.isRoundActive) return

        val dragLen = dragVector.length()
        if (dragLen < 16f) return
        val launchDir = Vector2(-dragVector.x, -dragVector.y).normalized()
        val stretchRatio = (dragLen / maxPullPx.coerceAtLeast(80f)).coerceIn(0.15f, 1.0f)
        lastDashStretchRatio = stretchRatio

        when (_turnState.value) {
            TurnState.AIM_OPENING -> {
                sim.startNonstopClash(launchDir, _botDifficulty.value, stretchRatio)
                _turnState.value = TurnState.NONSTOP_CLASH
                soundManager.playRedirectDash(stretchRatio)
                triggerHaptic((18L + (stretchRatio * 24f).toLong()).coerceIn(18L, 45L))
                broadcastLocalMove(launchDir, isOpening = true)
                syncHudState(sim, force = true)
            }

            TurnState.NONSTOP_CLASH -> {
                if (sim.redirectPlayerBall(launchDir, stretchRatio)) {
                    triggerHaptic((16L + (stretchRatio * 22f).toLong()).coerceIn(16L, 40L))
                    broadcastLocalMove(launchDir, isOpening = false)
                    syncHudState(sim, force = true)
                }
            }

            TurnState.KNOCKOUT_SLOWMO -> Unit
        }
    }

    fun onQuickLaunchOpening() {
        if (isBattlePaused) return
        val sim = _battleSim.value ?: return
        if (_turnState.value != TurnState.AIM_OPENING || !sim.isRoundActive) return
        val toEnemy = (sim.opponentBall.position - sim.playerBall.position).rotate(0.28f).normalized()
        sim.startNonstopClash(toEnemy, _botDifficulty.value)
        _turnState.value = TurnState.NONSTOP_CLASH
        soundManager.playRedirectDash()
        triggerHaptic(30L)
        broadcastLocalMove(toEnemy, isOpening = true)
        syncHudState(sim, force = true)
    }

    fun onPlayerTriggerUltimate() {
        if (isBattlePaused) return
        val sim = _battleSim.value ?: return
        if (!sim.isRoundActive) return
        if (sim.triggerUltimate(sim.playerBall)) {
            ultimatesUsedInMatch++
            if (activeTransport == ActiveTransport.LAN) {
                lanManager.sendUltimate()
            } else if (activeTransport == ActiveTransport.FIRESTORE) {
                localUltSeq++
                pushFirestoreCombatStateAsync()
            }
            syncHudState(sim, force = true)
        }
    }

    fun onPlayerTriggerCounter() {
        if (isBattlePaused) return
        val sim = _battleSim.value ?: return
        if (!sim.isRoundActive) return
        if (sim.triggerCounter(sim.playerBall)) {
            countersUsedInMatch++
            if (activeTransport == ActiveTransport.LAN) {
                lanManager.sendCounter()
            } else if (activeTransport == ActiveTransport.FIRESTORE) {
                localCounterSeq++
                pushFirestoreCombatStateAsync()
            }
            syncHudState(sim, force = true)
        }
    }

    private fun broadcastLocalMove(dir: Vector2, isOpening: Boolean) {
        if (activeTransport == ActiveTransport.LAN) {
            if (isOpening) {
                lanManager.sendStartClash(dir.x, dir.y)
            } else {
                lanManager.sendRedirectDash(dir.x, dir.y)
            }
        } else if (activeTransport == ActiveTransport.FIRESTORE) {
            localCmdSeq++
            localLastDirX = dir.x
            localLastDirY = dir.y
            pushFirestoreCombatStateAsync()
        }
    }

    private fun syncHudState(sim: BattleSimulation, force: Boolean) {
        val p = sim.playerBall
        val o = sim.opponentBall
        val pUltInt = if (p.ultimateGauge >= 100f) 100 else ((p.ultimateGauge.toInt() / 2) * 2)
        val oUltInt = if (o.ultimateGauge >= 100f) 100 else ((o.ultimateGauge.toInt() / 2) * 2)

        val pActiveHalf = if (p.isUltimateActive) (p.ultimateTimeRemaining * 2f + 0.9f).toInt().coerceAtLeast(1) else 0
        val oActiveHalf = if (o.isUltimateActive) (o.ultimateTimeRemaining * 2f + 0.9f).toInt().coerceAtLeast(1) else 0

        val pCdHalf = if (p.isUltimateOnCooldown) (p.ultimateCooldownRemaining * 2f + 0.9f).toInt().coerceAtLeast(1) else 0
        val oCdHalf = if (o.isUltimateOnCooldown) (o.ultimateCooldownRemaining * 2f + 0.9f).toInt().coerceAtLeast(1) else 0

        val redirectHalfSecs = (sim.playerRedirectCooldown * 2f + 0.9f).toInt().coerceAtLeast(0)

        val pCounterActiveHalf = if (p.isCounterActive) (p.counterActiveTimer * 2f + 0.9f).toInt().coerceAtLeast(1) else 0
        val pCounterCdHalf = if (p.isCounterOnCooldown) (p.counterCooldownRemaining * 2f + 0.9f).toInt().coerceAtLeast(1) else 0

        val candidate = BattleHudState(
            playerHp = p.currentHp.toInt().coerceAtLeast(0),
            playerMaxHp = p.maxHp.toInt().coerceAtLeast(1),
            opponentHp = o.currentHp.toInt().coerceAtLeast(0),
            opponentMaxHp = o.maxHp.toInt().coerceAtLeast(1),
            playerUltPercent = pUltInt,
            opponentUltPercent = oUltInt,
            isPlayerUltActive = p.isUltimateActive,
            isOpponentUltActive = o.isUltimateActive,
            playerUltActiveHalfSecs = pActiveHalf,
            opponentUltActiveHalfSecs = oActiveHalf,
            playerUltCooldownHalfSecs = pCdHalf,
            opponentUltCooldownHalfSecs = oCdHalf,
            playerUltMaxDurationSec = p.character.ultimateDurationSeconds,
            opponentUltMaxDurationSec = o.character.ultimateDurationSeconds,
            playerUltMaxCooldownSec = p.effectiveUltCooldownSec,
            opponentUltMaxCooldownSec = o.effectiveUltCooldownSec,
            redirectReady = sim.playerRedirectCooldown <= 0.05f,
            redirectHalfSecsLeft = redirectHalfSecs,
            isMultiDashActive = p.isUltimateActive && p.multiDashChargesRemaining > 0,
            multiDashCharges = p.multiDashChargesRemaining,
            canTriggerCounter = p.canActivateCounter,
            isPlayerCounterActive = p.isCounterActive,
            playerCounterActiveHalfSecs = pCounterActiveHalf,
            playerCounterCooldownHalfSecs = pCounterCdHalf,
            playerCounterMaxDurationSec = p.counterDurationSec,
            playerCounterMaxCooldownSec = p.counterCooldownSec,
            counterLabel = p.character.counterArchetype.simpleName,
            autoCounterPercent = p.effectiveAutoCounterPercent,
            isAutoCounterShielded = p.isCounterActive || p.shieldVisualTimer > 0.08f,
            lastImpactBanner = latestBannerText
        )

        if (force || candidate != _hudState.value) {
            _hudState.value = candidate
            syncAudioDirectorWithState()
        }
    }

    private fun handleMatchEnd(isPlayerWinner: Boolean) {
        val sim = _battleSim.value ?: return
        battleAudioDirector.stopBattleMusic()

        val diffBonus = when (_botDifficulty.value) {
            BotDifficulty.EASY -> 0
            BotDifficulty.MEDIUM -> 380
            BotDifficulty.HARD -> 820
        }
        val modeBonus = when (_selectedMode.value) {
            GameMode.ONLINE_MATCH -> 550
            GameMode.MULTIPLAYER -> 420
            GameMode.CUSTOM_MATCH -> 260
            GameMode.PRACTICE_BOT -> 0
        }
        val comboBonus = (maxComboInMatch * 35).coerceAtMost(350)

        val goldEarned = if (isPlayerWinner) {
            1_050 + diffBonus + modeBonus + comboBonus
        } else {
            340 + (comboBonus / 2)
        }
        val gemsEarned = if (isPlayerWinner) 28 else 9
        val expEarned = if (isPlayerWinner) 165 else 60
        val elapsedSec = ((System.currentTimeMillis() - matchStartMillis) / 1000L).toInt().coerceAtLeast(1)

        val stats = MatchStatistics(
            isPlayerWinner = isPlayerWinner,
            playerCharacterId = sim.playerBall.character.id,
            opponentCharacterId = sim.opponentBall.character.id,
            playerCharacterName = sim.playerBall.character.name,
            opponentCharacterName = sim.opponentBall.character.name,
            playerFinalHp = sim.playerBall.currentHp.toInt().coerceAtLeast(0),
            opponentFinalHp = sim.opponentBall.currentHp.toInt().coerceAtLeast(0),
            damageDealt = damageDealtInMatch,
            damageTaken = damageTakenInMatch,
            maxCombo = maxComboInMatch,
            wallBounces = wallBouncesInMatch,
            ultimatesUsed = ultimatesUsedInMatch,
            matchDurationSeconds = elapsedSec,
            goldEarned = goldEarned,
            gemsEarned = gemsEarned,
            expEarned = expEarned
        )
        _matchStats.value = stats

        val updatedQuests = _quests.value.map { q ->
            when (q.id) {
                "q_first_match" -> q.copy(currentProgress = (q.currentProgress + 1).coerceAtMost(q.targetProgress))
                "q_wall_ricochet" -> q.copy(currentProgress = (q.currentProgress + wallBouncesInMatch).coerceAtMost(q.targetProgress))
                "q_awakenings" -> q.copy(currentProgress = (q.currentProgress + ultimatesUsedInMatch).coerceAtMost(q.targetProgress))
                "q_clean_victory" -> if (isPlayerWinner && stats.playerFinalHp >= 120) q.copy(currentProgress = 1) else q
                "q_three_wins" -> if (isPlayerWinner) q.copy(currentProgress = (q.currentProgress + 1).coerceAtMost(q.targetProgress)) else q
                else -> q
            }
        }
        _quests.value = updatedQuests
        persistQuests(updatedQuests)

        refreshDailyQuestsIfNeeded()
        val updatedDaily = _dailyQuests.value.map { dq ->
            when {
                dq.id.startsWith("dq_play_") -> dq.copy(currentProgress = (dq.currentProgress + 1).coerceAtMost(dq.targetProgress))
                dq.id.startsWith("dq_win_") -> if (isPlayerWinner) dq.copy(currentProgress = (dq.currentProgress + 1).coerceAtMost(dq.targetProgress)) else dq
                dq.id.startsWith("dq_counter_") -> dq.copy(currentProgress = (dq.currentProgress + countersUsedInMatch).coerceAtMost(dq.targetProgress))
                dq.id.startsWith("dq_ult_") -> dq.copy(currentProgress = (dq.currentProgress + ultimatesUsedInMatch).coerceAtMost(dq.targetProgress))
                else -> dq
            }
        }
        _dailyQuests.value = updatedDaily
        persistDailyQuests(updatedDaily)

        val profile = _playerProfile.value
        val newGold = profile.gold + goldEarned
        val newGems = profile.gems + gemsEarned
        var newExp = profile.exp + expEarned
        var newLevel = profile.level
        var maxExp = profile.maxExp
        while (newExp >= maxExp) {
            newExp -= maxExp
            newLevel++
            maxExp = 200 + (newLevel - 1) * 100
        }

        val newWins = if (isPlayerWinner) profile.wins + 1 else profile.wins
        val newLosses = if (!isPlayerWinner) profile.losses + 1 else profile.losses
        val ratingDelta = when (_selectedMode.value) {
            GameMode.ONLINE_MATCH -> if (isPlayerWinner) 40 else -18
            GameMode.MULTIPLAYER -> if (isPlayerWinner) 20 else 0
            else -> if (isPlayerWinner) 12 else 0
        }
        val newRating = (profile.rankedRating + ratingDelta).coerceAtLeast(0)
        val newTier = PlayerProfile.computeTier(newRating, newWins + newLosses)

        val updatedProfile = profile.copy(
            gold = newGold,
            gems = newGems,
            exp = newExp,
            maxExp = maxExp,
            level = newLevel,
            wins = newWins,
            losses = newLosses,
            rankedRating = newRating,
            rankTier = newTier
        )
        _playerProfile.value = updatedProfile
        refreshLeaderboardState()

        prefs.edit()
            .putInt("gold", newGold)
            .putInt("gems", newGems)
            .putInt("exp", newExp)
            .putInt("max_exp", maxExp)
            .putInt("level", newLevel)
            .putInt("wins", newWins)
            .putInt("losses", newLosses)
            .putInt("rating", newRating)
            .apply()

        pushCloudSyncAsync()

        viewModelScope.launch {
            delay(950)
            cleanupActiveNetworkSession()
            if (_currentScreen.value == AppScreen.BATTLE_ARENA) {
                _lastNavWasPop.value = false
                _currentScreen.value = AppScreen.RESULT
                syncAudioDirectorWithState()
            }
        }
    }

    private fun mergeUpgradeMaps(
        local: Map<String, CharacterUpgradeState>,
        cloud: Map<String, CharacterUpgradeState>
    ): Map<String, CharacterUpgradeState> {
        val keys = local.keys + cloud.keys
        if (keys.isEmpty()) return emptyMap()
        val out = HashMap<String, CharacterUpgradeState>(keys.size)
        for (k in keys) {
            val a = local[k]
            val b = cloud[k]
            out[k] = when {
                a == null -> b!!
                b == null -> a
                else -> CharacterUpgradeState(
                    healthLevel = max(a.healthLevel, b.healthLevel),
                    powerLevel = max(a.powerLevel, b.powerLevel),
                    cooldownLevel = max(a.cooldownLevel, b.cooldownLevel)
                )
            }
        }
        return out
    }

    private fun persistFullLocalState(
        profile: PlayerProfile,
        unlocked: Set<String>,
        upgrades: Map<String, CharacterUpgradeState>
    ) {
        val editor = prefs.edit()
            .putString("username", profile.username)
            .putInt("gold", profile.gold)
            .putInt("gems", profile.gems)
            .putInt("exp", profile.exp)
            .putInt("max_exp", profile.maxExp)
            .putInt("level", profile.level)
            .putString("selected_char", profile.selectedCharacterId)
            .putInt("wins", profile.wins)
            .putInt("losses", profile.losses)
            .putInt("rating", profile.rankedRating)
            .putStringSet("unlocked_chars", unlocked)
        upgrades.forEach { (id, st) ->
            editor.putInt("upg_hp_$id", st.healthLevel)
            editor.putInt("upg_pwr_$id", st.powerLevel)
            editor.putInt("upg_cd_$id", st.cooldownLevel)
        }
        editor.apply()
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!_engineSettings.value.hapticsEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun loadPlayerProfile(): PlayerProfile {
        val name = prefs.getString("username", "Player") ?: "Player"
        val gold = prefs.getInt("gold", 0)
        val gems = prefs.getInt("gems", 0)
        val exp = prefs.getInt("exp", 0)
        val maxExp = prefs.getInt("max_exp", 200)
        val level = prefs.getInt("level", 1)
        val rawChar = prefs.getString("selected_char", "kakarot_z") ?: "kakarot_z"
        val validChar = CharacterRoster.getById(rawChar).id
        val wins = prefs.getInt("wins", 0)
        val losses = prefs.getInt("losses", 0)
        val rating = prefs.getInt("rating", 0)

        return PlayerProfile(
            username = name,
            level = level,
            exp = exp,
            maxExp = maxExp,
            gold = gold,
            gems = gems,
            selectedCharacterId = validChar,
            rankedRating = rating,
            rankTier = PlayerProfile.computeTier(rating, wins + losses),
            wins = wins,
            losses = losses
        )
    }

    private fun loadUnlockedCharacters(): Set<String> {
        val stored = prefs.getStringSet("unlocked_chars", null)
        if (stored.isNullOrEmpty()) return CharacterRoster.starterIds
        val validIds = CharacterRoster.characters.map { it.id }.toSet()
        val filtered = stored.intersect(validIds)
        return if (filtered.isEmpty()) CharacterRoster.starterIds else (filtered + CharacterRoster.starterIds)
    }

    private fun loadCharacterUpgrades(): Map<String, CharacterUpgradeState> {
        val result = HashMap<String, CharacterUpgradeState>()
        for (char in CharacterRoster.characters) {
            val hp = prefs.getInt("upg_hp_${char.id}", 0).coerceIn(0, CharacterUpgradeState.MAX_LEVEL)
            val pwr = prefs.getInt("upg_pwr_${char.id}", 0).coerceIn(0, CharacterUpgradeState.MAX_LEVEL)
            val cd = prefs.getInt("upg_cd_${char.id}", 0).coerceIn(0, CharacterUpgradeState.MAX_LEVEL)
            if (hp > 0 || pwr > 0 || cd > 0) {
                result[char.id] = CharacterUpgradeState(
                    healthLevel = hp,
                    powerLevel = pwr,
                    cooldownLevel = cd
                )
            }
        }
        return result
    }

    private fun loadQuests(): List<QuestItem> {
        return InitialGameData.defaultQuests.map { seed ->
            val prog = prefs.getInt("quest_prog_${seed.id}", 0)
            val claimed = prefs.getBoolean("quest_claimed_${seed.id}", false)
            seed.copy(currentProgress = prog.coerceIn(0, seed.targetProgress), isClaimed = claimed)
        }
    }

    private fun persistQuests(quests: List<QuestItem>) {
        val editor = prefs.edit()
        quests.forEach { q ->
            editor.putInt("quest_prog_${q.id}", q.currentProgress)
            editor.putBoolean("quest_claimed_${q.id}", q.isClaimed)
        }
        editor.apply()
    }

    private fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    private fun refreshDailyQuestsIfNeeded() {
        val today = currentEpochDay()
        val storedDay = prefs.getLong("daily_quest_epoch_day", -1L)
        if (storedDay != today || _dailyQuests.value.isEmpty()) {
            _dailyQuests.value = loadDailyQuests()
        }
    }

    private fun loadDailyQuests(): List<QuestItem> {
        val today = currentEpochDay()
        val storedDay = prefs.getLong("daily_quest_epoch_day", -1L)
        val seeds = InitialGameData.buildDailyQuests(today)
        if (storedDay != today) {
            prefs.edit().putLong("daily_quest_epoch_day", today).apply()
            return seeds
        }
        return seeds.map { seed ->
            val prog = prefs.getInt("dquest_prog_${seed.id}", 0)
            val claimed = prefs.getBoolean("dquest_claimed_${seed.id}", false)
            seed.copy(currentProgress = prog.coerceIn(0, seed.targetProgress), isClaimed = claimed)
        }
    }

    private fun persistDailyQuests(daily: List<QuestItem>) {
        val editor = prefs.edit().putLong("daily_quest_epoch_day", currentEpochDay())
        daily.forEach { q ->
            editor.putInt("dquest_prog_${q.id}", q.currentProgress)
            editor.putBoolean("dquest_claimed_${q.id}", q.isClaimed)
        }
        editor.apply()
    }

    private fun loadEngineSettings(): EngineSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val renderStr = prefs.getString("render_mode", RenderMode.BALANCED.name) ?: RenderMode.BALANCED.name
        val meshStr = prefs.getString("physics_mesh", PhysicsMesh.DYNAMIC.name) ?: PhysicsMesh.DYNAMIC.name
        val fpsStr = prefs.getString("target_fps", TargetFPS.FPS_60.name) ?: TargetFPS.FPS_60.name
        val music = prefs.getBoolean("music_enabled", true)
        val sfx = prefs.getBoolean("sfx_enabled", true)
        val voiceLines = prefs.getBoolean("voice_lines_enabled", true)
        val haptics = prefs.getBoolean("haptics_enabled", true)
        val shake = prefs.getBoolean("screen_shake", true)

        val theme = AppThemeMode.entries.firstOrNull { it.name == themeStr } ?: AppThemeMode.DARK
        val render = RenderMode.entries.firstOrNull { it.name == renderStr } ?: RenderMode.BALANCED
        val mesh = PhysicsMesh.entries.firstOrNull { it.name == meshStr } ?: PhysicsMesh.DYNAMIC
        val fps = TargetFPS.entries.firstOrNull { it.name == fpsStr } ?: TargetFPS.FPS_60

        return EngineSettings(
            themeMode = theme,
            renderMode = render,
            physicsMesh = mesh,
            targetFps = fps,
            musicEnabled = music,
            sfxEnabled = sfx,
            voiceLinesEnabled = voiceLines,
            hapticsEnabled = haptics,
            bloomPostFx = render.bloomGlow,
            screenShake = shake
        )
    }

    private fun persistEngineSettings(settings: EngineSettings) {
        prefs.edit()
            .putString("theme_mode", settings.themeMode.name)
            .putString("render_mode", settings.renderMode.name)
            .putString("physics_mesh", settings.physicsMesh.name)
            .putString("target_fps", settings.targetFps.name)
            .putBoolean("music_enabled", settings.musicEnabled)
            .putBoolean("sfx_enabled", settings.sfxEnabled)
            .putBoolean("voice_lines_enabled", settings.voiceLinesEnabled)
            .putBoolean("haptics_enabled", settings.hapticsEnabled)
            .putBoolean("screen_shake", settings.screenShake)
            .apply()
    }
}
