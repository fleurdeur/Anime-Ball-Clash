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
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("battle_arena_screen"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // 1. Compact Fixed-Height VS Strip (Player HP/Ult vs Bot HP/Ult — zero mid-fight resize!)
        ArenaCompactTopHud(
            playerName = simulation.playerBall.character.name,
            opponentName = simulation.opponentBall.character.name,
            hudState = hudState,
            palette = palette
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
                onPlayerLaunch = onPlayerLaunch
            )

            // Floating compact impact toast (never shifts court layout!)
            if (hudState.lastImpactBanner.isNotEmpty()) {
                Surface(
                    color = palette.overlaySurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = hudState.lastImpactBanner,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // Compact Opening Shot Prompt
            if (turnState == TurnState.AIM_OPENING) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = palette.overlaySurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Text(
                            text = "Drag to shoot • Bounces until 0 HP",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textPrimary,
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

        // 3. Single Compact 48dp Bottom Action Row (Exit, Sound, Settings, Dash, Ultimate)
        ArenaCompactBottomBar(
            ultimateName = simulation.playerBall.character.ultimateName,
            hudState = hudState,
            turnState = turnState,
            sfxEnabled = engineSettings.sfxEnabled,
            palette = palette,
            onSurrender = onSurrender,
            onToggleSfx = onToggleSfx,
            onOpenSettings = { showSettings = true },
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
private fun ArenaCompactTopHud(
    playerName: String,
    opponentName: String,
    hudState: BattleHudState,
    palette: AppPalette
) {
    Surface(
        color = palette.cardSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.border)
            ) {
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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
        isOnCooldown -> "WAIT ${ultCooldownHalfSecs / 2f}s"
        isReady -> "READY!"
        else -> "$ultPercent%"
    }

    val hpRatio = (hp.toFloat() / maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                Spacer(modifier = Modifier.width(4.dp))
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
                Spacer(modifier = Modifier.width(4.dp))
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
    turnState: TurnState,
    sfxEnabled: Boolean,
    palette: AppPalette,
    onSurrender: () -> Unit,
    onToggleSfx: () -> Unit,
    onOpenSettings: () -> Unit,
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
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
                    contentDescription = "Exit Match",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

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

        // Compact Dash indicator pill
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (hudState.redirectReady && turnState == TurnState.NONSTOP_CLASH) palette.blueAccent else palette.border
            ),
            modifier = Modifier.height(48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (hudState.redirectReady) palette.blueAccent else palette.textMuted)
                )
                Text(
                    text = when {
                        turnState == TurnState.AIM_OPENING -> "Aim"
                        hudState.redirectReady -> "Dash"
                        else -> "${hudState.redirectHalfSecsLeft / 2f}s"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textPrimary,
                    maxLines = 1
                )
            }
        }

        // Compact Ultimate Button (48dp height)
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
                            isPlayerOnCd -> palette.textMuted
                            else -> palette.goldAccent
                        },
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = when {
                            hudState.isPlayerUltActive -> "ULT ${hudState.playerUltActiveHalfSecs / 2f}s"
                            isPlayerOnCd -> "WAIT ${hudState.playerUltCooldownHalfSecs / 2f}s"
                            ultReady -> "ULT: $ultimateName"
                            else -> "Ult ${hudState.playerUltPercent}%"
                        },
                        style = MaterialTheme.typography.labelMedium,
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
                        .height(4.dp)
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
