package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.CharacterData
import com.example.model.GameMode
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AppTheme

@Composable
fun MatchmakingScreen(
    profile: PlayerProfile,
    playerChar: CharacterData,
    opponentName: String,
    opponentChar: CharacterData,
    gameMode: GameMode,
    countdown: Int,
    canStartRankedChallenger: Boolean = false,
    onStartRankedChallenger: () -> Unit = {},
    onCancel: () -> Unit
) {
    val palette = AppTheme.palette
    BackHandler { onCancel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding()
            .padding(20.dp)
            .testTag("matchmaking_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 460.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                color = palette.elevatedSurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, palette.border)
            ) {
                Text(
                    text = "${gameMode.title} • Fight to 0 HP",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FighterVersusColumn(
                            label = profile.username,
                            character = playerChar,
                            accent = palette.blueAccent,
                            stageBg = palette.stageSurface,
                            stageBorder = palette.stageLine,
                            textPrimary = palette.textPrimary,
                            textSecondary = palette.textSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = CircleShape,
                            color = palette.primaryAccent,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "VS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }
                        }

                        FighterVersusColumn(
                            label = opponentName,
                            character = opponentChar,
                            accent = palette.primaryAccent,
                            stageBg = palette.stageSurface,
                            stageBorder = palette.stageLine,
                            textPrimary = palette.textPrimary,
                            textSecondary = palette.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (countdown < 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = palette.blueAccent
                            )
                            Text(
                                text = "Searching online players...",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                        }
                    } else {
                        Text(
                            text = "Starting in $countdown...",
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary
                        )
                    }
                }
            }

            if (countdown < 0 && canStartRankedChallenger) {
                Button(
                    onClick = onStartRankedChallenger,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.blueAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("start_ranked_challenger_button")
                ) {
                    Text(
                        text = "Fight Ranked Challenger Now",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("cancel_matchmaking_button")
            ) {
                Text("Cancel", color = palette.textSecondary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun FighterVersusColumn(
    label: String,
    character: CharacterData,
    accent: Color,
    stageBg: Color,
    stageBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(102.dp)
                .clip(CircleShape)
                .background(stageBg)
                .border(1.dp, stageBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(90.dp)) {
                AnimeBallRenderer.drawPreviewAvatar(
                    scope = this,
                    character = character,
                    weaponAngle = 0.7f
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = character.name,
            style = MaterialTheme.typography.titleLarge,
            color = textPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = character.title,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
