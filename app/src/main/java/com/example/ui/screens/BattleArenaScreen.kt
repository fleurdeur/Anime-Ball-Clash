package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
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
    onSurrender: () -> Unit,
    onSaveEngineSettings: (EngineSettings) -> Unit
) {
    BackHandler { onSurrender() }

    val palette = AppTheme.palette
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("battle_arena_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Clean Top Fighter Health & Status Bar (only recomposes when integer HP changes)
        ArenaTopHud(
            playerName = simulation.playerBall.character.name,
            opponentName = simulation.opponentBall.character.name,
            hudState = hudState,
            palette = palette,
            onOpenSettings = { showSettings = true },
            onSurrender = onSurrender
        )

        // 2. High-Performance Isolated Playfield Court (60-120 FPS Canvas; zero parent recomposition)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(palette.stageSurface)
                .border(1.dp, palette.stageLine, RoundedCornerShape(22.dp))
        ) {
            ArenaCourtCanvas(
                simulation = simulation,
                turnState = turnState,
                redirectReady = hudState.redirectReady,
                engineSettings = engineSettings,
                palette = palette,
                onStepFrame = onStepFrame,
                onPlayerLaunch = onPlayerLaunch
            )

            // Opening Launch Prompt Overlay (disappears permanently once launched)
            if (turnState == TurnState.AIM_OPENING) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xD90E121B),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, palette.stageLine)
                    ) {
                        Text(
                            text = "Drag anywhere to launch • Nonstop bounce until 0 HP",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Button(
                        onClick = onQuickLaunchOpening,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primaryAccent,
                            contentColor = Color(0xFFF8FAFC)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("quick_launch_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Quick Launch Clash", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // 3. Bottom Tactical Action Bar (Redirect status + Ultimate Awakening button)
        ArenaBottomBar(
            ultimateName = simulation.playerBall.character.ultimateName,
            hudState = hudState,
            turnState = turnState,
            palette = palette,
            onTriggerUltimate = onTriggerUltimate
        )
    }

    if (showSettings) {
        GraphicsConfigDialog(
            currentSettings = engineSettings,
            selectedCharacterId = simulation.playerBall.character.id,
            onSaveSettings = onSaveEngineSettings,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun ArenaTopHud(
    playerName: String,
    opponentName: String,
    hudState: BattleHudState,
    palette: AppPalette,
    onOpenSettings: () -> Unit,
    onSurrender: () -> Unit
) {
    Surface(
        color = palette.cardSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Player HP Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = playerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${hudState.playerHp} HP",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.greenAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    LinearProgressIndicator(
                        progress = { (hudState.playerHp / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = palette.greenAccent,
                        trackColor = palette.elevatedSurface
                    )
                }

                // Controls cluster
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("arena_settings_button")
                    ) {
                        Icon(
                            Icons.Outlined.Tune,
                            contentDescription = "Engine & Theme Settings",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onSurrender,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("surrender_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Leave Match",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Opponent HP Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${hudState.opponentHp} HP",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.primaryAccent
                        )
                        Text(
                            text = opponentName,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    LinearProgressIndicator(
                        progress = { (hudState.opponentHp / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = palette.primaryAccent,
                        trackColor = palette.elevatedSurface
                    )
                }
            }

            if (hudState.lastImpactBanner.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = hudState.lastImpactBanner,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * Leaf Composable: `frameTick` is ONLY read inside `Canvas { ... }` DrawScope,
 * guaranteeing 0 layout/recomposition overhead during nonstop 60-120 FPS physics!
 */
@Composable
private fun ArenaCourtCanvas(
    simulation: BattleSimulation,
    turnState: TurnState,
    redirectReady: Boolean,
    engineSettings: EngineSettings,
    palette: AppPalette,
    onStepFrame: (Float) -> Unit,
    onPlayerLaunch: (Vector2, Float) -> Unit
) {
    var frameTick by remember { mutableIntStateOf(0) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    val currentTurnState by rememberUpdatedState(turnState)
    val currentOnLaunch by rememberUpdatedState(onPlayerLaunch)
    val currentOnStep by rememberUpdatedState(onStepFrame)
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
            .pointerInput(simulation) {
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
        // Observe frameTick solely inside DrawScope!
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

            // 1. Crisp athletic court markings
            drawRoundRect(
                color = palette.stageLine,
                topLeft = Offset(pad, pad),
                size = Size(courtW, courtH),
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(width = 3f)
            )
            val midY = size.height * 0.5f
            drawLine(
                color = palette.stageLine,
                start = Offset(pad + 12f, midY),
                end = Offset(size.width - pad - 12f, midY),
                strokeWidth = 2f
            )
            drawCircle(
                color = palette.stageLine,
                radius = size.minDimension * 0.16f,
                center = Offset(size.width * 0.5f, midY),
                style = Stroke(width = 2f)
            )

            // 2. Reactive wall impact flashes
            if (simulation.wallFlashLeft > 0.02f) {
                drawLine(
                    color = palette.blueAccent.copy(alpha = simulation.wallFlashLeft),
                    start = Offset(pad, pad + 20f),
                    end = Offset(pad, size.height - pad - 20f),
                    strokeWidth = 6f
                )
            }
            if (simulation.wallFlashRight > 0.02f) {
                drawLine(
                    color = palette.blueAccent.copy(alpha = simulation.wallFlashRight),
                    start = Offset(size.width - pad, pad + 20f),
                    end = Offset(size.width - pad, size.height - pad - 20f),
                    strokeWidth = 6f
                )
            }
            if (simulation.wallFlashTop > 0.02f) {
                drawLine(
                    color = palette.primaryAccent.copy(alpha = simulation.wallFlashTop),
                    start = Offset(pad + 20f, pad),
                    end = Offset(size.width - pad - 20f, pad),
                    strokeWidth = 6f
                )
            }
            if (simulation.wallFlashBottom > 0.02f) {
                drawLine(
                    color = palette.greenAccent.copy(alpha = simulation.wallFlashBottom),
                    start = Offset(pad + 20f, size.height - pad),
                    end = Offset(size.width - pad - 20f, size.height - pad),
                    strokeWidth = 6f
                )
            }

            // 3. Floor fire hazards
            CombatVfxRenderer.drawFloorHazards(this, simulation.fireHazards, simulation.globalTime)

            // 4. Opening reticle or drag trajectory laser
            if (turnState == TurnState.AIM_OPENING) {
                TrajectoryPredictor.drawReadyReticle(
                    scope = this,
                    ballPosition = simulation.playerBall.position,
                    ballRadius = simulation.playerBall.radius,
                    globalTime = simulation.globalTime
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
                    isRedirectAim = turnState == TurnState.NONSTOP_CLASH
                )
            }

            // 5. Render Opponent & Player Anime Spheres (all details locked onto Offset.Zero)
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

            // 6. Subtle Player ring marker so player always tracks their sphere at high speed
            drawCircle(
                color = Color(0xFF3CC27B).copy(alpha = 0.55f),
                radius = simulation.playerBall.radius * 1.12f,
                center = Offset(simulation.playerBall.position.x, simulation.playerBall.position.y),
                style = Stroke(width = 2f)
            )

            // 7. Shockwaves, sparks & damage numbers
            CombatVfxRenderer.drawVfx(
                scope = this,
                particles = simulation.particles,
                shockwaves = simulation.shockwaves,
                floatingTexts = simulation.floatingTexts,
                textMeasurer = textMeasurer,
                displayFont = TacticalFontFamily,
                renderMode = engineSettings.renderMode
            )
        }
    }
}

@Composable
private fun ArenaBottomBar(
    ultimateName: String,
    hudState: BattleHudState,
    turnState: TurnState,
    palette: AppPalette,
    onTriggerUltimate: () -> Unit
) {
    val ultReady = hudState.playerUltPercent >= 100 && !hudState.isPlayerUltActive

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tactical Redirect Indicator Pill
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                if (hudState.redirectReady && turnState == TurnState.NONSTOP_CLASH) palette.blueAccent else palette.border
            ),
            modifier = Modifier
                .weight(0.95f)
                .height(54.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (hudState.redirectReady) palette.blueAccent else palette.textMuted)
                )
                Column {
                    Text(
                        text = if (turnState == TurnState.AIM_OPENING) {
                            "Aim Opening Shot"
                        } else if (hudState.redirectReady) {
                            "Redirect Ready"
                        } else {
                            "Redirect ${(hudState.redirectHalfSecsLeft / 2f)}s"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Drag court to curve",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Ultimate Awakening Button
        Button(
            onClick = onTriggerUltimate,
            enabled = ultReady,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hudState.isPlayerUltActive) palette.goldAccent else palette.primaryAccent,
                contentColor = Color(0xFFF8FAFC),
                disabledContainerColor = palette.cardSurface,
                disabledContentColor = palette.textSecondary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp),
            modifier = Modifier
                .weight(1.25f)
                .height(54.dp)
                .testTag("ultimate_awaken_button")
        ) {
            Icon(
                Icons.Default.Bolt,
                contentDescription = null,
                tint = if (ultReady || hudState.isPlayerUltActive) Color.White else palette.goldAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    hudState.isPlayerUltActive -> "AWAKENED!"
                    ultReady -> "AWAKEN: $ultimateName"
                    else -> "Ultimate ${hudState.playerUltPercent}%"
                },
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
