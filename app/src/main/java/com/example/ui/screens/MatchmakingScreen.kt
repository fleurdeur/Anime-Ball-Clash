package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CharacterData
import com.example.model.GameMode
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AwakeningGold
import com.example.ui.theme.CrimsonFinisher
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyberVoltCyan
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.GunmetalSurface
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarFlareOrange

@Composable
fun MatchmakingScreen(
    profile: PlayerProfile,
    playerChar: CharacterData,
    opponentName: String,
    opponentChar: CharacterData,
    gameMode: GameMode,
    countdown: Int,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vsSpin")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animTime"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .testTag("matchmaking_screen")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_stage_1792790796382),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianVoid.copy(alpha = 0.80f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = CyberVoltCyan.copy(alpha = 0.16f),
                    shape = CutCornerShape(6.dp),
                    border = BorderStroke(1.dp, CyberVoltCyan)
                ) {
                    Text(
                        text = "${gameMode.title} • ARENA LOCK",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberVoltCyan,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CLASH STARTS IN $countdown",
                    style = MaterialTheme.typography.displayMedium,
                    color = AwakeningGold
                )
            }

            // Arcade VS Face-Off Split Plate
            Surface(
                color = GunmetalSurface.copy(alpha = 0.94f),
                shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp),
                border = BorderStroke(1.5.dp, HairlineSteel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player Side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(ObsidianVoid)
                                .border(2.dp, CyberVoltCyan, CircleShape)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                AnimeBallRenderer.drawPreviewAvatar(this, playerChar, animTime)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = profile.username,
                            style = MaterialTheme.typography.titleLarge,
                            color = CrispWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = playerChar.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberVoltCyan,
                            maxLines = 1
                        )
                    }

                    // Center VS Crest
                    Surface(
                        color = CrimsonFinisher,
                        shape = CutCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, AwakeningGold),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.headlineLarge,
                            color = CrispWhite,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    // Rival Side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(ObsidianVoid)
                                .border(2.dp, SolarFlareOrange, CircleShape)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                AnimeBallRenderer.drawPreviewAvatar(this, opponentChar, -animTime)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = opponentName,
                            style = MaterialTheme.typography.titleLarge,
                            color = CrispWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = opponentChar.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarFlareOrange,
                            maxLines = 1
                        )
                    }
                }
            }

            Button(
                onClick = onCancel,
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElevatedPanel),
                border = BorderStroke(1.dp, HairlineSteel),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("cancel_matchmaking_button")
            ) {
                Text(
                    text = "ABORT MATCH",
                    style = MaterialTheme.typography.labelLarge,
                    color = MutedSilver
                )
            }
        }
    }
}
