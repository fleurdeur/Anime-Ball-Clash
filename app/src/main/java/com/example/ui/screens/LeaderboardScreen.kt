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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppScreen
import com.example.model.CharacterRoster
import com.example.model.LeaderboardEntry
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.HubNavigationBar
import com.example.ui.theme.AppTheme

@Composable
fun LeaderboardScreen(
    leaderboard: List<LeaderboardEntry>,
    claimableQuestCount: Int,
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val palette = AppTheme.palette

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            HubNavigationBar(
                currentScreen = AppScreen.LEADERBOARD,
                claimableQuestCount = claimableQuestCount,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("leaderboard_screen")
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Arena Rankings",
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.textPrimary
                )
                Text(
                    text = "Win Ranked & Solo clashes to climb from Unranked to Master",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = leaderboard, key = { "${it.rank}_${it.name}" }) { entry ->
                        val character = remember(entry.characterId) {
                            CharacterRoster.getById(entry.characterId)
                        }

                        val rankBadgeBg = when (entry.rank) {
                            1 -> palette.goldAccent
                            2 -> Color(0xFF64748B)
                            3 -> Color(0xFFB45309)
                            else -> palette.elevatedSurface
                        }
                        val rankBadgeFg = when (entry.rank) {
                            1 -> Color(0xFF14161C)
                            2, 3 -> Color.White
                            else -> palette.textPrimary
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (entry.isPlayer) palette.blueAccentSoft else palette.cardSurface,
                            border = BorderStroke(
                                if (entry.isPlayer) 1.5.dp else 1.dp,
                                if (entry.isPlayer) palette.blueAccent else palette.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = rankBadgeBg,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${entry.rank}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = rankBadgeFg,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(palette.stageSurface)
                                        .border(1.dp, palette.stageLine, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(42.dp)) {
                                        AnimeBallRenderer.drawPreviewAvatar(
                                            scope = this,
                                            character = character,
                                            weaponAngle = 0.7f
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (entry.isPlayer) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = palette.primaryAccent, shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = "YOU",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${character.name} • ${entry.tier} • ${entry.winRate} WR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary
                                    )
                                }

                                Text(
                                    text = "${entry.rating} RP",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (entry.isPlayer) palette.blueAccent else palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
