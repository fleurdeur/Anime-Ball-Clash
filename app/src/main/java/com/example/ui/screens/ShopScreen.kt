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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CharacterData
import com.example.model.CharacterRoster
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
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
fun ShopScreen(
    profile: PlayerProfile,
    unlockedIds: Set<String>,
    onUnlockCharacter: (CharacterData) -> Boolean,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var statusBanner by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .testTag("shop_screen")
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("shop_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrispWhite)
                    }
                    Text("ARENA ARMORY", style = MaterialTheme.typography.headlineMedium, color = CrispWhite)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${profile.gold}", style = MaterialTheme.typography.labelLarge, color = CrispWhite)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Diamond, contentDescription = null, tint = CyberVoltCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${profile.gems}", style = MaterialTheme.typography.labelLarge, color = CrispWhite)
                    }
                }
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
            statusBanner?.let { msg ->
                Surface(
                    color = CyberVoltCyan.copy(alpha = 0.16f),
                    shape = CutCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberVoltCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberVoltCyan,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text(
                text = "COMBAT SPHERE CONTRACTS",
                style = MaterialTheme.typography.labelMedium,
                color = AwakeningGold
            )

            CharacterRoster.characters.forEach { char ->
                val isUnlocked = unlockedIds.contains(char.id)
                val canAfford = profile.gold >= char.unlockCostGold && profile.gems >= char.unlockCostGems

                Surface(
                    color = GunmetalSurface,
                    shape = CutCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isUnlocked) HairlineSteel else AwakeningGold.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianVoid)
                                    .border(1.5.dp, char.auraColor, CircleShape)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    AnimeBallRenderer.drawPreviewAvatar(this, char, 0f)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(char.name.uppercase(), style = MaterialTheme.typography.titleLarge, color = CrispWhite)
                                Text(
                                    text = "${char.passiveName} • ATK ${char.baseDamage}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSilver
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (isUnlocked) {
                            Surface(
                                color = ElevatedPanel,
                                shape = CutCornerShape(6.dp),
                                border = BorderStroke(1.dp, EmeraldVitality.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "OWNED",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldVitality,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    val ok = onUnlockCharacter(char)
                                    statusBanner = if (ok) {
                                        "RECRUITED ${char.name.uppercase()}!"
                                    } else {
                                        "INSUFFICIENT GOLD OR GEMS"
                                    }
                                },
                                enabled = canAfford,
                                shape = CutCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AwakeningGold,
                                    disabledContainerColor = ElevatedPanel
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("unlock_btn_${char.id}")
                            ) {
                                Text(
                                    text = "${char.unlockCostGold} G",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (canAfford) ObsidianVoid else MutedSilver
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
