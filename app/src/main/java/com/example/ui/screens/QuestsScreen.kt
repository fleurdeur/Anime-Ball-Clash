package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppScreen
import com.example.model.QuestItem
import com.example.ui.components.HubNavigationBar
import com.example.ui.theme.AppTheme

@Composable
fun QuestsScreen(
    quests: List<QuestItem>,
    dailyQuests: List<QuestItem> = emptyList(),
    onClaimQuest: (String) -> Unit,
    onClaimAll: () -> Unit = {},
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val palette = AppTheme.palette
    var selectedTab by remember { mutableIntStateOf(if (dailyQuests.isNotEmpty()) 0 else 1) }

    val dailyReadyCount = dailyQuests.count { !it.isClaimed && it.currentProgress >= it.targetProgress }
    val mainReadyCount = quests.count { !it.isClaimed && it.currentProgress >= it.targetProgress }
    val totalClaimableCount = dailyReadyCount + mainReadyCount

    val totalReadyGold = remember(quests, dailyQuests) {
        (quests + dailyQuests)
            .filter { !it.isClaimed && it.currentProgress >= it.targetProgress }
            .sumOf { it.rewardGold }
    }

    val activeList = if (selectedTab == 0 && dailyQuests.isNotEmpty()) dailyQuests else quests

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            HubNavigationBar(
                currentScreen = AppScreen.QUESTS,
                claimableQuestCount = totalClaimableCount,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quests",
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Finish simple daily & main goals for free Gold",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }

                    AnimatedVisibility(visible = totalClaimableCount > 0) {
                        Button(
                            onClick = onClaimAll,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.goldAccent,
                                contentColor = if (palette.isDark) Color(0xFF14161C) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("claim_all_quests_button")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Claim All (+${formatGold(totalReadyGold)} G)",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (dailyQuests.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuestTabButton(
                            label = if (dailyReadyCount > 0) "Daily ($dailyReadyCount Ready)" else "Daily Quests",
                            icon = Icons.Default.Today,
                            active = selectedTab == 0,
                            testTag = "daily_quests_tab",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        QuestTabButton(
                            label = if (mainReadyCount > 0) "Main Goals ($mainReadyCount Ready)" else "Main Goals",
                            icon = Icons.Outlined.EmojiEvents,
                            active = selectedTab == 1,
                            testTag = "main_quests_tab",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = palette.elevatedSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedTab == 0) {
                                "Daily Quests refresh every new day • Counter, Ultimate & Win rewards!"
                            } else {
                                "All-time fighter milestones with huge Gold bonuses"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                AnimatedContent(
                    targetState = activeList,
                    transitionSpec = {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(130))
                    },
                    label = "quest_tab_list_anim",
                    modifier = Modifier.weight(1f)
                ) { currentQuests ->
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = currentQuests,
                            key = { it.id },
                            contentType = { "quest_card" }
                        ) { quest ->
                            QuestCardItem(
                                quest = quest,
                                onClaimQuest = onClaimQuest
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestTabButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = AppTheme.palette
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (active) palette.primaryAccent else palette.cardSurface,
        border = BorderStroke(1.dp, if (active) palette.primaryAccent else palette.border),
        modifier = modifier
            .height(48.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) Color.White else palette.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (active) Color.White else palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuestCardItem(
    quest: QuestItem,
    onClaimQuest: (String) -> Unit
) {
    val palette = AppTheme.palette
    val isComplete = quest.currentProgress >= quest.targetProgress
    val rawRatio = (quest.currentProgress.toFloat() / quest.targetProgress.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawRatio,
        animationSpec = tween(durationMillis = 320),
        label = "quest_progress_${quest.id}"
    )

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (quest.isDaily) {
                            Surface(
                                color = palette.blueAccentSoft,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "DAILY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.blueAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = quest.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = quest.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = palette.goldAccentSoft,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "+${formatGold(quest.rewardGold)} G",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.goldAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (quest.rewardGems > 0) {
                        Text(
                            text = "+${quest.rewardGems} Gems",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.blueAccent
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
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
                                Text("Done", style = MaterialTheme.typography.labelLarge, color = palette.greenAccent)
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
                                text = "Active",
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
