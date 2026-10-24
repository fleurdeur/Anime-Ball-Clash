package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CharacterRoster
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AppTheme

@Composable
fun SignInScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val palette = AppTheme.palette
    val kakarot = remember { CharacterRoster.getById("kakarot_z") }
    val guts = remember { CharacterRoster.getById("guts_berserk") }
    val satoru = remember { CharacterRoster.getById("satoru_void") }

    val transition = rememberInfiniteTransition(label = "signin_showcase")
    val spinAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weapon_spin"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.primaryAccent.copy(alpha = if (palette.isDark) 0.18f else 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.28f),
                        radius = size.minDimension * 0.72f
                    ),
                    radius = size.minDimension * 0.72f,
                    center = Offset(size.width * 0.5f, size.height * 0.28f)
                )
            }
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("sign_in_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = palette.primaryAccentSoft,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = palette.primaryAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ONLINE & LAN ARENA",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.primaryAccent
                        )
                    }
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(palette.cardSurface)
                        .border(1.dp, palette.border, CircleShape)
                        .testTag("sign_in_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (palette.isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Dark or Light Mode",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Hero Fighter Showcase Stage
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.stageSurface)
                            .border(1.dp, palette.stageLine, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(84.dp)) {
                                AnimeBallRenderer.drawPreviewAvatar(
                                    scope = this,
                                    character = kakarot,
                                    weaponAngle = spinAngle,
                                    isUltimatePreview = false,
                                    globalTime = spinAngle
                                )
                            }
                            Canvas(modifier = Modifier.size(112.dp)) {
                                AnimeBallRenderer.drawPreviewAvatar(
                                    scope = this,
                                    character = guts,
                                    weaponAngle = -spinAngle * 1.2f,
                                    isUltimatePreview = true,
                                    globalTime = spinAngle
                                )
                            }
                            Canvas(modifier = Modifier.size(84.dp)) {
                                AnimeBallRenderer.drawPreviewAvatar(
                                    scope = this,
                                    character = satoru,
                                    weaponAngle = spinAngle + 1.5f,
                                    isUltimatePreview = true,
                                    globalTime = spinAngle
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displayMedium,
                        color = palette.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Battle with 224 detailed anime fighters. Play Custom duels, fight friends on LAN Wi-Fi or Room Codes, and climb Online Ranked.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureBadgePill("224 Fighters", palette.blueAccentSoft, palette.blueAccent, Modifier.weight(1f))
                        FeatureBadgePill("LAN & Online", palette.goldAccentSoft, palette.goldAccent, Modifier.weight(1f))
                        FeatureBadgePill("Cloud Sync", palette.primaryAccentSoft, palette.primaryAccent, Modifier.weight(1f))
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.primaryAccentSoft,
                    border = BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_in_error_banner")
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.primaryAccent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Button(
                onClick = onSignInClick,
                enabled = !isBusy,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primaryAccent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.4.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Connecting Account...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "G",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFEA4335)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.sign_in_with_google),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Your fighters, upgrades, Gold, and Ranked RP stay saved to your Google account.",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureBadgePill(
    text: String,
    bgColor: Color,
    fgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = fgColor,
                maxLines = 1
            )
        }
    }
}
