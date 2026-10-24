package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.AbilityVfxStyle
import com.example.model.AppThemeMode
import com.example.model.CharacterData
import com.example.model.CharacterRoster
import com.example.model.CombatBall
import com.example.model.ElementTag
import com.example.model.EngineSettings
import com.example.model.FloatingCombatText
import com.example.model.Particle
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import com.example.model.ShockwaveRing
import com.example.model.TargetFPS
import com.example.model.Vector2
import com.example.ui.theme.AppPalette
import com.example.ui.theme.AppTheme
import com.example.ui.theme.TacticalFontFamily
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GraphicsConfigDialog(
    currentSettings: EngineSettings,
    selectedCharacterId: String = "kakarot_z",
    onSaveSettings: (EngineSettings) -> Unit,
    onResetAccount: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val palette = AppTheme.palette

    var selectedThemeMode by remember(currentSettings.themeMode) { mutableStateOf(currentSettings.themeMode) }
    var selectedRenderMode by remember(currentSettings.renderMode) { mutableStateOf(currentSettings.renderMode) }
    var selectedPhysicsMesh by remember(currentSettings.physicsMesh) { mutableStateOf(currentSettings.physicsMesh) }
    var selectedFps by remember(currentSettings.targetFps) { mutableStateOf(currentSettings.targetFps) }
    var sfxEnabled by remember(currentSettings.sfxEnabled) { mutableStateOf(currentSettings.sfxEnabled) }
    var hapticsEnabled by remember(currentSettings.hapticsEnabled) { mutableStateOf(currentSettings.hapticsEnabled) }
    var screenShake by remember(currentSettings.screenShake) { mutableStateOf(currentSettings.screenShake) }
    var confirmReset by remember { mutableStateOf(false) }

    var previewUltActive by remember { mutableStateOf(true) }
    var demoGutsSword by remember { mutableStateOf(false) }

    val activePreviewId = if (demoGutsSword) "guts_berserk" else selectedCharacterId
    val previewChar = remember(activePreviewId) { CharacterRoster.getById(activePreviewId) }

    fun pushLiveSettings(
        theme: AppThemeMode = selectedThemeMode,
        render: RenderMode = selectedRenderMode,
        mesh: PhysicsMesh = selectedPhysicsMesh,
        fps: TargetFPS = selectedFps,
        sfx: Boolean = sfxEnabled,
        haptics: Boolean = hapticsEnabled,
        shake: Boolean = screenShake
    ) {
        onSaveSettings(
            EngineSettings(
                themeMode = theme,
                renderMode = render,
                physicsMesh = mesh,
                targetFps = fps,
                sfxEnabled = sfx,
                hapticsEnabled = haptics,
                bloomPostFx = render.bloomGlow,
                screenShake = shake
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("graphics_settings_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = palette.cardSurface,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = palette.primaryAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.textSecondary)
                    }
                }

                // Live Animated Graphics & Physics Preview Arena
                LiveGraphicsPreviewBox(
                    character = previewChar,
                    renderMode = selectedRenderMode,
                    physicsMesh = selectedPhysicsMesh,
                    targetFps = selectedFps,
                    screenShakeEnabled = screenShake,
                    isUltPreview = previewUltActive,
                    isGutsDemo = demoGutsSword,
                    palette = palette,
                    onToggleUlt = { previewUltActive = !previewUltActive },
                    onToggleGuts = { demoGutsSword = !demoGutsSword }
                )

                // 1. Theme: Dark / Light / Auto
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val active = selectedThemeMode == mode
                        val icon = when (mode) {
                            AppThemeMode.DARK -> Icons.Outlined.DarkMode
                            AppThemeMode.LIGHT -> Icons.Outlined.LightMode
                            AppThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
                        }
                        Surface(
                            onClick = {
                                selectedThemeMode = mode
                                pushLiveSettings(theme = mode)
                            },
                            color = if (active) palette.primaryAccent else palette.elevatedSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (active) palette.primaryAccent else palette.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("theme_mode_${mode.name.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) Color.White else palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) Color.White else palette.textPrimary
                                )
                            }
                        }
                    }
                }

                // 2. Graphics Quality: Low / Medium / High / Ultra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Graphics", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                    Text(
                        text = selectedRenderMode.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.blueAccent
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RenderMode.entries.forEach { mode ->
                        val active = selectedRenderMode == mode
                        Surface(
                            onClick = {
                                selectedRenderMode = mode
                                pushLiveSettings(render = mode)
                            },
                            color = if (active) palette.blueAccent else palette.elevatedSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (active) palette.blueAccent else palette.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("render_mode_${mode.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(2.dp)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) Color.White else palette.textPrimary
                                )
                            }
                        }
                    }
                }
                Text(
                    text = selectedRenderMode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )

                // 3. Bounce Feel: Solid / Bouncy
                Text("Bounce Feel", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsMesh.entries.forEach { mesh ->
                        val active = selectedPhysicsMesh == mesh
                        Surface(
                            onClick = {
                                selectedPhysicsMesh = mesh
                                pushLiveSettings(mesh = mesh)
                            },
                            color = if (active) palette.blueAccent else palette.elevatedSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (active) palette.blueAccent else palette.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("physics_mesh_${mesh.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = mesh.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) Color.White else palette.textPrimary
                                )
                            }
                        }
                    }
                }

                // 4. Frame Rate: 30 / 60 / 90 / 120
                Text("Frame Rate", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetFPS.entries.forEach { fps ->
                        val active = selectedFps == fps
                        Surface(
                            onClick = {
                                selectedFps = fps
                                pushLiveSettings(fps = fps)
                            },
                            color = if (active) palette.primaryAccent else palette.elevatedSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (active) palette.primaryAccent else palette.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("fps_option_${fps.fps}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${fps.fps}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) Color.White else palette.textPrimary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = palette.border)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound", style = MaterialTheme.typography.bodyLarge, color = palette.textPrimary)
                    Switch(
                        checked = sfxEnabled,
                        onCheckedChange = {
                            sfxEnabled = it
                            pushLiveSettings(sfx = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = palette.primaryAccent
                        ),
                        modifier = Modifier.testTag("sfx_toggle_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration", style = MaterialTheme.typography.bodyLarge, color = palette.textPrimary)
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = {
                            hapticsEnabled = it
                            pushLiveSettings(haptics = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = palette.primaryAccent
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Screen Shake", style = MaterialTheme.typography.bodyLarge, color = palette.textPrimary)
                    Switch(
                        checked = screenShake,
                        onCheckedChange = {
                            screenShake = it
                            pushLiveSettings(shake = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = palette.primaryAccent
                        )
                    )
                }

                if (onResetAccount != null || onSignOut != null) {
                    HorizontalDivider(color = palette.border)
                }

                if (onResetAccount != null) {
                    OutlinedButton(
                        onClick = {
                            if (confirmReset) {
                                onResetAccount()
                                confirmReset = false
                                onDismiss()
                            } else {
                                confirmReset = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.primaryAccent),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (confirmReset) palette.primaryAccentSoft else Color.Transparent,
                            contentColor = palette.primaryAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_account_button")
                    ) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (confirmReset) "Tap Again to Reset" else "Reset Progress",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (onSignOut != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onSignOut()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                    }
                }

                Button(
                    onClick = {
                        pushLiveSettings()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("apply_engine_settings_button")
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private class MiniPreviewState(character: CharacterData) {
    val ball = CombatBall(
        id = "preview",
        name = character.name,
        position = Vector2(180f, 80f),
        velocity = Vector2(290f, -155f),
        radius = 28f,
        character = character
    )
    val particles = ArrayList<Particle>(64)
    val shockwaves = ArrayList<ShockwaveRing>(10)
    val emptyTexts = emptyList<FloatingCombatText>()
    var widthPx = 360f
    var heightPx = 180f
    var time = 0f
    var shake = 0f
    private var nextId = 1L

    fun syncCharacter(char: CharacterData, isUlt: Boolean) {
        if (ball.character.id != char.id) {
            ball.character = char
            ball.trailPositions.clear()
        }
        val wasUlt = ball.isUltimateActive
        ball.isUltimateActive = isUlt
        ball.weaponScale = ball.targetWeaponScale
        if (isUlt && !wasUlt) {
            ball.weaponSpinBurst = 14f
            spawnWave(ball.position.x, ball.position.y, char, RenderMode.REALISTIC)
        }
    }

    fun step(dt: Float, renderMode: RenderMode) {
        time += dt
        shake = (shake - dt * 36f).coerceAtLeast(0f)

        val pad = 16f + ball.radius
        val left = pad
        val right = (widthPx - pad).coerceAtLeast(left + 20f)
        val top = pad
        val bottom = (heightPx - pad).coerceAtLeast(top + 12f)

        ball.position = Vector2(
            ball.position.x + ball.velocity.x * dt,
            ball.position.y + ball.velocity.y * dt
        )

        var bounced = false
        var nx = 0f
        var ny = 0f
        if (ball.position.x <= left) {
            ball.position = Vector2(left, ball.position.y)
            ball.velocity = Vector2(abs(ball.velocity.x), ball.velocity.y)
            bounced = true
            nx = 1f
        } else if (ball.position.x >= right) {
            ball.position = Vector2(right, ball.position.y)
            ball.velocity = Vector2(-abs(ball.velocity.x), ball.velocity.y)
            bounced = true
            nx = -1f
        }
        if (ball.position.y <= top) {
            ball.position = Vector2(ball.position.x, top)
            ball.velocity = Vector2(ball.velocity.x, abs(ball.velocity.y))
            bounced = true
            ny = 1f
        } else if (ball.position.y >= bottom) {
            ball.position = Vector2(ball.position.x, bottom)
            ball.velocity = Vector2(ball.velocity.x, -abs(ball.velocity.y))
            bounced = true
            ny = -1f
        }

        if (bounced) {
            ball.squashFactor = 0.75f
            ball.stretchFactor = 1.22f
            ball.deformAngle = atan2(ny, nx)
            ball.weaponSpinBurst = 12f
            shake = 9f
            spawnSparks(ball.position.x - nx * ball.radius * 0.7f, ball.position.y - ny * ball.radius * 0.7f, renderMode)
            spawnWave(ball.position.x, ball.position.y, ball.character, renderMode)
        } else {
            ball.deformAngle = atan2(ball.velocity.y, ball.velocity.x)
            ball.stretchFactor += (1.10f - ball.stretchFactor) * min(1f, dt * 10f)
            ball.squashFactor += (0.92f - ball.squashFactor) * min(1f, dt * 10f)
        }

        ball.weaponSpinBurst = (ball.weaponSpinBurst - dt * 16f).coerceAtLeast(0f)
        ball.weaponAngle = (ball.weaponAngle + (5.2f + ball.weaponSpinBurst) * dt) % (2f * Math.PI.toFloat())
        ball.weaponScale += (ball.targetWeaponScale - ball.weaponScale) * min(1f, dt * 12f)

        val maxTrail = renderMode.maxTrailNodes
        if (maxTrail <= 0) {
            if (ball.trailPositions.isNotEmpty()) ball.trailPositions.clear()
        } else {
            ball.trailPositions.addFirst(ball.position)
            while (ball.trailPositions.size > maxTrail) {
                ball.trailPositions.removeLast()
            }
        }

        var i = particles.size - 1
        while (i >= 0) {
            val p = particles[i]
            p.life -= dt
            if (p.life <= 0f) {
                particles.removeAt(i)
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.vx *= 0.92f
                p.vy *= 0.92f
            }
            i--
        }

        var s = shockwaves.size - 1
        while (s >= 0) {
            val ring = shockwaves[s]
            ring.life -= dt
            if (ring.life <= 0f) shockwaves.removeAt(s)
            s--
        }
    }

    private fun spawnSparks(x: Float, y: Float, renderMode: RenderMode) {
        val count = (renderMode.particleCountOnHit / 2).coerceAtLeast(3)
        val cap = renderMode.maxParticles
        for (i in 0 until count) {
            if (particles.size >= cap && particles.isNotEmpty()) {
                particles.removeAt(0)
            }
            val a = Random.nextFloat() * 2f * Math.PI.toFloat()
            val spd = 90f + Random.nextFloat() * 190f
            particles.add(
                Particle(
                    id = nextId++,
                    x = x,
                    y = y,
                    vx = cos(a) * spd,
                    vy = sin(a) * spd,
                    color = if (i % 3 == 0) Color(0xFFFDE047) else ball.character.auraColor,
                    life = 0.30f,
                    maxLife = 0.30f,
                    size = 3.2f + Random.nextFloat() * 2.4f,
                    isPixel = renderMode == RenderMode.PIXELIZED
                )
            )
        }
    }

    private fun spawnWave(x: Float, y: Float, char: CharacterData, renderMode: RenderMode) {
        val cap = renderMode.maxShockwaves
        while (shockwaves.size >= cap && shockwaves.isNotEmpty()) {
            shockwaves.removeAt(0)
        }
        val style = when {
            ball.isUltimateActive && (char.id == "guts_berserk" || char.element == ElementTag.SLASH) -> AbilityVfxStyle.SLASH_CROSS
            ball.isUltimateActive && char.element == ElementTag.ELEC -> AbilityVfxStyle.LIGHTNING_NOVA
            ball.isUltimateActive && char.element == ElementTag.MAGIC -> AbilityVfxStyle.DOMAIN_SIGIL
            else -> AbilityVfxStyle.SHOCKWAVE
        }
        shockwaves.add(
            ShockwaveRing(
                id = nextId++,
                x = x,
                y = y,
                color = char.auraColor,
                maxRadius = ball.radius * (if (ball.isUltimateActive) 2.5f else 1.8f),
                life = 0.32f,
                maxLife = 0.32f,
                style = style
            )
        )
    }
}

@Composable
private fun LiveGraphicsPreviewBox(
    character: CharacterData,
    renderMode: RenderMode,
    physicsMesh: PhysicsMesh,
    targetFps: TargetFPS,
    screenShakeEnabled: Boolean,
    isUltPreview: Boolean,
    isGutsDemo: Boolean,
    palette: AppPalette,
    onToggleUlt: () -> Unit,
    onToggleGuts: () -> Unit
) {
    val sim = remember { MiniPreviewState(character) }
    sim.syncCharacter(character, isUltPreview)

    var frameTick by remember { mutableIntStateOf(0) }
    val currentRenderMode by rememberUpdatedState(renderMode)
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(targetFps) {
        var lastNanos = 0L
        val minFrameNanos = 1_000_000_000L / targetFps.fps
        var accumNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val delta = nanos - lastNanos
                lastNanos = nanos
                accumNanos += delta
                if (accumNanos >= (minFrameNanos * 85L) / 100L) {
                    val dt = (accumNanos / 1_000_000_000f).coerceIn(0.004f, 0.040f)
                    accumNanos = 0L
                    sim.step(dt, currentRenderMode)
                    frameTick++
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Preview • ${renderMode.title} • ${physicsMesh.title} • ${targetFps.fps} FPS",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = onToggleGuts,
                    color = if (isGutsDemo) palette.primaryAccentSoft else palette.elevatedSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isGutsDemo) palette.primaryAccent else palette.border),
                    modifier = Modifier.testTag("preview_guts_toggle")
                ) {
                    Text(
                        text = if (isGutsDemo) "Guts" else "Fighter",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGutsDemo) palette.primaryAccent else palette.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    onClick = onToggleUlt,
                    color = if (isUltPreview) palette.goldAccentSoft else palette.elevatedSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isUltPreview) palette.goldAccent else palette.border),
                    modifier = Modifier.testTag("preview_ult_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isUltPreview) palette.goldAccent else palette.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isUltPreview) "Ult ON" else "Ult OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUltPreview) palette.goldAccent else palette.textSecondary
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.stageSurface)
                .border(1.dp, palette.stageLine, RoundedCornerShape(16.dp))
                .testTag("graphics_live_preview")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { sz ->
                        if (sz.width > 40 && sz.height > 40) {
                            sim.widthPx = sz.width.toFloat()
                            sim.heightPx = sz.height.toFloat()
                            sim.ball.radius = (min(sz.width, sz.height) * 0.21f).coerceIn(20f, 32f)
                            sim.ball.position = Vector2(
                                sim.ball.position.x.coerceIn(sim.ball.radius + 16f, sz.width - sim.ball.radius - 16f),
                                sim.ball.position.y.coerceIn(sim.ball.radius + 16f, sz.height - sim.ball.radius - 16f)
                            )
                        }
                    }
            ) {
                val tick = frameTick
                if (tick < 0) return@Canvas

                val shakeAmp = if (screenShakeEnabled && sim.shake > 0.3f) sim.shake * 0.45f else 0f
                val sx = if (shakeAmp > 0f) (Random.nextFloat() * 2f - 1f) * shakeAmp else 0f
                val sy = if (shakeAmp > 0f) (Random.nextFloat() * 2f - 1f) * shakeAmp else 0f

                withTransform({ translate(sx, sy) }) {
                    val pad = 10f
                    drawRoundRect(
                        color = palette.stageLine,
                        topLeft = Offset(pad, pad),
                        size = Size(size.width - pad * 2f, size.height - pad * 2f),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 1.8f)
                    )
                    drawLine(
                        color = palette.stageLine.copy(alpha = 0.65f),
                        start = Offset(size.width * 0.5f, pad + 6f),
                        end = Offset(size.width * 0.5f, size.height - pad - 6f),
                        strokeWidth = 1.5f
                    )

                    AnimeBallRenderer.drawBall(
                        scope = this,
                        ball = sim.ball,
                        renderMode = renderMode,
                        physicsMesh = physicsMesh,
                        globalTime = sim.time
                    )

                    CombatVfxRenderer.drawVfx(
                        scope = this,
                        particles = sim.particles,
                        shockwaves = sim.shockwaves,
                        floatingTexts = sim.emptyTexts,
                        textMeasurer = textMeasurer,
                        displayFont = TacticalFontFamily,
                        renderMode = renderMode,
                        defaultTextColor = palette.damageTextDefault,
                        isDarkStage = palette.isDark
                    )
                }
            }
        }
    }
}
