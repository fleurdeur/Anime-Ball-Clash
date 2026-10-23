package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BattleSimulation
import com.example.model.EngineSettings
import com.example.model.TurnState
import com.example.model.Vector2
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.CombatVfxRenderer
import com.example.ui.components.GraphicsConfigDialog
import com.example.ui.components.TrajectoryPredictor
import com.example.ui.theme.AwakeningGold
import com.example.ui.theme.CrimsonFinisher
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyberVoltCyan
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.EmeraldVitality
import com.example.ui.theme.GunmetalSurface
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RussoOneFontFamily
import com.example.ui.theme.SlateAbyss
import com.example.ui.theme.SolarFlareOrange
import com.example.viewmodel.BattleHudState
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun BattleArenaScreen(
    simulation: BattleSimulation,
    hudState: BattleHudState,
    turnState: TurnState,
    matchTimerSeconds: Int,
    engineSettings: EngineSettings,
    onStepFrame: (Float) -> Unit,
    onPlayerLaunch: (Vector2, Float) -> Unit,
    onTriggerUltimate: () -> Unit,
    onSurrender: () -> Unit,
    onSaveEngineSettings: (EngineSettings) -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    var showSettingsInBattle by remember { mutableStateOf(false) }

    // Frame counter read inside Canvas to guarantee smooth VSYNC redraws
    var frameTick by remember { mutableLongStateOf(0L) }

    // Drag-anywhere slingshot state
    var isDragging by remember { mutableStateOf(false) }
    var dragOrigin by remember { mutableStateOf(Vector2.Zero) }
    var currentDragVector by remember { mutableStateOf(Vector2.Zero) }

    val latestTurnState by rememberUpdatedState(turnState)
    val latestOnPlayerLaunch by rememberUpdatedState(onPlayerLaunch)

    val textMeasurer = rememberTextMeasurer()

    BackHandler { isPaused = true }

    // Hardware VSYNC Simulation Loop
    LaunchedEffect(simulation, isPaused, engineSettings.targetFps) {
        var lastNanos = 0L
        val minFrameNanos = 1_000_000_000L / engineSettings.targetFps.fps
        var accumNanos = 0L

        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                }
                val elapsed = nanos - lastNanos
                lastNanos = nanos
                accumNanos += elapsed

                if (!isPaused && accumNanos >= (minFrameNanos * 85L) / 100L) {
                    val dt = (accumNanos / 1_000_000_000f).coerceIn(0.004f, 0.04f)
                    accumNanos = 0L
                    onStepFrame(dt)
                    frameTick++
                }
            }
        }
    }

    val playerBall = simulation.playerBall
    val opponentBall = simulation.opponentBall

    val shakeOffset = if (simulation.screenShakeAmount > 0.5f && engineSettings.screenShake) {
        val amp = simulation.screenShakeAmount
        IntOffset(
            x = ((Random.nextFloat() - 0.5f) * amp).roundToInt(),
            y = ((Random.nextFloat() - 0.5f) * amp).roundToInt()
        )
    } else {
        IntOffset.Zero
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .offset { shakeOffset }
            .testTag("battle_arena_screen")
    ) {
        // 1. ARCADE FIGHTER TOP HUD
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SlateAbyss,
            border = BorderStroke(1.dp, HairlineSteel)
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PLAYER FIGHTER PLATE (Left)
                    FighterHealthCluster(
                        name = playerBall.name,
                        character = playerBall.character,
                        hp = hudState.playerHp,
                        delayedHp = hudState.playerDelayedHp,
                        status = hudState.playerStatusLabel,
                        barGradient = listOf(EmeraldVitality, CyberVoltCyan),
                        isMirrored = false,
                        modifier = Modifier.weight(1f)
                    )

                    // CENTER CLOCK + PAUSE
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        val mins = matchTimerSeconds / 60
                        val secs = matchTimerSeconds % 60
                        Surface(
                            color = GunmetalSurface,
                            shape = CutCornerShape(6.dp),
                            border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                style = MaterialTheme.typography.titleLarge,
                                color = AwakeningGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(
                            onClick = { isPaused = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("pause_battle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause Match",
                                tint = MutedSilver,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // AI OPPONENT FIGHTER PLATE (Right)
                    FighterHealthCluster(
                        name = opponentBall.name,
                        character = opponentBall.character,
                        hp = hudState.opponentHp,
                        delayedHp = hudState.opponentDelayedHp,
                        status = hudState.opponentStatusLabel,
                        barGradient = listOf(SolarFlareOrange, CrimsonFinisher),
                        isMirrored = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // TACTICAL TURN PHASE STRIP
                val (bannerText, bannerAccent) = when (turnState) {
                    TurnState.PLAYER_AIMING -> {
                        if (isDragging) {
                            val maxPull = min(simulation.arenaWidth, simulation.arenaHeight) * 0.34f
                            val pct = ((currentDragVector.length() / maxPull.coerceAtLeast(1f)).coerceIn(0f, 1f) * 100).toInt()
                            "LAUNCH POWER: $pct% — RELEASE TO STRIKE!" to AwakeningGold
                        } else {
                            "YOUR TURN — DRAG ANYWHERE TO SLINGSHOT" to CyberVoltCyan
                        }
                    }
                    TurnState.PLAYER_MOVING -> "PLAYER STRIKE ACTIVE — WALL RICOCHETS +2 DMG" to SolarFlareOrange
                    TurnState.AI_AIMING -> "RIVAL LOCKING ON — COUNTER-SHOT IMMINENT!" to CrimsonFinisher
                    TurnState.AI_MOVING -> "RIVAL COUNTER-STRIKE IN MOTION!" to SolarFlareOrange
                    TurnState.KNOCKOUT_SLOWMO -> "K.O. FINISH — ARENA DECIDED!" to AwakeningGold
                }

                val animatedAccent by animateColorAsState(bannerAccent, label = "turnBanner")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(4.dp))
                        .background(animatedAccent.copy(alpha = 0.14f))
                        .border(1.dp, animatedAccent.copy(alpha = 0.55f), CutCornerShape(4.dp))
                        .padding(vertical = 4.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bannerText,
                        style = MaterialTheme.typography.labelMedium,
                        color = animatedAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 2. ARENA PLAYFIELD (SACRED SEAL FLOOR + REACTIVE BOUNCE RAILS + VSYNC CANVAS)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.5.dp, HairlineSteel, RoundedCornerShape(18.dp))
                .onSizeChanged { sz ->
                    if (sz.width > 0 && sz.height > 0) {
                        simulation.configureArenaBounds(sz.width.toFloat(), sz.height.toFloat())
                    }
                }
        ) {
            // Illustrated Top-Down Tournament Seal Arena Floor
            Image(
                painter = painterResource(id = R.drawable.img_arena_floor_1792790796586),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle dark contrast glaze so combat spheres & trajectories pop cleanly
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianVoid.copy(alpha = 0.42f))
            )

            // Interactive Physics & VFX Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("arena_physics_canvas")
                    .pointerInput(simulation) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (latestTurnState == TurnState.PLAYER_AIMING && simulation.isRoundActive) {
                                    isDragging = true
                                    dragOrigin = Vector2(offset.x, offset.y)
                                    currentDragVector = Vector2.Zero
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (isDragging) {
                                    change.consume()
                                    currentDragVector = currentDragVector + Vector2(dragAmount.x, dragAmount.y)
                                }
                            },
                            onDragEnd = {
                                if (isDragging) {
                                    val maxPull = min(size.width, size.height) * 0.34f
                                    if (currentDragVector.length() >= 16f && latestTurnState == TurnState.PLAYER_AIMING) {
                                        latestOnPlayerLaunch(currentDragVector, maxPull)
                                    }
                                    isDragging = false
                                    currentDragVector = Vector2.Zero
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                currentDragVector = Vector2.Zero
                            }
                        )
                    }
            ) {
                // Observe VSYNC tick so DrawScope redraws every frame!
                val _tick = frameTick
                if (!simulation.isArenaInitialized) {
                    simulation.configureArenaBounds(size.width, size.height)
                }

                val pad = simulation.wallPadding
                val maxPullPx = min(size.width, size.height) * 0.34f

                // Reactive Glowing Ricochet Rails
                drawRoundRect(
                    color = CyberVoltCyan.copy(alpha = 0.32f),
                    topLeft = Offset(pad, pad),
                    size = Size(size.width - pad * 2f, size.height - pad * 2f),
                    cornerRadius = CornerRadius(24f, 24f),
                    style = Stroke(width = 4f)
                )

                // Wall impact flash highlights
                if (simulation.wallFlashLeft > 0f) {
                    drawLine(
                        color = AwakeningGold.copy(alpha = simulation.wallFlashLeft),
                        start = Offset(pad, pad),
                        end = Offset(pad, size.height - pad),
                        strokeWidth = 9f
                    )
                }
                if (simulation.wallFlashRight > 0f) {
                    drawLine(
                        color = AwakeningGold.copy(alpha = simulation.wallFlashRight),
                        start = Offset(size.width - pad, pad),
                        end = Offset(size.width - pad, size.height - pad),
                        strokeWidth = 9f
                    )
                }
                if (simulation.wallFlashTop > 0f) {
                    drawLine(
                        color = AwakeningGold.copy(alpha = simulation.wallFlashTop),
                        start = Offset(pad, pad),
                        end = Offset(size.width - pad, pad),
                        strokeWidth = 9f
                    )
                }
                if (simulation.wallFlashBottom > 0f) {
                    drawLine(
                        color = AwakeningGold.copy(alpha = simulation.wallFlashBottom),
                        start = Offset(pad, size.height - pad),
                        end = Offset(size.width - pad, size.height - pad),
                        strokeWidth = 9f
                    )
                }

                // Floor Fire Hazards (drawn beneath spheres)
                CombatVfxRenderer.drawFloorHazards(
                    scope = this,
                    hazards = simulation.fireHazards,
                    globalTime = simulation.globalTime
                )

                // Player Ready Reticle / Slingshot Trajectory
                if (turnState == TurnState.PLAYER_AIMING && simulation.isRoundActive) {
                    if (isDragging && currentDragVector.length() >= 16f) {
                        TrajectoryPredictor.drawAimTrajectory(
                            scope = this,
                            ballPosition = playerBall.position,
                            dragVector = currentDragVector,
                            maxPullPx = maxPullPx,
                            wallLeft = simulation.wallLeft,
                            wallRight = simulation.wallRight,
                            wallTop = simulation.wallTop,
                            wallBottom = simulation.wallBottom,
                            ballRadius = playerBall.radius
                        )
                    } else {
                        TrajectoryPredictor.drawReadyReticle(
                            scope = this,
                            ballPosition = playerBall.position,
                            ballRadius = playerBall.radius,
                            globalTime = simulation.globalTime
                        )
                    }
                }

                // AI Opponent Aiming Telegraph Laser!
                if (simulation.isAiAimingVisible && turnState == TurnState.AI_AIMING) {
                    TrajectoryPredictor.drawAiTelegraph(
                        scope = this,
                        aiPosition = opponentBall.position,
                        aiDirection = simulation.aiAimDirection,
                        chargeProgress = simulation.aiAimCharge,
                        wallLeft = simulation.wallLeft,
                        wallRight = simulation.wallRight,
                        wallTop = simulation.wallTop,
                        wallBottom = simulation.wallBottom,
                        ballRadius = opponentBall.radius
                    )
                }

                // Draw Opponent Sphere
                AnimeBallRenderer.drawBall(
                    scope = this,
                    ball = opponentBall,
                    renderMode = engineSettings.renderMode,
                    physicsMesh = engineSettings.physicsMesh,
                    globalTime = simulation.globalTime
                )

                // Draw Player Sphere
                AnimeBallRenderer.drawBall(
                    scope = this,
                    ball = playerBall,
                    renderMode = engineSettings.renderMode,
                    physicsMesh = engineSettings.physicsMesh,
                    globalTime = simulation.globalTime
                )

                // Shockwaves, Sparks & Floating Combat Text
                CombatVfxRenderer.drawVfx(
                    scope = this,
                    particles = simulation.particles,
                    shockwaves = simulation.shockwaves,
                    floatingTexts = simulation.floatingTexts,
                    textMeasurer = textMeasurer,
                    displayFont = RussoOneFontFamily,
                    renderMode = engineSettings.renderMode
                )
            }
        }

        // 3. BOTTOM AWAKENING COMMAND BAR
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SlateAbyss,
            border = BorderStroke(1.dp, HairlineSteel)
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val ultPct = hudState.playerUltGauge
                val isUltReady = ultPct >= 100f && !hudState.isPlayerUltActive
                val isUltActive = hudState.isPlayerUltActive

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUltActive) {
                                "${playerBall.character.ultimateName.uppercase()} ACTIVE"
                            } else {
                                "ULTIMATE: ${playerBall.character.ultimateName.uppercase()}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isUltReady || isUltActive) AwakeningGold else CrispWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isUltActive) {
                                String.format("%.1fs", hudState.playerUltRemainingSec)
                            } else {
                                "${ultPct.toInt()}%"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isUltReady) AwakeningGold else CyberVoltCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val fillFraction = if (isUltActive) {
                        (hudState.playerUltRemainingSec / playerBall.character.ultimateDurationSeconds).coerceIn(0f, 1f)
                    } else {
                        (ultPct / 100f).coerceIn(0f, 1f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CutCornerShape(4.dp))
                            .background(ElevatedPanel)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fillFraction)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        if (isUltReady || isUltActive) {
                                            listOf(SolarFlareOrange, AwakeningGold)
                                        } else {
                                            listOf(CyberVoltCyan, EmeraldVitality)
                                        }
                                    )
                                )
                        )
                    }
                }

                Button(
                    onClick = onTriggerUltimate,
                    enabled = isUltReady,
                    shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonFinisher,
                        disabledContainerColor = ElevatedPanel
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (isUltReady) AwakeningGold else HairlineSteel
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("trigger_ultimate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isUltReady) AwakeningGold else MutedSilver,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            isUltActive -> "AWAKENED"
                            isUltReady -> "AWAKEN"
                            else -> "CHARGE"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isUltReady) CrispWhite else MutedSilver
                    )
                }
            }
        }
    }

    if (isPaused) {
        AlertDialog(
            onDismissRequest = { isPaused = false },
            containerColor = GunmetalSurface,
            title = {
                Text(
                    text = "TACTICAL PAUSE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CrispWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${playerBall.character.name} vs ${opponentBall.character.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSilver
                    )
                    Button(
                        onClick = { showSettingsInBattle = true },
                        shape = CutCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElevatedPanel),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyberVoltCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GRAPHICS & PHYSICS ENGINE", style = MaterialTheme.typography.labelLarge, color = CyberVoltCyan)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isPaused = false },
                    shape = CutCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberVoltCyan),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("RESUME", style = MaterialTheme.typography.labelLarge, color = ObsidianVoid)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onSurrender,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("FORFEIT MATCH", style = MaterialTheme.typography.labelLarge, color = CrimsonFinisher)
                }
            }
        )
    }

    if (showSettingsInBattle) {
        GraphicsConfigDialog(
            currentSettings = engineSettings,
            onSaveSettings = onSaveEngineSettings,
            onDismiss = { showSettingsInBattle = false }
        )
    }
}

