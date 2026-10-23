package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.CharacterRoster
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadComplete: () -> Unit
) {
    val palette = AppTheme.palette
    var targetProgress by remember { mutableFloatStateOf(0.18f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "boot_progress"
    )
    val starterHero = remember { CharacterRoster.getById("kakarot_z") }

    LaunchedEffect(Unit) {
        targetProgress = 1.0f
        delay(780)
        onLoadComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding()
            .padding(24.dp)
            .testTag("loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 380.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = palette.stageSurface,
                border = BorderStroke(1.5.dp, palette.border),
                modifier = Modifier.size(132.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    AnimeBallRenderer.drawPreviewAvatar(
                        scope = this,
                        character = starterHero,
                        weaponAngle = 0.72f
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                color = palette.primaryAccentSoft,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "KINETIC 1V1 ARENA",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.primaryAccent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Anime Ball Clash",
                style = MaterialTheme.typography.displayLarge,
                color = palette.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "20 Anime Fighters • Nonstop Ricochet Combat",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50)),
                color = palette.primaryAccent,
                trackColor = palette.elevatedSurface
            )
        }
    }
}
