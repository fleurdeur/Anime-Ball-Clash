package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CharacterRoster
import com.example.model.MatchStatistics
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AppTheme

@Composable
fun ResultScreen(
    stats: MatchStatistics,
    profile: PlayerProfile,
    onRematch: () -> Unit,
    onCharacterSelect: () -> Unit,
    onHomeHub: () -> Unit
) {
    val palette = AppTheme.palette
    BackHandler { onHomeHub() }

    val playerChar = remember(stats.playerCharacterId) {
        CharacterRoster.getById(stats.playerCharacterId)
    }

    val bannerBg = if (stats.isPlayerWinner) palette.greenAccentSoft else palette.primaryAccentSoft
    val bannerFg = if (stats.isPlayerWinner) palette.greenAccent else palette.primaryAccent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("result_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = bannerBg,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, bannerFg.copy(alpha = 0.35f))
            ) {
                Text(
                    text = if (stats.isPlayerWinner) "KNOCKOUT VICTORY" else "DEFEATED",
                    style = MaterialTheme.typography.labelLarge,
                    color = bannerFg,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(palette.stageSurface),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(104.dp)) {
                    AnimeBallRenderer.drawPreviewAvatar(
                        scope = this,
                        character = playerChar,
                        weaponAngle = 0.72f,
                        isUltimatePreview = stats.isPlayerWinner
                    )
                }
            }

            Text(
                text = "${stats.playerCharacterName} vs ${stats.opponentCharacterName}",
                style = MaterialTheme.typography.displayMedium,
                color = palette.textPrimary
            )
            Text(
                text = "Final HP: ${stats.playerFinalHp} – ${stats.opponentFinalHp} • ${stats.matchDurationSeconds}s clash",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Match Rewards", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RewardChip("+${stats.goldEarned} Gold", palette.goldAccentSoft, palette.goldAccent, Modifier.weight(1f))
                        RewardChip("+${stats.gemsEarned} Gems", palette.blueAccentSoft, palette.blueAccent, Modifier.weight(1f))
                        RewardChip("+${stats.expEarned} XP", palette.elevatedSurface, palette.textPrimary, Modifier.weight(1f))
                    }
                    Text(
                        text = "Balance: ${profile.gold} Gold • Lv.${profile.level} (${profile.exp}/${profile.maxExp} XP)",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Combat Breakdown", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                    MetricRow("Damage Dealt", "${stats.damageDealt} HP")
                    HorizontalDivider(color = palette.border)
                    MetricRow("Damage Taken", "${stats.damageTaken} HP")
                    HorizontalDivider(color = palette.border)
                    MetricRow("Wall Ricochets", "${stats.wallBounces}")
                    HorizontalDivider(color = palette.border)
                    MetricRow("Max Bank Chain", "${stats.maxCombo}x")
                    HorizontalDivider(color = palette.border)
                    MetricRow("Ultimates Awakened", "${stats.ultimatesUsed}")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onHomeHub,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("return_home_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = palette.textPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Arena", color = palette.textPrimary, style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = onRematch,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primaryAccent, contentColor = Color.White),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(52.dp)
                        .testTag("rematch_button")
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rematch", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }

            OutlinedButton(
                onClick = onCharacterSelect,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("result_fighters_button")
            ) {
                Text("Switch or Recruit Fighters", color = palette.textPrimary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun RewardChip(label: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Surface(color = bg, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    val palette = AppTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
    }
}
