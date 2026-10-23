package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CharacterRoster
import com.example.model.LeaderboardEntry
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AwakeningGold
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyberVoltCyan
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.GunmetalSurface
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SlateAbyss
import com.example.ui.theme.SolarFlareOrange

@Composable
fun LeaderboardScreen(
    leaderboard: List<LeaderboardEntry>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .testTag("leaderboard_screen")
    ) {
        Surface(
            color = SlateAbyss,
            border = BorderStroke(1.dp, HairlineSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("leaderboard_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrispWhite)
                }
                Text("ARENA LADDER", style = MaterialTheme.typography.headlineMedium, color = CrispWhite)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leaderboard) { entry ->
                val rankAccent = when (entry.rank) {
                    1 -> AwakeningGold
                    2 -> CyberVoltCyan
                    3 -> SolarFlareOrange
                    else -> MutedSilver
                }
                val char = CharacterRoster.getById(entry.characterId)

                Surface(
                    color = if (entry.isPlayer) ElevatedPanel else GunmetalSurface,
                    shape = CutCornerShape(10.dp),
                    border = BorderStroke(
                        if (entry.isPlayer) 1.5.dp else 1.dp,
                        if (entry.isPlayer) CyberVoltCyan else HairlineSteel
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${entry.rank}",
                                style = MaterialTheme.typography.titleLarge,
                                color = rankAccent,
                                modifier = Modifier.width(36.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianVoid)
                                    .border(1.dp, char.auraColor, CircleShape)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    AnimeBallRenderer.drawPreviewAvatar(this, char, 0f)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (entry.isPlayer) "${entry.name} (YOU)" else entry.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (entry.isPlayer) CyberVoltCyan else CrispWhite
                                )
                                Text(
                                    text = "${entry.tier} • ${entry.winRate} WIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSilver
                                )
                            }
                        }

                        Text(
                            text = "${entry.rating} MMR",
                            style = MaterialTheme.typography.labelLarge,
                            color = AwakeningGold
                        )
                    }
                }
            }
        }
    }
}
