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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CharacterRoster
import com.example.model.MatchStatistics
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
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
import com.example.ui.theme.SolarFlareOrange

@Composable
fun ResultScreen(
    stats: MatchStatistics,
    profile: PlayerProfile,
    onRematch: () -> Unit,
    onCharacterSelect: () -> Unit,
    onHomeHub: () -> Unit
) {
    BackHandler { onHomeHub() }

    val isVictory = stats.isPlayerWinner
    val accentColor = if (isVictory) AwakeningGold else CrimsonFinisher
    val winnerChar = remember(stats) {
        CharacterRoster.getById(if (isVictory) stats.playerCharacterId else stats.opponentCharacterId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("result_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Champion Sphere Medallion
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(GunmetalSurface)
                .border(2.5.dp, accentColor, CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                AnimeBallRenderer.drawPreviewAvatar(this, winnerChar, 0.8f)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isVictory) "K.O. VICTORY!" else "ARENA DEFEAT",
                style = MaterialTheme.typography.displayMedium,
                color = accentColor
            )
            Text(
                text = "${stats.playerCharacterName} (${stats.playerFinalHp} HP) vs ${stats.opponentCharacterName} (${stats.opponentFinalHp} HP)",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSilver
            )
        }

        // Spoils & XP Panel
        Surface(
            color = GunmetalSurface,
            shape = CutCornerShape(14.dp),
            border = BorderStroke(1.dp, HairlineSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "BATTLE SPOILS",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberVoltCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RewardBadge(Icons.Default.MonetizationOn, "+${stats.goldEarned}", "GOLD", AwakeningGold)
                    RewardBadge(Icons.Default.Diamond, "+${stats.gemsEarned}", "GEMS", CyberVoltCyan)
                    RewardBadge(Icons.Default.Star, "+${stats.expEarned}", "EXP", EmeraldVitality)
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LEVEL ${profile.level}", style = MaterialTheme.typography.labelSmall, color = CrispWhite)
                        Text("${profile.exp} / ${profile.maxExp} EXP", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CutCornerShape(4.dp))
                            .background(ElevatedPanel)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((profile.exp.toFloat() / profile.maxExp).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(CyberVoltCyan, EmeraldVitality)))
                        )
                    }
                }
            }
        }

        // Telemetry Breakdown
        Surface(
            color = GunmetalSurface,
            shape = CutCornerShape(14.dp),
            border = BorderStroke(1.dp, HairlineSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "COMBAT TELEMETRY",
                    style = MaterialTheme.typography.labelMedium,
                    color = AwakeningGold
                )
                TelemetryLine("DAMAGE DEALT", "${stats.damageDealt} HP", EmeraldVitality)
                TelemetryLine("DAMAGE TAKEN", "${stats.damageTaken} HP", CrimsonFinisher)
                TelemetryLine("MAX RICOCHET COMBO", "${stats.maxCombo}x", AwakeningGold)
                TelemetryLine("TOTAL WALL BOUNCES", "${stats.wallBounces}", CyberVoltCyan)
                TelemetryLine("AWAKENINGS UNLEASHED", "${stats.ultimatesUsed}", SolarFlareOrange)
                TelemetryLine("CLASH DURATION", "${stats.matchDurationSeconds}s", CrispWhite)
            }
        }

        // CTAs
        Button(
            onClick = onRematch,
            shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolarFlareOrange),
            border = BorderStroke(1.5.dp, AwakeningGold),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("rematch_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = CrispWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text("INSTANT REMATCH", style = MaterialTheme.typography.titleLarge, color = CrispWhite)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCharacterSelect,
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElevatedPanel),
                border = BorderStroke(1.dp, HairlineSteel),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("result_roster_button")
            ) {
                Text("SWITCH FIGHTER", style = MaterialTheme.typography.labelLarge, color = CrispWhite)
            }

            Button(
                onClick = onHomeHub,
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberVoltCyan),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("result_home_hub_button")
            ) {
                Text("MAIN HUB", style = MaterialTheme.typography.labelLarge, color = ObsidianVoid)
            }
        }
    }
}

@Composable
private fun RewardBadge(icon: ImageVector, value: String, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge, color = CrispWhite)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedSilver)
        }
    }
}

@Composable
private fun TelemetryLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MutedSilver)
        Text(value, style = MaterialTheme.typography.labelLarge, color = valueColor)
    }
}
