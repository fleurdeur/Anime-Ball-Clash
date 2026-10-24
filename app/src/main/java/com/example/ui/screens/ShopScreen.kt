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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.CharacterUpgradeState
import com.example.model.PlayerProfile
import com.example.model.UpgradeStatType
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.HubNavigationBar
import com.example.ui.theme.AppTheme
import java.util.Locale

@Composable
fun ShopScreen(
    profile: PlayerProfile,
    unlockedIds: Set<String>,
    characterUpgrades: Map<String, CharacterUpgradeState>,
    claimableQuestCount: Int,
    onUnlockCharacter: (CharacterData) -> Boolean,
    onUpgradeStat: (String, UpgradeStatType) -> Boolean,
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val palette = AppTheme.palette
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Recruit 220+ Fighters, 1 = Upgrade Forge
    var searchQuery by remember { mutableStateOf("") }

    val sortedRecruits by remember(unlockedIds, searchQuery) {
        derivedStateOf {
            val q = searchQuery.trim()
            CharacterRoster.characters
                .filter { !it.isStarter }
                .filter { char ->
                    q.isEmpty() ||
                        char.name.contains(q, ignoreCase = true) ||
                        char.animeInspiration.contains(q, ignoreCase = true) ||
                        char.title.contains(q, ignoreCase = true)
                }
                .sortedWith(
                    compareBy<CharacterData> { unlockedIds.contains(it.id) }
                        .thenBy { it.unlockCostGold }
                )
        }
    }

    val unlockedRoster by remember(unlockedIds, profile.selectedCharacterId) {
        derivedStateOf {
            CharacterRoster.characters
                .filter { unlockedIds.contains(it.id) }
                .sortedByDescending { it.id == profile.selectedCharacterId }
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
                            text = if (selectedTab == 0) "Shop" else "Upgrades",
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (selectedTab == 0) {
                                "Unlock ${CharacterRoster.characters.size - 4} heroes & villains"
                            } else {
                                "Upgrade Health, Power & Faster Ult"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = palette.goldAccentSoft,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${formatGold(profile.gold)} G",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.goldAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShopTabPill(
                        label = "Fighters (${CharacterRoster.characters.size - 4})",
                        active = selectedTab == 0,
                        testTag = "shop_tab_recruit",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    ShopTabPill(
                        label = "Upgrades (${unlockedRoster.size})",
                        active = selectedTab == 1,
                        testTag = "shop_tab_upgrades",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        placeholder = { Text("Search fighter or anime...", color = palette.textMuted) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = palette.textSecondary)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.cardSurface,
                            unfocusedContainerColor = palette.cardSurface,
                            focusedBorderColor = palette.blueAccent,
                            unfocusedBorderColor = palette.border,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = sortedRecruits,
                            key = { it.id },
                            contentType = { "shop_recruit_card" }
                        ) { character ->
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
                                            text = "${character.alignment.badgeLabel} • ${character.animeInspiration} • ${character.maxHp} HP",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isVillain) palette.primaryAccent else palette.blueAccent,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${character.passiveName}: ${character.passiveDescription}",
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
                                                contentColor = if (palette.isDark) Color(0xFF14161C) else Color.White,
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
                                            Text("${formatGold(character.unlockCostGold)} G", style = MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = unlockedRoster,
                            key = { "upg_${it.id}" },
                            contentType = { "shop_upgrade_card" }
                        ) { character ->
                            val upg = characterUpgrades[character.id] ?: CharacterUpgradeState()
                            val effHp = upg.effectiveMaxHp(character.maxHp)
                            val effDmg = upg.effectiveBaseDamage(character.baseDamage)
                            val effCd = upg.effectiveUltimateCooldown(character.ultimateCooldownSeconds)
                            val isEquipped = profile.selectedCharacterId == character.id

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = palette.cardSurface,
                                border = BorderStroke(
                                    width = if (isEquipped) 1.5.dp else 1.dp,
                                    color = if (isEquipped) palette.primaryAccent else palette.border
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(palette.stageSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(54.dp)) {
                                                AnimeBallRenderer.drawPreviewAvatar(
                                                    scope = this,
                                                    character = character,
                                                    weaponAngle = 0.7f
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = character.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = palette.textPrimary
                                            )
                                            Text(
                                                text = "${character.animeInspiration} • Total Lv +${upg.totalUpgrades}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.textSecondary
                                            )
                                        }
                                        if (isEquipped) {
                                            Surface(color = palette.primaryAccentSoft, shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = "Equipped",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.primaryAccent,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = palette.border)

                                    UpgradeTrackRow(
                                        icon = Icons.Default.Favorite,
                                        accent = palette.greenAccent,
                                        title = "Health (+35 HP)",
                                        currentLevel = upg.healthLevel,
                                        statPreview = "$effHp HP",
                                        costGold = upg.nextCostGold(UpgradeStatType.HEALTH),
                                        playerGold = profile.gold,
                                        testTag = "shop_upgrade_hp_${character.id}",
                                        onUpgrade = { onUpgradeStat(character.id, UpgradeStatType.HEALTH) }
                                    )

                                    UpgradeTrackRow(
                                        icon = Icons.Default.Bolt,
                                        accent = palette.primaryAccent,
                                        title = "Power (+2 DMG)",
                                        currentLevel = upg.powerLevel,
                                        statPreview = "$effDmg DMG",
                                        costGold = upg.nextCostGold(UpgradeStatType.POWER),
                                        playerGold = profile.gold,
                                        testTag = "shop_upgrade_pwr_${character.id}",
                                        onUpgrade = { onUpgradeStat(character.id, UpgradeStatType.POWER) }
                                    )

                                    UpgradeTrackRow(
                                        icon = Icons.Default.Speed,
                                        accent = palette.blueAccent,
                                        title = "Faster Ult (-0.45s)",
                                        currentLevel = upg.cooldownLevel,
                                        statPreview = String.format(Locale.US, "%.1fs", effCd),
                                        costGold = upg.nextCostGold(UpgradeStatType.COOLDOWN),
                                        playerGold = profile.gold,
                                        testTag = "shop_upgrade_cd_${character.id}",
                                        onUpgrade = { onUpgradeStat(character.id, UpgradeStatType.COOLDOWN) }
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

@Composable
private fun ShopTabPill(
    label: String,
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
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (active) Color.White else palette.textPrimary
            )
        }
    }
}
