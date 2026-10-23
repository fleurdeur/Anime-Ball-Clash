package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.model.CharacterRoster
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
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            AnimeBallClashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: GameViewModel = viewModel()
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
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val countdown by viewModel.matchmakingCountdown.collectAsStateWithLifecycle()
    val opponentName by viewModel.opponentName.collectAsStateWithLifecycle()
    val opponentChar by viewModel.opponentCharacter.collectAsStateWithLifecycle()
    val battleSim by viewModel.battleSim.collectAsStateWithLifecycle()
    val hudState by viewModel.hudState.collectAsStateWithLifecycle()
    val turnState by viewModel.turnState.collectAsStateWithLifecycle()
    val matchStats by viewModel.matchStats.collectAsStateWithLifecycle()
    val matchTimerSeconds by viewModel.matchTimerSeconds.collectAsStateWithLifecycle()

    val playerChar = CharacterRoster.getById(playerProfile.selectedCharacterId)

    when (currentScreen) {
        AppScreen.LOADING -> {
            LoadingScreen(
                onLoadComplete = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.HOME_HUB -> {
            HomeHubScreen(
                profile = playerProfile,
                engineSettings = engineSettings,
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onCycleCharacter = { delta -> viewModel.cycleShowcaseCharacter(delta) },
                onStartBattle = { mode, diff -> viewModel.startMatchmaking(mode, diff) },
                onUpdateUsername = { newName -> viewModel.updateUsername(newName) },
                onSaveEngineSettings = { newSettings -> viewModel.updateEngineSettings(newSettings) }
            )
        }

        AppScreen.CHARACTER_SELECT -> {
            CharacterSelectScreen(
                currentSelectedId = playerProfile.selectedCharacterId,
                unlockedIds = unlockedCharacterIds,
                engineSettings = engineSettings,
                onSelectCharacter = { charId -> viewModel.selectCharacter(charId) },
                onStartBattle = { viewModel.startMatchmaking(GameMode.PRACTICE_BOT) },
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
            battleSim?.let { sim ->
                BattleArenaScreen(
                    simulation = sim,
                    hudState = hudState,
                    turnState = turnState,
                    matchTimerSeconds = matchTimerSeconds,
                    engineSettings = engineSettings,
                    onStepFrame = { dt -> viewModel.stepBattleFrame(dt) },
                    onPlayerLaunch = { dragVector, maxPullPx ->
                        viewModel.onPlayerLaunch(dragVector, maxPullPx)
                    },
                    onTriggerUltimate = { viewModel.onPlayerTriggerUltimate() },
                    onSurrender = { viewModel.navigateTo(AppScreen.HOME_HUB) },
                    onSaveEngineSettings = { newSettings ->
                        viewModel.updateEngineSettings(newSettings)
                    }
                )
            }
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
                onUnlockCharacter = { char -> viewModel.unlockCharacter(char) },
                onBack = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.QUESTS -> {
            QuestsScreen(
                quests = quests,
                onClaimQuest = { questId -> viewModel.claimQuest(questId) },
                onBack = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }

        AppScreen.LEADERBOARD -> {
            LeaderboardScreen(
                leaderboard = leaderboard,
                onBack = { viewModel.navigateTo(AppScreen.HOME_HUB) }
            )
        }
    }
}