@Composable
private fun FighterHealthCluster(
    name: String,
    character: com.example.model.CharacterData,
    hp: Int,
    delayedHp: Float,
    status: String,
    barGradient: List<Color>,
    isMirrored: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isMirrored) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isMirrored) Arrangement.End else Arrangement.Start
        ) {
            if (!isMirrored) {
                MiniSphereBadge(character)
                Spacer(modifier = Modifier.width(6.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = if (isMirrored) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = name.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = CrispWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$hp HP",
                    style = MaterialTheme.typography.titleLarge,
                    color = barGradient.first(),
                    fontSize = 14.sp
                )
            }

            if (isMirrored) {
                Spacer(modifier = Modifier.width(6.dp))
                MiniSphereBadge(character)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dual-layer Fighting Game Health Bar (Delayed Ghost Bar + Instant HP Bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.dp)
                .clip(CutCornerShape(4.dp))
                .background(ElevatedPanel)
                .border(1.dp, HairlineSteel, CutCornerShape(4.dp)),
            contentAlignment = if (isMirrored) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            // Delayed white/amber damage trail
            Box(
                modifier = Modifier
                    .fillMaxWidth((delayedHp / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(AwakeningGold.copy(alpha = 0.65f))
            )
            // Actual HP
            Box(
                modifier = Modifier
                    .fillMaxWidth((hp / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(barGradient))
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                status.startsWith("AWAKENED") -> AwakeningGold
                status.startsWith("BLEED") -> CrimsonFinisher
                status.startsWith("STUN") -> CyberVoltCyan
                else -> MutedSilver
            },
            maxLines = 1
        )
    }
}

@Composable
private fun MiniSphereBadge(character: com.example.model.CharacterData) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(ObsidianVoid)
            .border(1.5.dp, character.auraColor, CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            AnimeBallRenderer.drawPreviewAvatar(this, character, 0f)
        }
    }
}
