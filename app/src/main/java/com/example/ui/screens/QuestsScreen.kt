package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.QuestItem
import com.example.ui.theme.AwakeningGold
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyberVoltCyan
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.EmeraldVitality
import com.example.ui.theme.GunmetalSurface
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SlateAbyss

@Composable
fun QuestsScreen(
    quests: List<QuestItem>,
    onClaimQuest: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .testTag("quests_screen")
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
                        .testTag("quests_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrispWhite)
                }
                Text("COMBAT MISSIONS", style = MaterialTheme.typography.headlineMedium, color = CrispWhite)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            quests.forEach { quest ->
                val isCompleted = quest.currentProgress >= quest.targetProgress

                Surface(
                    color = GunmetalSurface,
                    shape = CutCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            quest.isClaimed -> HairlineSteel
                            isCompleted -> CyberVoltCyan
                            else -> HairlineSteel
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(quest.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = CrispWhite)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, null, tint = AwakeningGold, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+${quest.rewardGold}", style = MaterialTheme.typography.labelMedium, color = AwakeningGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Diamond, null, tint = CyberVoltCyan, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+${quest.rewardGems}", style = MaterialTheme.typography.labelMedium, color = CyberVoltCyan)
                            }
                        }

                        Text(quest.description, style = MaterialTheme.typography.bodyMedium, color = MutedSilver)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${quest.currentProgress} / ${quest.targetProgress}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCompleted) EmeraldVitality else MutedSilver
                                )
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
                                            .fillMaxWidth((quest.currentProgress.toFloat() / quest.targetProgress).coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .background(Brush.horizontalGradient(listOf(CyberVoltCyan, EmeraldVitality)))
                                    )
                                }
                            }

                            Button(
                                onClick = { onClaimQuest(quest.id) },
                                enabled = isCompleted && !quest.isClaimed,
                                shape = CutCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberVoltCyan,
                                    disabledContainerColor = ElevatedPanel
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("claim_quest_${quest.id}")
                            ) {
                                Text(
                                    text = when {
                                        quest.isClaimed -> "CLAIMED"
                                        isCompleted -> "CLAIM"
                                        else -> "ACTIVE"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isCompleted && !quest.isClaimed) ObsidianVoid else MutedSilver
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
