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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.AppThemeMode
import com.example.model.CharacterRoster
import com.example.model.EngineSettings
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import com.example.model.TargetFPS
import com.example.ui.theme.AppTheme

@Composable
fun GraphicsConfigDialog(
    currentSettings: EngineSettings,
    selectedCharacterId: String = "kakarot_z",
    onSaveSettings: (EngineSettings) -> Unit,
    onResetAccount: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val palette = AppTheme.palette

    var selectedThemeMode by remember(currentSettings.themeMode) { mutableStateOf(currentSettings.themeMode) }
    var selectedRenderMode by remember { mutableStateOf(currentSettings.renderMode) }
    var selectedPhysicsMesh by remember { mutableStateOf(currentSettings.physicsMesh) }
    var selectedFps by remember { mutableStateOf(currentSettings.targetFps) }
    var hapticsEnabled by remember { mutableStateOf(currentSettings.hapticsEnabled) }
    var screenShake by remember { mutableStateOf(currentSettings.screenShake) }
    var confirmReset by remember { mutableStateOf(false) }

    val previewChar = remember(selectedCharacterId) { CharacterRoster.getById(selectedCharacterId) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("graphics_settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = palette.cardSurface,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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

                // 1. Appearance: Dark / Light / System (applies live immediately!)
                Text(
                    text = "Appearance",
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
                                onSaveSettings(
                                    currentSettings.copy(
                                        themeMode = mode,
                                        renderMode = selectedRenderMode,
                                        physicsMesh = selectedPhysicsMesh,
                                        targetFps = selectedFps,
                                        hapticsEnabled = hapticsEnabled,
                                        screenShake = screenShake
                                    )
                                )
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

                // Fighter Preview Swatch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(98.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.stageSurface)
                        .border(1.dp, palette.border, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(116.dp, 90.dp)) {
                        AnimeBallRenderer.drawPreviewAvatar(
                            scope = this,
                            character = previewChar,
                            weaponAngle = 0.7f
                        )
                    }
                }

                // 2. Graphics Preset
                Text("Graphics Preset", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RenderMode.entries.forEach { mode ->
                        val active = selectedRenderMode == mode
                        Surface(
                            onClick = { selectedRenderMode = mode },
                            color = if (active) palette.blueAccent else palette.elevatedSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (active) palette.blueAccent else palette.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) Color.White else palette.textPrimary
                                )
                            }
                        }
                    }
                }

                // 3. Sphere Deformation
                Text("Impact Elasticity", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsMesh.entries.forEach { mesh ->
                        val active = selectedPhysicsMesh == mesh
                        Surface(
                            onClick = { selectedPhysicsMesh = mesh },
                            color = if (active) palette.blueAccent else palette.elevatedSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (active) palette.blueAccent else palette.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
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

                // 4. Frame Rate Target
                Text("Frame Rate Target", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetFPS.entries.forEach { fps ->
                        val active = selectedFps == fps
                        Surface(
                            onClick = { selectedFps = fps },
                            color = if (active) palette.primaryAccent else palette.elevatedSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (active) palette.primaryAccent else palette.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
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
                    Text("Collision Haptics", style = MaterialTheme.typography.bodyLarge, color = palette.textPrimary)
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { hapticsEnabled = it },
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
                    Text("Impact Screen Shake", style = MaterialTheme.typography.bodyLarge, color = palette.textPrimary)
                    Switch(
                        checked = screenShake,
                        onCheckedChange = { screenShake = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = palette.primaryAccent
                        )
                    )
                }

                if (onResetAccount != null) {
                    HorizontalDivider(color = palette.border)
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
                            text = if (confirmReset) "Tap Again to Wipe Progress" else "Reset Account to Fresh Zero",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Button(
                    onClick = {
                        onSaveSettings(
                            EngineSettings(
                                themeMode = selectedThemeMode,
                                renderMode = selectedRenderMode,
                                physicsMesh = selectedPhysicsMesh,
                                targetFps = selectedFps,
                                sfxEnabled = currentSettings.sfxEnabled,
                                hapticsEnabled = hapticsEnabled,
                                bloomPostFx = selectedRenderMode == RenderMode.REALISTIC,
                                screenShake = screenShake
                            )
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("apply_engine_settings_button")
                ) {
                    Text(
                        text = "Apply & Close",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
