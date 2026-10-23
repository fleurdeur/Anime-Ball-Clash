package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppScreen
import com.example.model.BattleSimulation
import com.example.model.BotDifficulty
import com.example.model.CharacterData
import com.example.model.CharacterRoster
import com.example.model.CombatBall
import com.example.model.EngineSettings
import com.example.model.GameMode
import com.example.model.InitialGameData
import com.example.model.LeaderboardEntry
import com.example.model.MatchStatistics
import com.example.model.PlayerProfile
import com.example.model.QuestItem
import com.example.model.TurnState
import com.example.model.Vector2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class BattleHudState(
    val playerHp: Int = 100,
    val playerDelayedHp: Float = 100f,
    val opponentHp: Int = 100,
    val opponentDelayedHp: Float = 100f,
    val playerUltGauge: Float = 25f,
    val opponentUltGauge: Float = 25f,
    val isPlayerUltActive: Boolean = false,
    val playerUltRemainingSec: Float = 0f,
    val isOpponentUltActive: Boolean = false,
    val playerStatusLabel: String = "READY",
    val opponentStatusLabel: String = "READY",
    val lastImpactBanner: String = ""
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("anime_ball_clash_prefs", Context.MODE_PRIVATE)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _currentScreen = MutableStateFlow(AppScreen.LOADING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _engineSettings = MutableStateFlow(EngineSettings())
    val engineSettings: StateFlow<EngineSettings> = _engineSettings.asStateFlow()

    private val _playerProfile = MutableStateFlow(loadPlayerProfile())
    val playerProfile: StateFlow<PlayerProfile> = _playerProfile.asStateFlow()

    private val _unlockedCharacterIds = MutableStateFlow(loadUnlockedCharacters())
    val unlockedCharacterIds: StateFlow<Set<String>> = _unlockedCharacterIds.asStateFlow()

    private val _quests = MutableStateFlow(InitialGameData.defaultQuests)
    val quests: StateFlow<List<QuestItem>> = _quests.asStateFlow()

    private val _leaderboard = MutableStateFlow(InitialGameData.leaderboardEntries)
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.PRACTICE_BOT)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _botDifficulty = MutableStateFlow(BotDifficulty.MEDIUM)
    val botDifficulty: StateFlow<BotDifficulty> = _botDifficulty.asStateFlow()

    private val _matchmakingCountdown = MutableStateFlow(3)
    val matchmakingCountdown: StateFlow<Int> = _matchmakingCountdown.asStateFlow()

    private val _opponentName = MutableStateFlow("Bot Demon")
    val opponentName: StateFlow<String> = _opponentName.asStateFlow()

    private val _opponentCharacter = MutableStateFlow(CharacterRoster.getById("demon_slash"))
    val opponentCharacter: StateFlow<CharacterData> = _opponentCharacter.asStateFlow()

    private val _battleSim = MutableStateFlow<BattleSimulation?>(null)
    val battleSim: StateFlow<BattleSimulation?> = _battleSim.asStateFlow()

    private val _turnState = MutableStateFlow(TurnState.PLAYER_AIMING)
    val turnState: StateFlow<TurnState> = _turnState.asStateFlow()

    private val _hudState = MutableStateFlow(BattleHudState())
    val hudState: StateFlow<BattleHudState> = _hudState.asStateFlow()

    private val _matchStats = MutableStateFlow<MatchStatistics?>(null)
    val matchStats: StateFlow<MatchStatistics?> = _matchStats.asStateFlow()

    private val _matchTimerSeconds = MutableStateFlow(120)
    val matchTimerSeconds: StateFlow<Int> = _matchTimerSeconds.asStateFlow()

    private var damageDealtInMatch = 0
    private var damageTakenInMatch = 0
    private var maxComboInMatch = 0
    private var wallBouncesInMatch = 0
    private var ultimatesUsedInMatch = 0

    private var phaseTimer = 0f
    private var aiPlannedDir = Vector2(0f, 1f)
    private var aiUltTriggeredThisTurn = false

    private var matchmakingJob: Job? = null
    private var matchTimerJob: Job? = null

    init {
        viewModelScope.launch {
            delay(2200)
            if (_currentScreen.value == AppScreen.LOADING) {
                _currentScreen.value = AppScreen.HOME_HUB
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.BATTLE_ARENA) {
            matchTimerJob?.cancel()
        }
        if (screen != AppScreen.MATCHMAKING) {
            matchmakingJob?.cancel()
        }
        _currentScreen.value = screen
    }

    fun updateEngineSettings(newSettings: EngineSettings) {
        _engineSettings.value = newSettings
        _battleSim.value?.engineSettings = newSettings
    }

    fun updateUsername(newName: String) {
        val trimmed = newName.take(16).trim()
        if (trimmed.isNotEmpty()) {
            val updated = _playerProfile.value.copy(username = trimmed)
            _playerProfile.value = updated
            prefs.edit().putString("username", trimmed).apply()
        }
    }

    fun selectCharacter(charId: String) {
        if (!_unlockedCharacterIds.value.contains(charId)) return
        val updated = _playerProfile.value.copy(selectedCharacterId = charId)
        _playerProfile.value = updated
        prefs.edit().putString("selected_char", charId).apply()
    }

    fun cycleShowcaseCharacter(delta: Int) {
        val unlockedList = CharacterRoster.characters.filter { _unlockedCharacterIds.value.contains(it.id) }
        if (unlockedList.isEmpty()) return
        val currentIdx = unlockedList.indexOfFirst { it.id == _playerProfile.value.selectedCharacterId }.coerceAtLeast(0)
        val nextIdx = (currentIdx + delta + unlockedList.size) % unlockedList.size
        selectCharacter(unlockedList[nextIdx].id)
        triggerHaptic(18L)
    }

    fun unlockCharacter(character: CharacterData): Boolean {
        val profile = _playerProfile.value
        if (profile.gold >= character.unlockCostGold && profile.gems >= character.unlockCostGems) {
            val updatedGold = profile.gold - character.unlockCostGold
            val updatedGems = profile.gems - character.unlockCostGems
            _playerProfile.value = profile.copy(gold = updatedGold, gems = updatedGems)

            val currentSet = _unlockedCharacterIds.value.toMutableSet()
            currentSet.add(character.id)
            _unlockedCharacterIds.value = currentSet

            prefs.edit()
                .putInt("gold", updatedGold)
                .putInt("gems", updatedGems)
                .putStringSet("unlocked_chars", currentSet)
                .apply()
            triggerHaptic(50L)
            return true
        }
        return false
    }

    fun claimQuest(questId: String) {
        val list = _quests.value.toMutableList()
        val index = list.indexOfFirst { it.id == questId }
        if (index != -1 && !list[index].isClaimed && list[index].currentProgress >= list[index].targetProgress) {
            val q = list[index]
            list[index] = q.copy(isClaimed = true)
            _quests.value = list

            val profile = _playerProfile.value
            val updated = profile.copy(
                gold = profile.gold + q.rewardGold,
                gems = profile.gems + q.rewardGems
            )
            _playerProfile.value = updated
            prefs.edit().putInt("gold", updated.gold).putInt("gems", updated.gems).apply()
            triggerHaptic(40L)
        }
    }

    fun startMatchmaking(mode: GameMode, difficulty: BotDifficulty = _botDifficulty.value) {
        _selectedMode.value = mode
        _botDifficulty.value = difficulty
        _currentScreen.value = AppScreen.MATCHMAKING

        val playerCharId = _playerProfile.value.selectedCharacterId
        val candidates = CharacterRoster.characters.filter { it.id != playerCharId }
        val oppChar = candidates.randomOrNull() ?: CharacterRoster.getById("demon_slash")
        _opponentCharacter.value = oppChar

        _opponentName.value = when (mode) {
            GameMode.PRACTICE_BOT -> "${difficulty.shortLabel} • ${oppChar.name.substringBefore(" ")}"
            GameMode.ONLINE_MATCH -> listOf("KuroBlade_JP", "RaidenZero", "SoraVortex", "Akira_99", "ZenithStrike").random()
            GameMode.FRIENDS_LOBBY -> "DuelRival_01"
        }

        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            _matchmakingCountdown.value = 3
            delay(550)
            _matchmakingCountdown.value = 2
            delay(550)
            _matchmakingCountdown.value = 1
            delay(550)
            setupBattle()
            _currentScreen.value = AppScreen.BATTLE_ARENA
        }
    }

    fun startInstantBattle(mode: GameMode, difficulty: BotDifficulty = _botDifficulty.value) {
        _selectedMode.value = mode
        _botDifficulty.value = difficulty
        val playerCharId = _playerProfile.value.selectedCharacterId
        val oppChar = CharacterRoster.characters.filter { it.id != playerCharId }.random()
        _opponentCharacter.value = oppChar
        _opponentName.value = "${difficulty.shortLabel} • ${oppChar.name.substringBefore(" ")}"
        setupBattle()
        _currentScreen.value = AppScreen.BATTLE_ARENA
    }

    private fun setupBattle() {
        damageDealtInMatch = 0
        damageTakenInMatch = 0
        maxComboInMatch = 0
        wallBouncesInMatch = 0
        ultimatesUsedInMatch = 0
        phaseTimer = 0f
        _matchTimerSeconds.value = 120

        val playerChar = CharacterRoster.getById(_playerProfile.value.selectedCharacterId)
        val oppChar = _opponentCharacter.value

        val pBall = CombatBall(
            id = "player",
            name = _playerProfile.value.username,
            position = Vector2(500f, 1100f),
            velocity = Vector2.Zero,
            radius = 56f,
            character = playerChar
        )

        val oBall = CombatBall(
            id = "opponent",
            name = _opponentName.value,
            position = Vector2(500f, 340f),
            velocity = Vector2.Zero,
            radius = 56f,
            character = oppChar
        )

        var latestBanner = ""

        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = _engineSettings.value,
            onCollisionImpact = { isPlayerAttacker, damage, isCrit, passiveLabel ->
                triggerHaptic(if (isCrit) 75L else 35L)
                if (isPlayerAttacker) {
                    damageDealtInMatch += damage
                } else {
                    damageTakenInMatch += damage
                }
                latestBanner = "$passiveLabel • -$damage HP"
                _hudState.value = _hudState.value.copy(lastImpactBanner = latestBanner)
            },
            onWallBounce = { ball ->
                wallBouncesInMatch++
                if (ball.wallBounceCombo > maxComboInMatch) {
                    maxComboInMatch = ball.wallBounceCombo
                }
                triggerHaptic(12L)
            },
            onKnockout = { isPlayerWinner ->
                _turnState.value = TurnState.KNOCKOUT_SLOWMO
                triggerHaptic(160L)
                handleMatchEnd(isPlayerWinner)
            }
        )

        _battleSim.value = sim
        _turnState.value = TurnState.PLAYER_AIMING
        syncHudState(sim)
        startMatchTimer()
    }

    private fun startMatchTimer() {
        matchTimerJob?.cancel()
        matchTimerJob = viewModelScope.launch {
            while (_matchTimerSeconds.value > 0 && _battleSim.value?.isRoundActive == true) {
                delay(1000)
                _matchTimerSeconds.value -= 1
            }
            val sim = _battleSim.value
            if (_matchTimerSeconds.value <= 0 && sim != null && sim.isRoundActive) {
                sim.isRoundActive = false
                handleMatchEnd(sim.playerBall.currentHp >= sim.opponentBall.currentHp)
            }
        }
    }

    /**
     * Driven directly by Compose VSYNC frame clock in BattleArenaScreen for butter-smooth 60-120 FPS physics & deterministic AI turns.
     */
    fun stepBattleFrame(dt: Float) {
        val sim = _battleSim.value ?: return
        if (!sim.isArenaInitialized) return

        sim.update(dt)

        if (sim.isRoundActive) {
            when (_turnState.value) {
                TurnState.PLAYER_AIMING -> {
                    sim.isAiAimingVisible = false
                    // Decay any lingering micro-stun while waiting
                    if (sim.playerBall.stunTimeRemaining > 0f) {
                        sim.playerBall.stunTimeRemaining = (sim.playerBall.stunTimeRemaining - dt).coerceAtLeast(0f)
                    }
                }

                TurnState.PLAYER_MOVING -> {
                    phaseTimer += dt
                    if (phaseTimer >= 0.20f && (sim.areBallsAtRest() || phaseTimer >= 4.2f)) {
                        sim.haltBalls()
                        phaseTimer = 0f
                        // Check if Unlimited Void froze the opponent for a bonus player turn
                        if (sim.opponentBall.stunTimeRemaining > 0.6f) {
                            sim.opponentBall.stunTimeRemaining = 0f
                            sim.spawnFloatingText(
                                "VOID FREEZE • BONUS TURN!",
                                sim.playerBall.position.x,
                                sim.playerBall.position.y - sim.playerBall.radius * 1.4f,
                                androidx.compose.ui.graphics.Color(0xFF00F0FF),
                                isCrit = true
                            )
                            _turnState.value = TurnState.PLAYER_AIMING
                        } else {
                            beginAiAiming(sim)
                        }
                    }
                }

                TurnState.AI_AIMING -> {
                    phaseTimer += dt
                    val telegraphDuration = 0.68f
                    sim.isAiAimingVisible = true
                    sim.aiAimCharge = (phaseTimer / telegraphDuration).coerceIn(0f, 1f)

                    // AI triggers its Ultimate mid-aim if ready!
                    if (!aiUltTriggeredThisTurn && phaseTimer >= 0.24f &&
                        sim.opponentBall.ultimateGauge >= 100f && !sim.opponentBall.isUltimateActive
                    ) {
                        aiUltTriggeredThisTurn = true
                        sim.triggerUltimate(sim.opponentBall)
                        triggerHaptic(70L)
                    }

                    if (phaseTimer >= telegraphDuration) {
                        sim.opponentBall.stunTimeRemaining = 0f
                        val power = (0.76f + Random.nextFloat() * 0.24f) * _botDifficulty.value.power
                        sim.launchOpponent(aiPlannedDir, power)
                        triggerHaptic(28L)
                        phaseTimer = 0f
                        _turnState.value = TurnState.AI_MOVING
                    }
                }

                TurnState.AI_MOVING -> {
                    phaseTimer += dt
                    if (phaseTimer >= 0.20f && (sim.areBallsAtRest() || phaseTimer >= 4.2f)) {
                        sim.haltBalls()
                        phaseTimer = 0f
                        if (sim.playerBall.stunTimeRemaining > 0.6f) {
                            sim.playerBall.stunTimeRemaining = 0f
                            sim.spawnFloatingText(
                                "STUNNED • AI BONUS SHOT!",
                                sim.opponentBall.position.x,
                                sim.opponentBall.position.y + sim.opponentBall.radius * 1.4f,
                                androidx.compose.ui.graphics.Color(0xFFFF1E42),
                                isCrit = true
                            )
                            beginAiAiming(sim)
                        } else {
                            sim.playerBall.stunTimeRemaining = 0f
                            _turnState.value = TurnState.PLAYER_AIMING
                        }
                    }
                }

                TurnState.KNOCKOUT_SLOWMO -> Unit
            }
        }

        syncHudState(sim)
    }

    private fun beginAiAiming(sim: BattleSimulation) {
        aiPlannedDir = sim.computeAiAimDirection(_botDifficulty.value)
        aiUltTriggeredThisTurn = false
        sim.aiAimDirection = aiPlannedDir
        sim.aiAimCharge = 0f
        sim.isAiAimingVisible = true
        phaseTimer = 0f
        _turnState.value = TurnState.AI_AIMING
    }

    fun onPlayerLaunch(dragVector: Vector2, maxPullPx: Float) {
        val sim = _battleSim.value ?: return
        if (_turnState.value != TurnState.PLAYER_AIMING || !sim.isRoundActive) return

        val dragLen = dragVector.length()
        if (dragLen < 16f) return

        val launchDir = Vector2(-dragVector.x, -dragVector.y).normalized()
        val powerRatio = (dragLen / maxPullPx.coerceAtLeast(100f)).coerceIn(0.24f, 1.0f)

        sim.playerBall.stunTimeRemaining = 0f
        sim.launchPlayer(launchDir, powerRatio)
        phaseTimer = 0f
        _turnState.value = TurnState.PLAYER_MOVING
        triggerHaptic(32L)
    }

    fun onPlayerTriggerUltimate() {
        val sim = _battleSim.value ?: return
        if (!sim.isRoundActive) return
        if (sim.playerBall.ultimateGauge >= 100f && !sim.playerBall.isUltimateActive) {
            sim.triggerUltimate(sim.playerBall)
            ultimatesUsedInMatch++
            triggerHaptic(95L)
            syncHudState(sim)
        }
    }

    private fun syncHudState(sim: BattleSimulation) {
        val p = sim.playerBall
        val o = sim.opponentBall
        val pStatus = when {
            p.isUltimateActive -> "AWAKENED (${String.format("%.1fs", p.ultimateTimeRemaining)})"
            p.isStunned -> "STUNNED"
            p.bleedTicksRemaining > 0 -> "BLEED x${p.bleedTicksRemaining}"
            else -> "NORMAL"
        }
        val oStatus = when {
            o.isUltimateActive -> "AWAKENED (${String.format("%.1fs", o.ultimateTimeRemaining)})"
            o.isStunned -> "STUNNED"
            o.bleedTicksRemaining > 0 -> "BLEED x${o.bleedTicksRemaining}"
            else -> "NORMAL"
        }

        _hudState.value = _hudState.value.copy(
            playerHp = p.currentHp.toInt().coerceAtLeast(0),
            playerDelayedHp = p.delayedHp,
            opponentHp = o.currentHp.toInt().coerceAtLeast(0),
            opponentDelayedHp = o.delayedHp,
            playerUltGauge = p.ultimateGauge,
            opponentUltGauge = o.ultimateGauge,
            isPlayerUltActive = p.isUltimateActive,
            playerUltRemainingSec = p.ultimateTimeRemaining,
            isOpponentUltActive = o.isUltimateActive,
            playerStatusLabel = pStatus,
            opponentStatusLabel = oStatus
        )
    }

    private fun handleMatchEnd(isPlayerWinner: Boolean) {
        val sim = _battleSim.value ?: return
        matchTimerJob?.cancel()

        val goldEarned = if (isPlayerWinner) 400 else 120
        val gemsEarned = if (isPlayerWinner) 30 else 8
        val expEarned = if (isPlayerWinner) 220 else 75

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
            matchDurationSeconds = (120 - _matchTimerSeconds.value).coerceAtLeast(1),
            goldEarned = goldEarned,
            gemsEarned = gemsEarned,
            expEarned = expEarned
        )
        _matchStats.value = stats

        // Advance quests
        val updatedQuests = _quests.value.map { q ->
            when (q.id) {
                "q1" -> q.copy(currentProgress = (q.currentProgress + 1).coerceAtMost(q.targetProgress))
                "q2" -> q.copy(currentProgress = (q.currentProgress + wallBouncesInMatch).coerceAtMost(q.targetProgress))
                "q3" -> q.copy(currentProgress = (q.currentProgress + ultimatesUsedInMatch).coerceAtMost(q.targetProgress))
                "q4" -> if (isPlayerWinner && stats.playerFinalHp >= 40) q.copy(currentProgress = 1) else q
                else -> q
            }
        }
        _quests.value = updatedQuests

        val profile = _playerProfile.value
        val newGold = profile.gold + goldEarned
        val newGems = profile.gems + gemsEarned
        var newExp = profile.exp + expEarned
        var newLevel = profile.level
        val newWins = if (isPlayerWinner) profile.wins + 1 else profile.wins
        val newLosses = if (!isPlayerWinner) profile.losses + 1 else profile.losses
        val newRating = if (isPlayerWinner) profile.rankedRating + 28 else (profile.rankedRating - 14).coerceAtLeast(0)

        if (newExp >= profile.maxExp) {
            newLevel++
            newExp -= profile.maxExp
        }

        val updatedProfile = profile.copy(
            gold = newGold,
            gems = newGems,
            exp = newExp,
            level = newLevel,
            wins = newWins,
            losses = newLosses,
            rankedRating = newRating
        )
        _playerProfile.value = updatedProfile

        prefs.edit()
            .putInt("gold", newGold)
            .putInt("gems", newGems)
            .putInt("exp", newExp)
            .putInt("level", newLevel)
            .putInt("wins", newWins)
            .putInt("losses", newLosses)
            .putInt("rating", newRating)
            .apply()

        viewModelScope.launch {
            delay(1100)
            if (_currentScreen.value == AppScreen.BATTLE_ARENA) {
                _currentScreen.value = AppScreen.RESULT
            }
        }
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
        } catch (_: Exception) {}
    }

    private fun loadPlayerProfile(): PlayerProfile {
        val name = prefs.getString("username", "ShadowSlayer99") ?: "ShadowSlayer99"
        val gold = prefs.getInt("gold", 14250)
        val gems = prefs.getInt("gems", 320)
        val exp = prefs.getInt("exp", 3450)
        val level = prefs.getInt("level", 42)
        val selChar = prefs.getString("selected_char", "saiyan_spirit") ?: "saiyan_spirit"
        val wins = prefs.getInt("wins", 89)
        val losses = prefs.getInt("losses", 23)
        val rating = prefs.getInt("rating", 1840)

        return PlayerProfile(
            username = name,
            level = level,
            exp = exp,
            maxExp = 5000,
            gold = gold,
            gems = gems,
            selectedCharacterId = selChar,
            rankedRating = rating,
            wins = wins,
            losses = losses
        )
    }

    private fun loadUnlockedCharacters(): Set<String> {
        val stored = prefs.getStringSet("unlocked_chars", null)
        return stored ?: setOf("saiyan_spirit", "demon_slash", "six_glow_magic", "lightning_ninja")
    }
}
