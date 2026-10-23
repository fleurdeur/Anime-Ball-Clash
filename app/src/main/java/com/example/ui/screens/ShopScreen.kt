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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppScreen
import com.example.model.CharacterAlignment
import com.example.model.CharacterData
import com.example.model.CharacterRoster
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.HubNavigationBar
import com.example.ui.theme.AppTheme

@Composable
fun ShopScreen(
    profile: PlayerProfile,
    unlockedIds: Set<String>,
    claimableQuestCount: Int,
    onUnlockCharacter: (CharacterData) -> Boolean,
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val palette = AppTheme.palette
    val sortedRecruits by remember(unlockedIds) {
        derivedStateOf {
            CharacterRoster.characters
                .filter { !it.isStarter }
                .sortedWith(
                    compareBy<CharacterData> { unlockedIds.contains(it.id) }
                        .thenBy { it.unlockCostGold }
                )
        }
    }

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            HubNavigationBar(
                currentScreen = AppScreen.SHOP,
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
                    .testTag("shop_screen")
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fighter Unlock",
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Recruit ${sortedRecruits.size} popular, cult & niche heroes and villains",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = palette.goldAccentSoft,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${profile.gold} G",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.goldAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            color = palette.blueAccentSoft,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, palette.blueAccent.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${profile.gems} Gems",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.blueAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = sortedRecruits, key = { it.id }) { character ->
                        val isUnlocked = unlockedIds.contains(character.id)
                        val canAfford = profile.gold >= character.unlockCostGold
                        val isVillain = character.alignment == CharacterAlignment.ANTAGONIST

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.cardSurface,
                            border = BorderStroke(1.dp, palette.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(palette.stageSurface)
                                        .border(1.dp, palette.stageLine, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(64.dp)) {
                                        AnimeBallRenderer.drawPreviewAvatar(
                                            scope = this,
                                            character = character,
                                            weaponAngle = 0.7f
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${character.name} • ${character.title}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${character.alignment.badgeLabel} • ${character.animeInspiration} • ${character.popularityTier.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isVillain) palette.primaryAccent else palette.blueAccent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = character.passiveDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isUnlocked) {
                                    Surface(
                                        color = palette.greenAccentSoft,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, palette.greenAccent.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = palette.greenAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Owned", style = MaterialTheme.typography.labelLarge, color = palette.greenAccent)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { onUnlockCharacter(character) },
                                        enabled = canAfford,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.goldAccent,
                                            contentColor = Color(0xFF14161C),
                                            disabledContainerColor = palette.elevatedSurface,
                                            disabledContentColor = palette.textMuted
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("unlock_${character.id}")
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${character.unlockCostGold} G", style = MaterialTheme.typography.labelLarge)
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
