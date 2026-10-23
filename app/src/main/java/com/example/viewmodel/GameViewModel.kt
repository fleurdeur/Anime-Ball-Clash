package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppScreen
import com.example.model.AppThemeMode
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
import com.example.model.PhysicsMesh
import com.example.model.PlayerProfile
import com.example.model.QuestItem
import com.example.model.RenderMode
import com.example.model.TargetFPS
import com.example.model.TurnState
import com.example.model.Vector2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BattleHudState(
    val playerHp: Int = 100,
    val opponentHp: Int = 100,
    val playerUltPercent: Int = 0,
    val opponentUltPercent: Int = 0,
    val isPlayerUltActive: Boolean = false,
    val isOpponentUltActive: Boolean = false,
    val redirectReady: Boolean = true,
    val redirectHalfSecsLeft: Int = 0,
    val lastImpactBanner: String = ""
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    // Fresh clean-account preferences key: starts strictly at 0 progress on first install
    private val prefs = application.getSharedPreferences("anime_ball_clash_clean_v1", Context.MODE_PRIVATE)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _currentScreen = MutableStateFlow(AppScreen.LOADING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _engineSettings = MutableStateFlow(loadEngineSettings())
    val engineSettings: StateFlow<EngineSettings> = _engineSettings.asStateFlow()

    private val _playerProfile = MutableStateFlow(loadPlayerProfile())
    val playerProfile: StateFlow<PlayerProfile> = _playerProfile.asStateFlow()

    private val _unlockedCharacterIds = MutableStateFlow(loadUnlockedCharacters())
    val unlockedCharacterIds: StateFlow<Set<String>> = _unlockedCharacterIds.asStateFlow()

    private val _quests = MutableStateFlow(loadQuests())
    val quests: StateFlow<List<QuestItem>> = _quests.asStateFlow()

    private val _leaderboard = MutableStateFlow(InitialGameData.buildLeaderboard(_playerProfile.value))
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.PRACTICE_BOT)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _botDifficulty = MutableStateFlow(BotDifficulty.MEDIUM)
    val botDifficulty: StateFlow<BotDifficulty> = _botDifficulty.asStateFlow()

    private val _matchmakingCountdown = MutableStateFlow(2)
    val matchmakingCountdown: StateFlow<Int> = _matchmakingCountdown.asStateFlow()

    private val _opponentName = MutableStateFlow("Rival Kamado")
    val opponentName: StateFlow<String> = _opponentName.asStateFlow()

    private val _opponentCharacter = MutableStateFlow(CharacterRoster.getById("kamado_sun"))
    val opponentCharacter: StateFlow<CharacterData> = _opponentCharacter.asStateFlow()

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
    private var matchStartMillis = 0L
    private var latestBannerText = ""

    private var matchmakingJob: Job? = null

    init {
        viewModelScope.launch {
            delay(850)
            if (_currentScreen.value == AppScreen.LOADING) {
                _currentScreen.value = AppScreen.HOME_HUB
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.MATCHMAKING) {
            matchmakingJob?.cancel()
        }
        _currentScreen.value = screen
    }

    fun setBotDifficulty(difficulty: BotDifficulty) {
        _botDifficulty.value = difficulty
    }

    fun updateEngineSettings(newSettings: EngineSettings) {
        _engineSettings.value = newSettings
        _battleSim.value?.engineSettings = newSettings
        persistEngineSettings(newSettings)
    }

    fun setThemeMode(mode: AppThemeMode) {
        val updated = _engineSettings.value.copy(themeMode = mode)
        updateEngineSettings(updated)
        triggerHaptic(14L)
    }

    fun updateUsername(newName: String) {
        val trimmed = newName.take(16).trim()
        if (trimmed.isNotEmpty()) {
            val updated = _playerProfile.value.copy(username = trimmed)
            _playerProfile.value = updated
            _leaderboard.value = InitialGameData.buildLeaderboard(updated)
            prefs.edit().putString("username", trimmed).apply()
        }
    }

    fun resetAccountToZero() {
        val savedSettings = _engineSettings.value
        prefs.edit().clear().apply()
        persistEngineSettings(savedSettings)
        val freshProfile = PlayerProfile()
        _playerProfile.value = freshProfile
        _unlockedCharacterIds.value = CharacterRoster.starterIds
        _quests.value = InitialGameData.defaultQuests
        _leaderboard.value = InitialGameData.buildLeaderboard(freshProfile)
        triggerHaptic(35L)
    }

    fun selectCharacter(charId: String) {
        if (!_unlockedCharacterIds.value.contains(charId)) return
        val updated = _playerProfile.value.copy(selectedCharacterId = charId)
        _playerProfile.value = updated
        _leaderboard.value = InitialGameData.buildLeaderboard(updated)
        prefs.edit().putString("selected_char", charId).apply()
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
            _leaderboard.value = InitialGameData.buildLeaderboard(updatedProfile)

            val currentSet = _unlockedCharacterIds.value.toMutableSet()
            currentSet.add(character.id)
            _unlockedCharacterIds.value = currentSet

            prefs.edit()
                .putInt("gold", updatedGold)
                .putInt("gems", updatedGems)
                .putString("selected_char", character.id)
                .putStringSet("unlocked_chars", currentSet)
                .apply()
            triggerHaptic(45L)
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
            persistQuests(list)

            val profile = _playerProfile.value
            val updated = profile.copy(
                gold = profile.gold + q.rewardGold,
                gems = profile.gems + q.rewardGems
            )
            _playerProfile.value = updated
            prefs.edit().putInt("gold", updated.gold).putInt("gems", updated.gems).apply()
            triggerHaptic(35L)
        }
    }

    fun startMatchmaking(mode: GameMode, difficulty: BotDifficulty = _botDifficulty.value) {
        _selectedMode.value = mode
        _botDifficulty.value = difficulty
        _currentScreen.value = AppScreen.MATCHMAKING

        val playerCharId = _playerProfile.value.selectedCharacterId
        val oppChar = CharacterRoster.characters.filter { it.id != playerCharId }.random()
        _opponentCharacter.value = oppChar

        _opponentName.value = when (mode) {
            GameMode.PRACTICE_BOT -> "${difficulty.shortLabel} ${oppChar.name}"
            GameMode.ONLINE_MATCH -> listOf("KuroBlade", "RaidenZero", "SoraVortex", "Akira_JP", "ZenithStrike").random()
            GameMode.FRIENDS_LOBBY -> "Arena Rival"
        }

        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            _matchmakingCountdown.value = 2
            delay(480)
            _matchmakingCountdown.value = 1
            delay(480)
            setupBattle()
            _currentScreen.value = AppScreen.BATTLE_ARENA
        }
    }

    private fun setupBattle() {
        damageDealtInMatch = 0
        damageTakenInMatch = 0
        maxComboInMatch = 0
        wallBouncesInMatch = 0
        ultimatesUsedInMatch = 0
        latestBannerText = ""
        matchStartMillis = System.currentTimeMillis()

        val playerChar = CharacterRoster.getById(_playerProfile.value.selectedCharacterId)
        val oppChar = _opponentCharacter.value

        val pBall = CombatBall(
            id = "player",
            name = _playerProfile.value.username,
            position = Vector2(500f, 1100f),
            velocity = Vector2.Zero,
            radius = 52f,
            character = playerChar
        )

        val oBall = CombatBall(
            id = "opponent",
            name = _opponentName.value,
            position = Vector2(500f, 340f),
            velocity = Vector2.Zero,
            radius = 52f,
            character = oppChar
        )

        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = _engineSettings.value,
            onCollisionImpact = { isPlayerAttacker, damage, isCrit, passiveLabel ->
                triggerHaptic(if (isCrit) 55L else 25L)
                if (isPlayerAttacker) {
                    damageDealtInMatch += damage
                } else {
                    damageTakenInMatch += damage
                }
                latestBannerText = "$passiveLabel • -$damage HP"
            },
            onWallBounce = { ball ->
                wallBouncesInMatch++
                if (ball.wallBounceCombo > maxComboInMatch) {
                    maxComboInMatch = ball.wallBounceCombo
                }
            },
            onKnockout = { isPlayerWinner ->
                _turnState.value = TurnState.KNOCKOUT_SLOWMO
                triggerHaptic(140L)
                handleMatchEnd(isPlayerWinner)
            }
        )

        _battleSim.value = sim
        _turnState.value = TurnState.AIM_OPENING
        syncHudState(sim, force = true)
    }

    /**
     * Advances simulation at VSYNC rate without causing root UI recompositions.
     */
    fun stepBattleFrame(dt: Float) {
        val sim = _battleSim.value ?: return
        if (!sim.isArenaInitialized) return

        sim.update(dt)

        if (sim.isRoundActive && _turnState.value == TurnState.NONSTOP_CLASH) {
            sim.performAiTacticalStep(dt, _botDifficulty.value)
        }

        syncHudState(sim, force = false)
    }

    /**
     * Handles both the initial match-opening launch AND mid-flight tactical redirects.
     */
    fun onPlayerLaunch(dragVector: Vector2, maxPullPx: Float) {
        val sim = _battleSim.value ?: return
        if (!sim.isRoundActive) return

        val dragLen = dragVector.length()
        if (dragLen < 16f) return
        val launchDir = Vector2(-dragVector.x, -dragVector.y).normalized()

        when (_turnState.value) {
            TurnState.AIM_OPENING -> {
                sim.startNonstopClash(launchDir, _botDifficulty.value)
                _turnState.value = TurnState.NONSTOP_CLASH
                triggerHaptic(30L)
                syncHudState(sim, force = true)
            }
            TurnState.NONSTOP_CLASH -> {
                if (sim.redirectPlayerBall(launchDir)) {
                    triggerHaptic(22L)
                    syncHudState(sim, force = true)
                }
            }
            TurnState.KNOCKOUT_SLOWMO -> Unit
        }
    }

    fun onQuickLaunchOpening() {
        val sim = _battleSim.value ?: return
        if (_turnState.value != TurnState.AIM_OPENING || !sim.isRoundActive) return
        val toEnemy = (sim.opponentBall.position - sim.playerBall.position).rotate(0.28f).normalized()
        sim.startNonstopClash(toEnemy, _botDifficulty.value)
        _turnState.value = TurnState.NONSTOP_CLASH
        triggerHaptic(30L)
        syncHudState(sim, force = true)
    }

    fun onPlayerTriggerUltimate() {
        val sim = _battleSim.value ?: return
        if (!sim.isRoundActive) return
        if (sim.playerBall.ultimateGauge >= 100f && !sim.playerBall.isUltimateActive) {
            sim.triggerUltimate(sim.playerBall)
            ultimatesUsedInMatch++
            triggerHaptic(80L)
            syncHudState(sim, force = true)
        }
    }

    private fun syncHudState(sim: BattleSimulation, force: Boolean) {
        val p = sim.playerBall
        val o = sim.opponentBall
        val pUltInt = if (p.ultimateGauge >= 100f) 100 else ((p.ultimateGauge.toInt() / 2) * 2)
        val oUltInt = if (o.ultimateGauge >= 100f) 100 else ((o.ultimateGauge.toInt() / 5) * 5)
        val redirectHalfSecs = (sim.playerRedirectCooldown * 2f + 0.9f).toInt().coerceAtLeast(0)

        val candidate = BattleHudState(
            playerHp = p.currentHp.toInt().coerceAtLeast(0),
            opponentHp = o.currentHp.toInt().coerceAtLeast(0),
            playerUltPercent = pUltInt,
            opponentUltPercent = oUltInt,
            isPlayerUltActive = p.isUltimateActive,
            isOpponentUltActive = o.isUltimateActive,
            redirectReady = sim.playerRedirectCooldown <= 0.05f,
            redirectHalfSecsLeft = redirectHalfSecs,
            lastImpactBanner = latestBannerText
        )

        if (force || candidate != _hudState.value) {
            _hudState.value = candidate
        }
    }

    private fun handleMatchEnd(isPlayerWinner: Boolean) {
        val sim = _battleSim.value ?: return

        val goldEarned = if (isPlayerWinner) 220 else 80
        val gemsEarned = if (isPlayerWinner) 15 else 5
        val expEarned = if (isPlayerWinner) 120 else 45
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
                "q_clean_victory" -> if (isPlayerWinner && stats.playerFinalHp >= 40) q.copy(currentProgress = 1) else q
                "q_three_wins" -> if (isPlayerWinner) q.copy(currentProgress = (q.currentProgress + 1).coerceAtMost(q.targetProgress)) else q
                else -> q
            }
        }
        _quests.value = updatedQuests
        persistQuests(updatedQuests)

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
        val ratingDelta = if (_selectedMode.value == GameMode.ONLINE_MATCH) {
            if (isPlayerWinner) 35 else -15
        } else {
            if (isPlayerWinner) 15 else 0
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
        _leaderboard.value = InitialGameData.buildLeaderboard(updatedProfile)

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

        viewModelScope.launch {
            delay(950)
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

    private fun loadEngineSettings(): EngineSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val renderStr = prefs.getString("render_mode", RenderMode.BALANCED.name) ?: RenderMode.BALANCED.name
        val meshStr = prefs.getString("physics_mesh", PhysicsMesh.DYNAMIC.name) ?: PhysicsMesh.DYNAMIC.name
        val fpsStr = prefs.getString("target_fps", TargetFPS.FPS_60.name) ?: TargetFPS.FPS_60.name
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
            hapticsEnabled = haptics,
            bloomPostFx = render == RenderMode.REALISTIC,
            screenShake = shake
        )
    }

    private fun persistEngineSettings(settings: EngineSettings) {
        prefs.edit()
            .putString("theme_mode", settings.themeMode.name)
            .putString("render_mode", settings.renderMode.name)
            .putString("physics_mesh", settings.physicsMesh.name)
            .putString("target_fps", settings.targetFps.name)
            .putBoolean("haptics_enabled", settings.hapticsEnabled)
            .putBoolean("screen_shake", settings.screenShake)
            .apply()
    }
}
