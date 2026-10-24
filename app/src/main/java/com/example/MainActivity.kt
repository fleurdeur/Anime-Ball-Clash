package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.model.AppThemeMode
import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import com.example.model.GameMode
import com.example.ui.screens.BattleArenaScreen
import com.example.ui.screens.CharacterSelectScreen
import com.example.ui.screens.HomeHubScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.MatchmakingScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.AnimeBallClashTheme
import com.example.ui.theme.AppTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            val viewModel: GameViewModel = viewModel()
            val engineSettings by viewModel.engineSettings.collectAsStateWithLifecycle()

            AnimeBallClashTheme(themeMode = engineSettings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppTheme.palette.background
                ) {
                    AnimeBallClashApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AnimeBallClashApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val engineSettings by viewModel.engineSettings.collectAsStateWithLifecycle()
    val playerProfile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val unlockedCharacterIds by viewModel.unlockedCharacterIds.collectAsStateWithLifecycle()
    val characterUpgrades by viewModel.characterUpgrades.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val botDifficulty by viewModel.botDifficulty.collectAsStateWithLifecycle()
    val countdown by viewModel.matchmakingCountdown.collectAsStateWithLifecycle()
    val opponentName by viewModel.opponentName.collectAsStateWithLifecycle()
    val opponentChar by viewModel.opponentCharacter.collectAsStateWithLifecycle()
    val matchStats by viewModel.matchStats.collectAsStateWithLifecycle()

    val palette = AppTheme.palette

    val claimableQuestCount by remember(quests) {
        derivedStateOf {
            quests.count { !it.isClaimed && it.currentProgress >= it.targetProgress }
        }
    }

    val playerChar = remember(playerProfile.selectedCharacterId) {
        CharacterRoster.getById(playerProfile.selectedCharacterId)
    }

    val activeUpgrades = characterUpgrades[playerProfile.selectedCharacterId] ?: CharacterUpgradeState()

    // Dynamically match system bar icon contrast to Dark vs Light mode
    val view = LocalView.current
    if (!view.isInEditMode) {
        val useDarkStatusIcons = !palette.isDark
        SideEffect {
            val window = (view.context as? ComponentActivity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = useDarkStatusIcons
            controller.isAppearanceLightNavigationBars = useDarkStatusIcons
        }
    }

    when (currentScreen) {
        AppScreen.LOADING -> {
            LoadingScreen(
                onLoadComplete = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.HOME_HUB -> {
            HomeHubScreen(
                profile = playerProfile,
                activeUpgrades = activeUpgrades,
                unlockedCount = unlockedCharacterIds.size,
                claimableQuestCount = claimableQuestCount,
                selectedDifficulty = botDifficulty,
                engineSettings = engineSettings,
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onCycleCharacter = { delta -> viewModel.cycleShowcaseCharacter(delta) },
                onSelectDifficulty = { diff -> viewModel.setBotDifficulty(diff) },
                onStartBattle = { mode, diff -> viewModel.startMatchmaking(mode, diff) },
                onUpdateUsername = { newName -> viewModel.updateUsername(newName) },
                onSaveEngineSettings = { newSettings -> viewModel.updateEngineSettings(newSettings) },
                onQuickToggleTheme = {
                    val nextMode = if (palette.isDark) AppThemeMode.LIGHT else AppThemeMode.DARK
                    viewModel.setThemeMode(nextMode)
                },
                onResetAccount = { viewModel.resetAccountToZero() }
            )
        }

        AppScreen.CHARACTER_SELECT -> {
            CharacterSelectScreen(
                profile = playerProfile,
                unlockedIds = unlockedCharacterIds,
                characterUpgrades = characterUpgrades,
                claimableQuestCount = claimableQuestCount,
                onSelectCharacter = { charId -> viewModel.selectCharacter(charId) },
                onUnlockCharacter = { char -> viewModel.unlockCharacter(char) },
                onUpgradeStat = { charId, stat -> viewModel.upgradeCharacterStat(charId, stat) },
                onStartBattle = { viewModel.startMatchmaking(GameMode.PRACTICE_BOT) },
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onBack = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.MATCHMAKING -> {
            MatchmakingScreen(
                profile = playerProfile,
                playerChar = playerChar,
                opponentName = opponentName,
                opponentChar = opponentChar,
                gameMode = selectedMode,
                countdown = countdown,
                onCancel = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.BATTLE_ARENA -> {
            BattleArenaRoute(viewModel = viewModel)
        }

        AppScreen.RESULT -> {
            matchStats?.let { stats ->
                ResultScreen(
                    stats = stats,
                    profile = playerProfile,
                    onRematch = { viewModel.startMatchmaking(selectedMode) },
                    onCharacterSelect = { viewModel.navigateTo(AppScreen.CHARACTER_SELECT) },
                    onHomeHub = { viewModel.navigateTo(AppScreen.HOME_HUB) }
                )
            }
        }

        AppScreen.SHOP -> {
            ShopScreen(
                profile = playerProfile,
                unlockedIds = unlockedCharacterIds,
                characterUpgrades = characterUpgrades,
                claimableQuestCount = claimableQuestCount,
                onUnlockCharacter = { char -> viewModel.unlockCharacter(char) },
                onUpgradeStat = { charId, stat -> viewModel.upgradeCharacterStat(charId, stat) },
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onBack = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.QUESTS -> {
            QuestsScreen(
                quests = quests,
                onClaimQuest = { questId -> viewModel.claimQuest(questId) },
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onBack = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.LEADERBOARD -> {
            LeaderboardScreen(
                leaderboard = leaderboard,
                claimableQuestCount = claimableQuestCount,
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onBack = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }
    }
}

@Composable
private fun BattleArenaRoute(viewModel: GameViewModel) {
    val battleSim by viewModel.battleSim.collectAsStateWithLifecycle()
    val hudState by viewModel.hudState.collectAsStateWithLifecycle()
    val turnState by viewModel.turnState.collectAsStateWithLifecycle()
    val engineSettings by viewModel.engineSettings.collectAsStateWithLifecycle()

    battleSim?.let { sim ->
        BattleArenaScreen(
            simulation = sim,
            hudState = hudState,
            turnState = turnState,
            engineSettings = engineSettings,
            onStepFrame = { dt -> viewModel.stepBattleFrame(dt) },
            onPlayerLaunch = { dragVector, maxPullPx ->
                viewModel.onPlayerLaunch(dragVector, maxPullPx)
            },
            onQuickLaunchOpening = { viewModel.onQuickLaunchOpening() },
            onTriggerUltimate = { viewModel.onPlayerTriggerUltimate() },
            onToggleSfx = { viewModel.toggleSfx() },
            onSurrender = { viewModel.navigateTo(AppScreen.HOME_HUB) },
            onSaveEngineSettings = { newSettings ->
                viewModel.updateEngineSettings(newSettings)
            }
        )
    }
}
