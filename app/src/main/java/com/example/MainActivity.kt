package com.example

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.model.AppThemeMode
import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import com.example.model.GameMode
import com.example.network.AuthManager
import com.example.ui.screens.BattleArenaScreen
import com.example.ui.screens.CharacterSelectScreen
import com.example.ui.screens.CustomMatchScreen
import com.example.ui.screens.HomeHubScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.MatchmakingScreen
import com.example.ui.screens.MultiplayerLobbyScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.theme.AnimeBallClashTheme
import com.example.ui.theme.AppTheme
import com.example.viewmodel.GameViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            AnimeBallRootAuthGate()
        }
    }
}

@Composable
private fun AnimeBallRootAuthGate() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val prefs = remember(context) {
        context.getSharedPreferences("anime_ball_clash_clean_v1", Context.MODE_PRIVATE)
    }
    var preLoginThemeMode by remember {
        val raw = prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        mutableStateOf(AppThemeMode.entries.firstOrNull { it.name == raw } ?: AppThemeMode.DARK)
    }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }
    var isAuthBusy by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                scope = scope,
                onAuthSuccess = { user ->
                    currentUser = user
                    authErrorMessage = null
                },
                onUnauthenticated = {
                    // Wait cleanly for interactive Google Sign-In button tap
                }
            )
        }
    }

    val signedInUser = currentUser
    if (signedInUser == null) {
        AnimeBallClashTheme(themeMode = preLoginThemeMode) {
            ApplySystemBarAppearance()
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppTheme.palette.background
            ) {
                SignInScreen(
                    isBusy = isAuthBusy,
                    errorMessage = authErrorMessage,
                    onSignInClick = {
                        isAuthBusy = true
                        authErrorMessage = null
                        AuthManager.signInWithGoogle(
                            context = context,
                            credentialManager = credentialManager,
                            scope = scope,
                            onAuthSuccess = { user ->
                                isAuthBusy = false
                                currentUser = user
                            },
                            onAuthCancelled = {
                                isAuthBusy = false
                            },
                            onAuthError = { err ->
                                isAuthBusy = false
                                authErrorMessage = err
                            }
                        )
                    },
                    onToggleTheme = {
                        val next = if (preLoginThemeMode == AppThemeMode.DARK) {
                            AppThemeMode.LIGHT
                        } else {
                            AppThemeMode.DARK
                        }
                        preLoginThemeMode = next
                        prefs.edit().putString("theme_mode", next.name).apply()
                    }
                )
            }
        }
    } else {
        // Strictly instantiate GameViewModel and Firestore listeners ONLY after authentication succeeds
        val viewModel: GameViewModel = viewModel(key = signedInUser.uid)
        val engineSettings by viewModel.engineSettings.collectAsStateWithLifecycle()
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner, viewModel) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> viewModel.onAppPause()
                    Lifecycle.Event.ON_RESUME -> viewModel.onAppResume()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(signedInUser.uid) {
            viewModel.bindAuthenticatedUser(
                userId = signedInUser.uid,
                displayName = signedInUser.displayName
            )
        }

        AnimeBallClashTheme(themeMode = engineSettings.themeMode) {
            ApplySystemBarAppearance()
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppTheme.palette.background
            ) {
                AnimeBallClashApp(
                    viewModel = viewModel,
                    onSignOut = {
                        viewModel.onAppPause()
                        AuthManager.signOut(
                            credentialManager = credentialManager,
                            scope = scope,
                            onComplete = {
                                currentUser = null
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ApplySystemBarAppearance() {
    val palette = AppTheme.palette
    val view = LocalView.current
    if (!view.isInEditMode) {
        val isDarkTheme = palette.isDark
        SideEffect {
            val activity = view.context as? ComponentActivity ?: return@SideEffect
            val barStyle = if (isDarkTheme) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            }
            activity.enableEdgeToEdge(
                statusBarStyle = barStyle,
                navigationBarStyle = barStyle
            )
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}

@Composable
fun AnimeBallClashApp(
    viewModel: GameViewModel,
    onSignOut: () -> Unit = {}
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val lastNavWasPop by viewModel.lastNavWasPop.collectAsStateWithLifecycle()
    val engineSettings by viewModel.engineSettings.collectAsStateWithLifecycle()
    val playerProfile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val unlockedCharacterIds by viewModel.unlockedCharacterIds.collectAsStateWithLifecycle()
    val characterUpgrades by viewModel.characterUpgrades.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val dailyQuests by viewModel.dailyQuests.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val botDifficulty by viewModel.botDifficulty.collectAsStateWithLifecycle()
    val customOpponent by viewModel.customOpponentCharacter.collectAsStateWithLifecycle()
    val countdown by viewModel.matchmakingCountdown.collectAsStateWithLifecycle()
    val canStartChallenger by viewModel.canStartRankedChallenger.collectAsStateWithLifecycle()
    val opponentName by viewModel.opponentName.collectAsStateWithLifecycle()
    val opponentChar by viewModel.opponentCharacter.collectAsStateWithLifecycle()
    val localIpAddress by viewModel.localIpAddress.collectAsStateWithLifecycle()
    val lanSessionState by viewModel.lanSessionState.collectAsStateWithLifecycle()
    val discoveredLanHosts by viewModel.discoveredLanHosts.collectAsStateWithLifecycle()
    val activeRoomCode by viewModel.activeRoomCode.collectAsStateWithLifecycle()
    val isOnlineRoomBusy by viewModel.isOnlineRoomBusy.collectAsStateWithLifecycle()
    val lobbyStatusMessage by viewModel.lobbyStatusMessage.collectAsStateWithLifecycle()
    val matchStats by viewModel.matchStats.collectAsStateWithLifecycle()

    val palette = AppTheme.palette

    val claimableQuestCount by remember(quests, dailyQuests) {
        derivedStateOf {
            quests.count { !it.isClaimed && it.currentProgress >= it.targetProgress } +
                dailyQuests.count { !it.isClaimed && it.currentProgress >= it.targetProgress }
        }
    }

    val playerChar = remember(playerProfile.selectedCharacterId) {
        CharacterRoster.getById(playerProfile.selectedCharacterId)
    }

    val activeUpgrades = characterUpgrades[playerProfile.selectedCharacterId] ?: CharacterUpgradeState()

    AnimatedContent(
        targetState = currentScreen,
        modifier = Modifier.fillMaxSize(),
        label = "screen_page_transition",
        transitionSpec = {
            val isArenaTransition =
                initialState == AppScreen.LOADING ||
                    targetState == AppScreen.LOADING ||
                    initialState == AppScreen.BATTLE_ARENA ||
                    targetState == AppScreen.BATTLE_ARENA

            if (isArenaTransition) {
                (fadeIn(tween(180, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.985f, animationSpec = tween(180, easing = FastOutSlowInEasing)))
                    .togetherWith(fadeOut(tween(140)))
                    .using(SizeTransform(clip = false))
            } else if (lastNavWasPop) {
                (slideInHorizontally(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> -fullWidth / 7 }
                ) + fadeIn(tween(200)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(200, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth / 7 }
                        ) + fadeOut(tween(160))
                    )
                    .using(SizeTransform(clip = false))
            } else {
                (slideInHorizontally(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> fullWidth / 7 }
                ) + fadeIn(tween(200)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(200, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth / 7 }
                        ) + fadeOut(tween(160))
                    )
                    .using(SizeTransform(clip = false))
            }
        }
    ) { screen ->
        when (screen) {
            AppScreen.LOADING -> {
                LoadingScreen(
                    onLoadComplete = { viewModel.navigateTo(AppScreen.HOME_HUB, recordHistory = false) }
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
                    onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                    onCycleCharacter = { delta -> viewModel.cycleShowcaseCharacter(delta) },
                    onSelectDifficulty = { diff -> viewModel.setBotDifficulty(diff) },
                    onStartBattle = { mode, diff -> viewModel.startMatchmaking(mode, diff) },
                    onUpdateUsername = { newName -> viewModel.updateUsername(newName) },
                    onSaveEngineSettings = { newSettings -> viewModel.updateEngineSettings(newSettings) },
                    onQuickToggleTheme = {
                        val nextMode = if (palette.isDark) AppThemeMode.LIGHT else AppThemeMode.DARK
                        viewModel.setThemeMode(nextMode)
                    },
                    onResetAccount = { viewModel.resetAccountToZero() },
                    onSignOut = onSignOut
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
                    onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                    onBack = { viewModel.navigateBack() }
                )
            }

            AppScreen.CUSTOM_MATCH -> {
                CustomMatchScreen(
                    profile = playerProfile,
                    unlockedIds = unlockedCharacterIds,
                    selectedOpponent = customOpponent,
                    selectedDifficulty = botDifficulty,
                    onSelectPlayerCharacter = { charId -> viewModel.selectCharacter(charId) },
                    onSelectOpponentCharacter = { char -> viewModel.selectCustomOpponent(char) },
                    onSelectDifficulty = { diff -> viewModel.setBotDifficulty(diff) },
                    onStartCustomFight = { viewModel.startCustomMatch() },
                    onBack = { viewModel.navigateBack() }
                )
            }

            AppScreen.MULTIPLAYER_LOBBY -> {
                MultiplayerLobbyScreen(
                    profile = playerProfile,
                    playerChar = playerChar,
                    localIpAddress = localIpAddress,
                    lanSessionState = lanSessionState,
                    discoveredLanHosts = discoveredLanHosts,
                    activeRoomCode = activeRoomCode,
                    isOnlineRoomBusy = isOnlineRoomBusy,
                    lobbyStatusMessage = lobbyStatusMessage,
                    onStartLanDiscovery = { viewModel.startLanDiscovery() },
                    onHostLanRoom = { viewModel.hostLanRoom() },
                    onJoinLanAddress = { addr -> viewModel.joinLanAddress(addr) },
                    onCancelLanSession = { viewModel.cancelLanSession() },
                    onCreateOnlineRoom = { viewModel.createOnlineFriendRoom() },
                    onJoinOnlineRoom = { code -> viewModel.joinOnlineFriendRoom(code) },
                    onCancelOnlineRoom = { viewModel.cancelOnlineRoom() },
                    onChangeFighter = { viewModel.navigateTo(AppScreen.CHARACTER_SELECT) },
                    onBack = { viewModel.navigateBack() }
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
                    canStartRankedChallenger = canStartChallenger,
                    onStartRankedChallenger = { viewModel.startRankedChallengerFallback() },
                    onCancel = { viewModel.navigateBack() }
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
                        onHomeHub = { viewModel.navigateToHomeReset() },
                        onBack = { viewModel.navigateBack() }
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
                    onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                    onBack = { viewModel.navigateBack() }
                )
            }

            AppScreen.QUESTS -> {
                QuestsScreen(
                    quests = quests,
                    dailyQuests = dailyQuests,
                    onClaimQuest = { questId -> viewModel.claimQuest(questId) },
                    onClaimAll = { viewModel.claimAllReadyQuests() },
                    onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                    onBack = { viewModel.navigateBack() }
                )
            }

            AppScreen.LEADERBOARD -> {
                LeaderboardScreen(
                    leaderboard = leaderboard,
                    claimableQuestCount = claimableQuestCount,
                    onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                    onBack = { viewModel.navigateBack() }
                )
            }
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
            onTriggerCounter = { viewModel.onPlayerTriggerCounter() },
            onToggleSfx = { viewModel.toggleSfx() },
            onToggleMusic = { viewModel.toggleMusic() },
            onSetPaused = { paused -> viewModel.setBattlePaused(paused) },
            onSurrender = { viewModel.forfeitAndExitArena(toOrigin = true) },
            onSaveEngineSettings = { newSettings ->
                viewModel.updateEngineSettings(newSettings)
            }
        )
    }
}
