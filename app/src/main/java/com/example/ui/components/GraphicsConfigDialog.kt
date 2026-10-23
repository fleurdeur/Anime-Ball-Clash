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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.CharacterRoster
import com.example.model.CombatBall
import com.example.model.EngineSettings
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import com.example.model.TargetFPS
import com.example.model.Vector2
import com.example.model.VerletHairNode
import com.example.ui.theme.AwakeningGold
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyberVoltCyan
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.GunmetalSurface
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarFlareOrange

@Composable
fun GraphicsConfigDialog(
    currentSettings: EngineSettings,
    onSaveSettings: (EngineSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRenderMode by remember { mutableStateOf(currentSettings.renderMode) }
    var selectedPhysicsMesh by remember { mutableStateOf(currentSettings.physicsMesh) }
    var selectedFps by remember { mutableStateOf(currentSettings.targetFps) }
    var sfxEnabled by remember { mutableStateOf(currentSettings.sfxEnabled) }
    var hapticsEnabled by remember { mutableStateOf(currentSettings.hapticsEnabled) }
    var screenShake by remember { mutableStateOf(currentSettings.screenShake) }

    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                animTime += ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = nanos
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("graphics_settings_dialog"),
            shape = CutCornerShape(16.dp),
            color = GunmetalSurface,
            border = BorderStroke(1.5.dp, HairlineSteel)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyberVoltCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENGINE CONFIGURATOR",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CrispWhite
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedSilver)
                    }
                }

                // Live Preview Swatch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                        .clip(CutCornerShape(10.dp))
                        .background(ObsidianVoid)
                        .border(1.dp, HairlineSteel, CutCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(150.dp, 110.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f + 6f
                        val r = size.minDimension * 0.30f
                        val ball = CombatBall(
                            id = "cfg_preview",
                            name = "Preview",
                            position = Vector2(cx, cy),
                            velocity = Vector2.Zero,
                            radius = r,
                            character = CharacterRoster.getById("saiyan_spirit"),
                            weaponAngle = animTime * 3f,
                            hairNodes = if (selectedPhysicsMesh == PhysicsMesh.DYNAMIC) {
                                List(5) { idx ->
                                    VerletHairNode(cx, cy - r * 0.5f - idx * (r * 0.28f), cx, cy - r * 0.5f - idx * (r * 0.28f))
                                }
                            } else emptyList()
                        )
                        AnimeBallRenderer.drawBall(
                            scope = this,
                            ball = ball,
                            renderMode = selectedRenderMode,
                            physicsMesh = selectedPhysicsMesh,
                            globalTime = animTime
                        )
                    }
                }

                // 1. Render Tier
                Text("RENDER PIPELINE", style = MaterialTheme.typography.labelMedium, color = CyberVoltCyan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RenderMode.values().forEach { mode ->
                        val active = selectedRenderMode == mode
                        Surface(
                            onClick = { selectedRenderMode = mode },
                            color = if (active) CyberVoltCyan else ElevatedPanel,
                            shape = CutCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (active) CyberVoltCyan else HairlineSteel),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (active) ObsidianVoid else CrispWhite
                                )
                            }
                        }
                    }
                }

                // 2. Physics Mesh
                Text("DEFORMATION & HAIR MESH", style = MaterialTheme.typography.labelMedium, color = AwakeningGold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsMesh.values().forEach { mesh ->
                        val active = selectedPhysicsMesh == mesh
                        Surface(
                            onClick = { selectedPhysicsMesh = mesh },
                            color = if (active) AwakeningGold else ElevatedPanel,
                            shape = CutCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (active) AwakeningGold else HairlineSteel),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = mesh.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) ObsidianVoid else CrispWhite
                                )
                            }
                        }
                    }
                }

                // 3. Frame Target Chips
                Text("TARGET REFRESH RATE", style = MaterialTheme.typography.labelMedium, color = SolarFlareOrange)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetFPS.values().forEach { fps ->
                        val active = selectedFps == fps
                        Surface(
                            onClick = { selectedFps = fps },
                            color = if (active) SolarFlareOrange else ElevatedPanel,
                            shape = CutCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (active) SolarFlareOrange else HairlineSteel),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${fps.fps}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CrispWhite
                                )
                            }
                        }
                    }
                }

                // Haptics & Screen Shake Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("IMPACT HAPTICS", style = MaterialTheme.typography.bodyMedium, color = CrispWhite)
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { hapticsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberVoltCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ARENA SCREEN SHAKE", style = MaterialTheme.typography.bodyMedium, color = CrispWhite)
                    Switch(
                        checked = screenShake,
                        onCheckedChange = { screenShake = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberVoltCyan)
                    )
                }

                Button(
                    onClick = {
                        onSaveSettings(
                            EngineSettings(
                                renderMode = selectedRenderMode,
                                physicsMesh = selectedPhysicsMesh,
                                targetFps = selectedFps,
                                sfxEnabled = sfxEnabled,
                                hapticsEnabled = hapticsEnabled,
                                bloomPostFx = selectedRenderMode == RenderMode.REALISTIC,
                                screenShake = screenShake
                            )
                        )
                        onDismiss()
                    },
                    shape = CutCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberVoltCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("apply_engine_settings_button")
                ) {
                    Text(
                        text = "APPLY & SAVE",
                        style = MaterialTheme.typography.labelLarge,
                        color = ObsidianVoid
                    )
                }
            }
        }
    }
}
