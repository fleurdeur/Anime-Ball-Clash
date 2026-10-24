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
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
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
    onToggleSfx: () -> Unit,
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
        // 1. Symmetric Top Fighter HUD: Scaled MaxHP + Ultimate Progress & Cooldown Bars for BOTH Player and AI
        ArenaTopHud(
            playerName = simulation.playerBall.character.name,
            opponentName = simulation.opponentBall.character.name,
            hudState = hudState,
            sfxEnabled = engineSettings.sfxEnabled,
            palette = palette,
            onToggleSfx = onToggleSfx,
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

            // Opening Launch Prompt Overlay
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
                            text = "Drag anywhere to launch • Extended nonstop clash until 0 HP",
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

        // 3. Bottom Tactical Bar (Redirect + Player Ultimate Progress/Cooldown Action Button)
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
    sfxEnabled: Boolean,
    palette: AppPalette,
    onToggleSfx: () -> Unit,
    onOpenSettings: () -> Unit,
    onSurrender: () -> Unit
) {
    Surface(
        color = palette.cardSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Player Fighter Column (HP + Ultimate Progress/Cooldown Bar)
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

                // Compact 48dp accessible controls cluster (SFX + Settings + Surrender)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleSfx,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("arena_sfx_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (sfxEnabled) Icons.AutoMirrored.Outlined.VolumeUp else Icons.AutoMirrored.Outlined.VolumeOff,
                            contentDescription = if (sfxEnabled) "Mute Sound Effects" else "Enable Sound Effects",
                            tint = if (sfxEnabled) palette.blueAccent else palette.textMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }
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
                            modifier = Modifier.size(19.dp)
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
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // AI Opponent Fighter Column (HP + AI Ultimate Progress/Cooldown Bar)
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
        isUltActive -> "AWAKEN ${ultActiveHalfSecs / 2f}s"
        isOnCooldown -> "CD ${ultCooldownHalfSecs / 2f}s"
        isReady -> "ULT READY!"
        else -> "ULT $ultPercent%"
    }

    val hpRatio = (hp.toFloat() / maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!alignRight) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$hp",
                    style = MaterialTheme.typography.labelLarge,
                    color = hpColor
                )
            } else {
                Text(
                    text = "$hp",
                    style = MaterialTheme.typography.labelLarge,
                    color = hpColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { hpRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(50)),
            color = hpColor,
            trackColor = palette.elevatedSurface
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Symmetric Ultimate Progress / Active / Cooldown Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTagPrefix}_ult_bar_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!alignRight) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = ultBarColor,
                    modifier = Modifier.size(12.dp)
                )
                LinearProgressIndicator(
                    progress = { ultProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
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
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = ultBarColor,
                    trackColor = palette.elevatedSurface
                )
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = ultBarColor,
                    modifier = Modifier.size(12.dp)
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

            CombatVfxRenderer.drawFloorHazards(this, simulation.fireHazards, simulation.globalTime)

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
                color = Color(0xFF3CC27B).copy(alpha = 0.55f),
                radius = simulation.playerBall.radius * 1.12f,
                center = Offset(simulation.playerBall.position.x, simulation.playerBall.position.y),
                style = Stroke(width = 2f)
            )

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
    val isPlayerOnCd = hudState.playerUltCooldownHalfSecs > 0
    val ultReady = hudState.playerUltPercent >= 100 && !hudState.isPlayerUltActive && !isPlayerOnCd

    val bottomProgress = when {
        hudState.isPlayerUltActive -> ((hudState.playerUltActiveHalfSecs / 2f) / hudState.playerUltMaxDurationSec.coerceAtLeast(1f)).coerceIn(0f, 1f)
        isPlayerOnCd -> ((hudState.playerUltCooldownHalfSecs / 2f) / hudState.playerUltMaxCooldownSec.coerceAtLeast(1f)).coerceIn(0f, 1f)
        else -> (hudState.playerUltPercent / 100f).coerceIn(0f, 1f)
    }

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
                .height(56.dp)
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

        // Player Ultimate Awakening Button with embedded Progress & Cooldown Bar
        Surface(
            onClick = { if (ultReady) onTriggerUltimate() },
            enabled = ultReady,
            shape = RoundedCornerShape(16.dp),
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
                .weight(1.30f)
                .height(56.dp)
                .testTag("ultimate_awaken_button")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
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
                            isPlayerOnCd -> palette.textMuted
                            else -> palette.goldAccent
                        },
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when {
                            hudState.isPlayerUltActive -> "AWAKENED • ${hudState.playerUltActiveHalfSecs / 2f}s"
                            isPlayerOnCd -> "COOLDOWN • ${hudState.playerUltCooldownHalfSecs / 2f}s"
                            ultReady -> "AWAKEN: $ultimateName"
                            else -> "Ultimate ${hudState.playerUltPercent}%"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            ultReady -> Color.White
                            hudState.isPlayerUltActive -> palette.goldAccent
                            isPlayerOnCd -> palette.textSecondary
                            else -> palette.textPrimary
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                LinearProgressIndicator(
                    progress = { bottomProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = when {
                        ultReady -> Color.White
                        hudState.isPlayerUltActive -> palette.goldAccent
                        isPlayerOnCd -> palette.textMuted
                        else -> palette.blueAccent
                    },
                    trackColor = if (ultReady) Color.White.copy(alpha = 0.28f) else palette.elevatedSurface
                )
            }
        }
    }
}
