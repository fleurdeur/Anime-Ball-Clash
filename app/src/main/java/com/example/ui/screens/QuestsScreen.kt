package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppScreen
import com.example.model.QuestItem
import com.example.ui.components.HubNavigationBar
import com.example.ui.theme.AppTheme

@Composable
fun QuestsScreen(
    quests: List<QuestItem>,
    onClaimQuest: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val palette = AppTheme.palette
    val claimableCount = quests.count { !it.isClaimed && it.currentProgress >= it.targetProgress }

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            HubNavigationBar(
                currentScreen = AppScreen.QUESTS,
                claimableQuestCount = claimableCount,
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
                    .testTag("quests_screen")
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Missions",
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.textPrimary
                )
                Text(
                    text = "Complete arena milestones to fund new anime fighters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = quests, key = { it.id }) { quest ->
                        val isComplete = quest.currentProgress >= quest.targetProgress
                        val progressRatio = (quest.currentProgress.toFloat() / quest.targetProgress.coerceAtLeast(1)).coerceIn(0f, 1f)

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.cardSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isComplete && !quest.isClaimed) palette.primaryAccent else palette.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = quest.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = palette.textPrimary
                                        )
                                        Text(
                                            text = quest.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = palette.textSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = palette.goldAccentSoft,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "+${formatGold(quest.rewardGold)} G",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.goldAccent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        LinearProgressIndicator(
                                            progress = { progressRatio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(50)),
                                            color = if (isComplete) palette.greenAccent else palette.blueAccent,
                                            trackColor = palette.elevatedSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${quest.currentProgress} / ${quest.targetProgress}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textMuted
                                        )
                                    }

                                    when {
                                        quest.isClaimed -> {
                                            Surface(
                                                color = palette.greenAccentSoft,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, palette.greenAccent.copy(alpha = 0.35f))
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = palette.greenAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Claimed", style = MaterialTheme.typography.labelLarge, color = palette.greenAccent)
                                                }
                                            }
                                        }
                                        isComplete -> {
                                            Button(
                                                onClick = { onClaimQuest(quest.id) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = palette.primaryAccent,
                                                    contentColor = Color(0xFFF8FAFC)
                                                ),
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .testTag("claim_${quest.id}")
                                            ) {
                                                Text("Claim", style = MaterialTheme.typography.labelLarge)
                                            }
                                        }
                                        else -> {
                                            Surface(
                                                color = palette.elevatedSurface,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, palette.border)
                                            ) {
                                                Text(
                                                    text = "In Progress",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = palette.textSecondary,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
