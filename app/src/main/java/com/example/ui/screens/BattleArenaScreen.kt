package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BattleSimulation
import com.example.model.EngineSettings
import com.example.model.TurnState
import com.example.model.Vector2
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.CombatVfxRenderer
import com.example.ui.components.GraphicsConfigDialog
import com.example.ui.components.TrajectoryPredictor
import com.example.ui.theme.AppPalette
import com.example.ui.theme.AppTheme
import com.example.ui.theme.TacticalFontFamily
import com.example.viewmodel.BattleHudState
import kotlin.random.Random

@Composable
fun BattleArenaScreen(
    simulation: BattleSimulation,
    hudState: BattleHudState,
    turnState: TurnState,
    engineSettings: EngineSettings,
    onStepFrame: (Float) -> Unit,
    onPlayerLaunch: (Vector2, Float) -> Unit,
    onQuickLaunchOpening: () -> Unit,
    onTriggerUltimate: () -> Unit,
    onTriggerCounter: () -> Unit = {},
    onToggleSfx: () -> Unit,
    onToggleMusic: () -> Unit = {},
    onSetPaused: (Boolean) -> Unit = {},
    onSurrender: () -> Unit,
    onSaveEngineSettings: (EngineSettings) -> Unit
) {
    val palette = AppTheme.palette
    var showSettings by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            onSetPaused(false)
        }
    }

    BackHandler {
        if (showSettings) {
            showSettings = false
            onSetPaused(false)
        } else {
            onSurrender()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("battle_arena_screen")
    ) {
        val isCompactWidth = maxWidth < 385.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // 1. Top Bar: Exit + Player vs Rival HP/Ult Strip + Settings (all 48dp touch targets)
            ArenaTopBarWithHud(
                playerName = simulation.playerBall.character.name,
                opponentName = simulation.opponentBall.character.name,
                hudState = hudState,
                palette = palette,
                isCompactWidth = isCompactWidth,
                onSurrender = onSurrender,
                onOpenSettings = {
                    showSettings = true
                    onSetPaused(true)
                }
            )

            // 2. Maximized Playfield Court (60–120 FPS isolated Canvas + floating overlays)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.stageSurface)
                    .border(1.dp, palette.stageLine, RoundedCornerShape(18.dp))
            ) {
                ArenaCourtCanvas(
                    simulation = simulation,
                    turnState = turnState,
                    redirectReady = hudState.redirectReady,
                    engineSettings = engineSettings,
                    palette = palette,
                    onStepFrame = onStepFrame,
                    onPlayerLaunch = onPlayerLaunch,
                    onDoubleTapUltimate = onTriggerUltimate
                )

                // Floating Dash / Multi-Dash Indicator Pill (top-start, never shifts layout)
                Surface(
                    color = if (hudState.isMultiDashActive) {
                        palette.goldAccentSoft
                    } else {
                        palette.overlaySurface
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        when {
                            hudState.isMultiDashActive -> palette.goldAccent
                            hudState.redirectReady && turnState == TurnState.NONSTOP_CLASH -> palette.blueAccent
                            else -> palette.border
                        }
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 6.dp)
                        .testTag("dash_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hudState.isMultiDashActive -> palette.goldAccent
                                        hudState.redirectReady -> palette.blueAccent
                                        else -> palette.textMuted
                                    }
                                )
                        )
                        Text(
                            text = when {
                                turnState == TurnState.AIM_OPENING -> "Stretch to Aim"
                                hudState.isMultiDashActive && hudState.redirectReady ->
                                    "MULTI-DASH x${hudState.multiDashCharges}!"
                                hudState.isMultiDashActive ->
                                    "Dash x${hudState.multiDashCharges} (${hudState.redirectHalfSecsLeft / 2f}s)"
                                hudState.redirectReady -> "Dash Ready"
                                else -> "Dash ${hudState.redirectHalfSecsLeft / 2f}s"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hudState.isMultiDashActive) palette.goldAccent else palette.textPrimary,
                            maxLines = 1
                        )
                    }
                }

                // Floating compact impact banner (top-end / center-top, never shifts court layout)
                androidx.compose.animation.AnimatedVisibility(
                    visible = hudState.lastImpactBanner.isNotEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 8.dp, start = 112.dp)
                ) {
                    Surface(
                        color = palette.overlaySurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Text(
                            text = hudState.lastImpactBanner,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Compact Opening Shot Prompt
                if (turnState == TurnState.AIM_OPENING) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp, start = 12.dp, end = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = palette.overlaySurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Text(
                                text = "Pull further for faster dash • Double-tap for Ultimate!",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Button(
                            onClick = onQuickLaunchOpening,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primaryAccent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("quick_launch_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Start Fight", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // 3. Bottom Combat Bar: Sound, Music (when space permits), Auto-Counter Status, Ultimate Ability
            ArenaCompactBottomBar(
                ultimateName = simulation.playerBall.character.ultimateName,
                hudState = hudState,
                sfxEnabled = engineSettings.sfxEnabled,
                musicEnabled = engineSettings.musicEnabled,
                isCompactWidth = isCompactWidth,
                palette = palette,
                onToggleSfx = onToggleSfx,
                onToggleMusic = onToggleMusic,
                onTriggerUltimate = onTriggerUltimate
            )
        }
    }

    if (showSettings) {
        GraphicsConfigDialog(
            currentSettings = engineSettings,
            selectedCharacterId = simulation.playerBall.character.id,
            onSaveSettings = onSaveEngineSettings,
            onDismiss = {
                showSettings = false
                onSetPaused(false)
            }
        )
    }
}

@Composable
private fun ArenaTopBarWithHud(
    playerName: String,
    opponentName: String,
    hudState: BattleHudState,
    palette: AppPalette,
    isCompactWidth: Boolean,
    onSurrender: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Exit button (48dp touch target)
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier.size(48.dp)
        ) {
            IconButton(
                onClick = onSurrender,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("surrender_button")
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Leave Fight",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isCompactWidth) 7.dp else 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FighterHudColumn(
                    name = playerName,
                    hp = hudState.playerHp,
                    maxHp = hudState.playerMaxHp,
                    hpColor = palette.greenAccent,
                    ultPercent = hudState.playerUltPercent,
                    isUltActive = hudState.isPlayerUltActive,
                    ultActiveHalfSecs = hudState.playerUltActiveHalfSecs,
                    ultCooldownHalfSecs = hudState.playerUltCooldownHalfSecs,
                    ultMaxDurationSec = hudState.playerUltMaxDurationSec,
                    ultMaxCooldownSec = hudState.playerUltMaxCooldownSec,
                    palette = palette,
                    alignRight = false,
                    testTagPrefix = "player",
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = palette.elevatedSurface,
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                FighterHudColumn(
                    name = opponentName,
                    hp = hudState.opponentHp,
                    maxHp = hudState.opponentMaxHp,
                    hpColor = palette.primaryAccent,
                    ultPercent = hudState.opponentUltPercent,
                    isUltActive = hudState.isOpponentUltActive,
                    ultActiveHalfSecs = hudState.opponentUltActiveHalfSecs,
                    ultCooldownHalfSecs = hudState.opponentUltCooldownHalfSecs,
                    ultMaxDurationSec = hudState.opponentUltMaxDurationSec,
                    ultMaxCooldownSec = hudState.opponentUltMaxCooldownSec,
                    palette = palette,
                    alignRight = true,
                    testTagPrefix = "opponent",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Settings button (48dp touch target)
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier.size(48.dp)
        ) {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("arena_settings_button")
            ) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = "Settings",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun FighterHudColumn(
    name: String,
    hp: Int,
    maxHp: Int,
    hpColor: Color,
    ultPercent: Int,
    isUltActive: Boolean,
    ultActiveHalfSecs: Int,
    ultCooldownHalfSecs: Int,
    ultMaxDurationSec: Float,
    ultMaxCooldownSec: Float,
    palette: AppPalette,
    alignRight: Boolean,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    val isOnCooldown = ultCooldownHalfSecs > 0
    val isReady = !isUltActive && !isOnCooldown && ultPercent >= 100

    val ultProgress = when {
        isUltActive -> ((ultActiveHalfSecs / 2f) / ultMaxDurationSec.coerceAtLeast(1f)).coerceIn(0f, 1f)
        isOnCooldown -> ((ultCooldownHalfSecs / 2f) / ultMaxCooldownSec.coerceAtLeast(1f)).coerceIn(0f, 1f)
        else -> (ultPercent / 100f).coerceIn(0f, 1f)
    }

    val ultBarColor = when {
        isUltActive -> palette.goldAccent
        isOnCooldown -> palette.textMuted
        isReady -> palette.primaryAccent
        else -> palette.blueAccent
    }

    val ultStatusLabel = when {
        isUltActive -> "ON ${ultActiveHalfSecs / 2f}s"
        isOnCooldown -> "${ultCooldownHalfSecs / 2f}s"
        isReady -> "READY"
        else -> "$ultPercent%"
    }

    val hpRatio = (hp.toFloat() / maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!alignRight) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$hp",
                    style = MaterialTheme.typography.labelMedium,
                    color = hpColor
                )
            } else {
                Text(
                    text = "$hp",
                    style = MaterialTheme.typography.labelMedium,
                    color = hpColor
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        LinearProgressIndicator(
            progress = { hpRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = hpColor,
            trackColor = palette.elevatedSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTagPrefix}_ult_bar_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (!alignRight) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = ultBarColor,
                    modifier = Modifier.size(11.dp)
                )
                LinearProgressIndicator(
                    progress = { ultProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = ultBarColor,
                    trackColor = palette.elevatedSurface
                )
                Text(
                    text = ultStatusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = ultBarColor,
                    maxLines = 1
                )
            } else {
                Text(
                    text = ultStatusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = ultBarColor,
                    maxLines = 1
                )
                LinearProgressIndicator(
                    progress = { ultProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = ultBarColor,
                    trackColor = palette.elevatedSurface
                )
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = ultBarColor,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun ArenaCourtCanvas(
    simulation: BattleSimulation,
    turnState: TurnState,
    redirectReady: Boolean,
    engineSettings: EngineSettings,
    palette: AppPalette,
    onStepFrame: (Float) -> Unit,
    onPlayerLaunch: (Vector2, Float) -> Unit,
    onDoubleTapUltimate: () -> Unit
) {
    var frameTick by remember { mutableIntStateOf(0) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    val currentTurnState by rememberUpdatedState(turnState)
    val currentOnLaunch by rememberUpdatedState(onPlayerLaunch)
    val currentOnStep by rememberUpdatedState(onStepFrame)
    val currentOnDoubleTapUlt by rememberUpdatedState(onDoubleTapUltimate)
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(simulation, engineSettings.targetFps) {
        var lastNanos = 0L
        val minFrameNanos = 1_000_000_000L / engineSettings.targetFps.fps
        var accumNanos = 0L

        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val deltaNanos = nanos - lastNanos
                lastNanos = nanos
                accumNanos += deltaNanos

                if (accumNanos >= (minFrameNanos * 85L) / 100L) {
                    val dt = (accumNanos / 1_000_000_000f).coerceIn(0.001f, 0.035f)
                    accumNanos = 0L
                    currentOnStep(dt)
                    frameTick++
                }
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { sz ->
                simulation.configureArenaBounds(sz.width.toFloat(), sz.height.toFloat())
            }
            .pointerInput(simulation, "double_tap_ultimate") {
                detectTapGestures(
                    onDoubleTap = {
                        if (currentTurnState != TurnState.KNOCKOUT_SLOWMO) {
                            currentOnDoubleTapUlt()
                        }
                    }
                )
            }
            .pointerInput(simulation, "slingshot_drag") {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (currentTurnState != TurnState.KNOCKOUT_SLOWMO) {
                            dragStart = offset
                            dragCurrent = offset
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragCurrent = change.position
                    },
                    onDragEnd = {
                        val s = dragStart
                        val c = dragCurrent
                        if (s != null && c != null) {
                            val vec = Vector2(c.x - s.x, c.y - s.y)
                            val maxPull = size.width * 0.34f
                            currentOnLaunch(vec, maxPull)
                        }
                        dragStart = null
                        dragCurrent = null
                    },
                    onDragCancel = {
                        dragStart = null
                        dragCurrent = null
                    }
                )
            }
            .testTag("arena_playfield_canvas")
    ) {
        val tick = frameTick
        if (tick < 0) return@Canvas

        val shake = if (engineSettings.screenShake && simulation.screenShakeAmount > 0.5f) {
            simulation.screenShakeAmount
        } else 0f
        val sx = if (shake > 0f) (Random.nextFloat() * 2f - 1f) * shake * 0.45f else 0f
        val sy = if (shake > 0f) (Random.nextFloat() * 2f - 1f) * shake * 0.45f else 0f

        withTransform({ translate(sx, sy) }) {
            val pad = simulation.wallPadding
            val courtW = size.width - pad * 2f
            val courtH = size.height - pad * 2f

            drawRoundRect(
                color = palette.stageLine,
                topLeft = Offset(pad, pad),
                size = Size(courtW, courtH),
                cornerRadius = CornerRadius(20f, 20f),
                style = Stroke(width = 2.6f)
            )
            val midY = size.height * 0.5f
            drawLine(
                color = palette.stageLine,
                start = Offset(pad + 10f, midY),
                end = Offset(size.width - pad - 10f, midY),
                strokeWidth = 1.8f
            )
            drawCircle(
                color = palette.stageLine,
                radius = size.minDimension * 0.15f,
                center = Offset(size.width * 0.5f, midY),
                style = Stroke(width = 1.8f)
            )

            if (simulation.wallFlashLeft > 0.02f) {
                drawLine(
                    color = palette.blueAccent.copy(alpha = simulation.wallFlashLeft),
                    start = Offset(pad, pad + 18f),
                    end = Offset(pad, size.height - pad - 18f),
                    strokeWidth = 6f
                )
            }
            if (simulation.wallFlashRight > 0.02f) {
                drawLine(
                    color = palette.blueAccent.copy(alpha = simulation.wallFlashRight),
                    start = Offset(size.width - pad, pad + 18f),
                    end = Offset(size.width - pad, size.height - pad - 18f),
                    strokeWidth = 6f
                )
            }
            if (simulation.wallFlashTop > 0.02f) {
                drawLine(
                    color = palette.primaryAccent.copy(alpha = simulation.wallFlashTop),
                    start = Offset(pad + 18f, pad),
                    end = Offset(size.width - pad - 18f, pad),
                    strokeWidth = 6f
                )
            }
            if (simulation.wallFlashBottom > 0.02f) {
                drawLine(
                    color = palette.greenAccent.copy(alpha = simulation.wallFlashBottom),
                    start = Offset(pad + 18f, size.height - pad),
                    end = Offset(size.width - pad - 18f, size.height - pad),
                    strokeWidth = 6f
                )
            }

            CombatVfxRenderer.drawFloorHazards(this, simulation.fireHazards, simulation.globalTime)

            if (turnState == TurnState.AIM_OPENING) {
                TrajectoryPredictor.drawReadyReticle(
                    scope = this,
                    ballPosition = simulation.playerBall.position,
                    ballRadius = simulation.playerBall.radius,
                    globalTime = simulation.globalTime,
                    reticleColor = palette.aimReticle
                )
            }

            val s = dragStart
            val c = dragCurrent
            if (s != null && c != null && (turnState == TurnState.AIM_OPENING || redirectReady)) {
                TrajectoryPredictor.drawAimTrajectory(
                    scope = this,
                    ballPosition = simulation.playerBall.position,
                    dragVector = Vector2(c.x - s.x, c.y - s.y),
                    maxPullPx = size.width * 0.34f,
                    wallLeft = simulation.wallLeft,
                    wallRight = simulation.wallRight,
                    wallTop = simulation.wallTop,
                    wallBottom = simulation.wallBottom,
                    ballRadius = simulation.playerBall.radius,
                    isRedirectAim = turnState == TurnState.NONSTOP_CLASH,
                    reticleColor = palette.aimReticle
                )
            }

            AnimeBallRenderer.drawBall(
                scope = this,
                ball = simulation.opponentBall,
                renderMode = engineSettings.renderMode,
                physicsMesh = engineSettings.physicsMesh,
                globalTime = simulation.globalTime
            )
            AnimeBallRenderer.drawBall(
                scope = this,
                ball = simulation.playerBall,
                renderMode = engineSettings.renderMode,
                physicsMesh = engineSettings.physicsMesh,
                globalTime = simulation.globalTime
            )

            drawCircle(
                color = palette.greenAccent.copy(alpha = 0.55f),
                radius = simulation.playerBall.radius * 1.12f,
                center = Offset(simulation.playerBall.position.x, simulation.playerBall.position.y),
                style = Stroke(width = 2f)
            )

            CombatVfxRenderer.drawProjectiles(
                scope = this,
                projectiles = simulation.projectiles,
                globalTime = simulation.globalTime,
                renderMode = engineSettings.renderMode
            )

            CombatVfxRenderer.drawVfx(
                scope = this,
                particles = simulation.particles,
                shockwaves = simulation.shockwaves,
                floatingTexts = simulation.floatingTexts,
                textMeasurer = textMeasurer,
                displayFont = TacticalFontFamily,
                renderMode = engineSettings.renderMode,
                defaultTextColor = palette.damageTextDefault,
                isDarkStage = palette.isDark
            )
        }
    }
}

@Composable
private fun ArenaCompactBottomBar(
    ultimateName: String,
    hudState: BattleHudState,
    sfxEnabled: Boolean,
    musicEnabled: Boolean,
    isCompactWidth: Boolean,
    palette: AppPalette,
    onToggleSfx: () -> Unit,
    onToggleMusic: () -> Unit,
    onTriggerUltimate: () -> Unit
) {
    val isPlayerUltOnCd = hudState.playerUltCooldownHalfSecs > 0
    val ultReady = hudState.playerUltPercent >= 100 && !hudState.isPlayerUltActive && !isPlayerUltOnCd

    val ultProgress = when {
        hudState.isPlayerUltActive -> ((hudState.playerUltActiveHalfSecs / 2f) / hudState.playerUltMaxDurationSec.coerceAtLeast(1f)).coerceIn(0f, 1f)
        isPlayerUltOnCd -> ((hudState.playerUltCooldownHalfSecs / 2f) / hudState.playerUltMaxCooldownSec.coerceAtLeast(1f)).coerceIn(0f, 1f)
        else -> (hudState.playerUltPercent / 100f).coerceIn(0f, 1f)
    }

    val pulseTransition = rememberInfiniteTransition(label = "ult_ready_pulse")
    val readyScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "readyScale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sound toggle (48dp touch target)
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier.size(48.dp)
        ) {
            IconButton(
                onClick = onToggleSfx,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("arena_sfx_toggle_button")
            ) {
                Icon(
                    imageVector = if (sfxEnabled) Icons.AutoMirrored.Outlined.VolumeUp else Icons.AutoMirrored.Outlined.VolumeOff,
                    contentDescription = if (sfxEnabled) "Mute Sound" else "Turn On Sound",
                    tint = if (sfxEnabled) palette.blueAccent else palette.textMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Music toggle (48dp touch target)
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier.size(48.dp)
        ) {
            IconButton(
                onClick = onToggleMusic,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("arena_music_toggle_button")
            ) {
                Icon(
                    imageVector = if (musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    contentDescription = if (musicEnabled) "Mute Fight Music" else "Play Fight Music",
                    tint = if (musicEnabled) palette.goldAccent else palette.textMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Full-width Ultimate Button (48dp height — Counter bar removed; double-tapping court also fires Ultimate!)
        Surface(
            onClick = { if (ultReady) onTriggerUltimate() },
            enabled = ultReady,
            shape = RoundedCornerShape(12.dp),
            color = when {
                hudState.isPlayerUltActive -> palette.goldAccentSoft
                ultReady -> palette.primaryAccent
                else -> palette.cardSurface
            },
            border = BorderStroke(
                1.dp,
                when {
                    hudState.isPlayerUltActive -> palette.goldAccent
                    ultReady -> palette.primaryAccent
                    else -> palette.border
                }
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .graphicsLayer {
                    val s = if (ultReady) readyScale else 1f
                    scaleX = s
                    scaleY = s
                }
                .testTag("ultimate_awaken_button")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = when {
                            ultReady -> Color.White
                            hudState.isPlayerUltActive -> palette.goldAccent
                            isPlayerUltOnCd -> palette.textMuted
                            else -> palette.goldAccent
                        },
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            hudState.isPlayerUltActive && hudState.multiDashCharges > 0 ->
                                "ULT ${hudState.playerUltActiveHalfSecs / 2f}s • ${hudState.multiDashCharges}x DASH"
                            hudState.isPlayerUltActive ->
                                "ULT ${hudState.playerUltActiveHalfSecs / 2f}s"
                            isPlayerUltOnCd ->
                                "Wait ${hudState.playerUltCooldownHalfSecs / 2f}s"
                            ultReady ->
                                if (isCompactWidth) "ULT: $ultimateName (2x Tap!)" else "ULT: $ultimateName • Tap or 2x Tap!"
                            else ->
                                "Ult ${hudState.playerUltPercent}% (2x Tap Ready)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            ultReady -> Color.White
                            hudState.isPlayerUltActive -> palette.goldAccent
                            isPlayerUltOnCd -> palette.textSecondary
                            else -> palette.textPrimary
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                LinearProgressIndicator(
                    progress = { ultProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = when {
                        ultReady -> Color.White
                        hudState.isPlayerUltActive -> palette.goldAccent
                        isPlayerUltOnCd -> palette.textMuted
                        else -> palette.blueAccent
                    },
                    trackColor = if (ultReady) Color.White.copy(alpha = 0.28f) else palette.elevatedSurface
                )
            }
        }
    }
}
