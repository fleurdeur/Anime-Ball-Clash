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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CharacterRoster
import com.example.model.CombatBall
import com.example.model.InitialGameData
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import com.example.model.Vector2
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AwakeningGold
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyberVoltCyan
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.GunmetalSurface
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarFlareOrange
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadComplete: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0.18f) }
    var tipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (progress < 1.0f) {
            delay(85)
            progress = (progress + 0.055f).coerceAtMost(1.0f)
            if (progress >= 1.0f) {
                delay(180)
                onLoadComplete()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            tipIndex = (tipIndex + 1) % InitialGameData.combatTips.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loadingClash")
    val clashOffset by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clashOffset"
    )
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animTime"
    )

    val saiyan = remember { CharacterRoster.getById("saiyan_spirit") }
    val demon = remember { CharacterRoster.getById("demon_slash") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .clickable { onLoadComplete() }
            .testTag("loading_screen")
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianVoid.copy(alpha = 0.72f),
                            ObsidianVoid.copy(alpha = 0.55f),
                            ObsidianVoid.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Centerpiece Clashing Spheres + Arcade Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(220.dp, 150.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension * 0.24f

                    val leftBall = CombatBall(
                        id = "l",
                        name = "Saiyan",
                        position = Vector2(cx - r - clashOffset, cy),
                        velocity = Vector2(200f, 0f),
                        radius = r,
                        character = saiyan,
                        stretchFactor = 1.14f,
                        squashFactor = 0.88f,
                        weaponAngle = animTime
                    )
                    val rightBall = CombatBall(
                        id = "r",
                        name = "Demon",
                        position = Vector2(cx + r + clashOffset, cy),
                        velocity = Vector2(-200f, 0f),
                        radius = r,
                        character = demon,
                        stretchFactor = 1.14f,
                        squashFactor = 0.88f,
                        deformAngle = 3.14f,
                        weaponAngle = -animTime
                    )

                    AnimeBallRenderer.drawBall(this, leftBall, RenderMode.REALISTIC, PhysicsMesh.SIMPLE, animTime)
                    AnimeBallRenderer.drawBall(this, rightBall, RenderMode.REALISTIC, PhysicsMesh.SIMPLE, animTime)

                    // Center impact flash
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, AwakeningGold.copy(alpha = 0.6f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = (40f - clashOffset).coerceAtLeast(12f) * 2.2f
                        ),
                        radius = (40f - clashOffset).coerceAtLeast(12f) * 2.2f,
                        center = Offset(cx, cy)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = SolarFlareOrange.copy(alpha = 0.18f),
                    shape = CutCornerShape(6.dp),
                    border = BorderStroke(1.dp, SolarFlareOrange)
                ) {
                    Text(
                        text = "1V1 KINETIC ARENA FIGHTER",
                        style = MaterialTheme.typography.labelMedium,
                        color = AwakeningGold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ANIME BALL",
                    style = MaterialTheme.typography.displayLarge,
                    color = CrispWhite,
                    fontSize = 40.sp
                )
                Text(
                    text = "CLASH",
                    style = MaterialTheme.typography.displayLarge,
                    color = CyberVoltCyan,
                    fontSize = 44.sp
                )
            }

            // Bottom Katana Energy Loader & Combat Tip
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = GunmetalSurface.copy(alpha = 0.90f),
                    shape = CutCornerShape(10.dp),
                    border = BorderStroke(1.dp, HairlineSteel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = InitialGameData.combatTips[tipIndex],
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrispWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "INITIALIZING PHYSICS ENGINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSilver
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = AwakeningGold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CutCornerShape(4.dp))
                        .background(ElevatedPanel)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SolarFlareOrange, AwakeningGold, CyberVoltCyan)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("VER 2.0 OVERDRIVE", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                    Text("TAP ANYWHERE TO ENTER", style = MaterialTheme.typography.labelSmall, color = CyberVoltCyan)
                }
            }
        }
    }
}
