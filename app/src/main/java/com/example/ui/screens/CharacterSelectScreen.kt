package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AnimeTier
import com.example.model.AppScreen
import com.example.model.CharacterAlignment
import com.example.model.CharacterClass
import com.example.model.CharacterData
import com.example.model.CharacterRoster
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.HubNavigationBar
import com.example.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelectScreen(
    profile: PlayerProfile,
    unlockedIds: Set<String>,
    claimableQuestCount: Int,
    onSelectCharacter: (String) -> Unit,
    onUnlockCharacter: (CharacterData) -> Boolean,
    onStartBattle: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit
) {
    val palette = AppTheme.palette
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedAlignment by remember { mutableStateOf<CharacterAlignment?>(null) }
    var selectedTier by remember { mutableStateOf<AnimeTier?>(null) }
    var selectedRole by remember { mutableStateOf<CharacterClass?>(null) }
    var inspectedCharacter by remember { mutableStateOf<CharacterData?>(null) }

    val filteredRoster by remember(searchQuery, selectedAlignment, selectedTier, selectedRole) {
        derivedStateOf {
            CharacterRoster.characters.filter { char ->
                val matchesAlignment = selectedAlignment == null || char.alignment == selectedAlignment
                val matchesTier = selectedTier == null || char.popularityTier == selectedTier
                val matchesRole = selectedRole == null || char.classType == selectedRole
                val q = searchQuery.trim()
                val matchesSearch = q.isEmpty() ||
                    char.name.contains(q, ignoreCase = true) ||
                    char.title.contains(q, ignoreCase = true) ||
                    char.animeInspiration.contains(q, ignoreCase = true)
                matchesAlignment && matchesTier && matchesRole && matchesSearch
            }
        }
    }

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            HubNavigationBar(
                currentScreen = AppScreen.CHARACTER_SELECT,
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
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("character_select_screen")
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fighters",
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "${unlockedIds.size} of ${CharacterRoster.characters.size} unlocked • Heroes & Villains",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                    Surface(color = palette.goldAccentSoft, shape = RoundedCornerShape(50)) {
                        Text(
                            text = "${profile.gold} Gold",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.goldAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    placeholder = { Text("Search hero, villain, or anime series...", color = palette.textMuted) },
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
                        .testTag("fighter_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Row: Protagonists vs Antagonists + Popular / Cult / Niche + Combat Roles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleChip(
                        label = "All (${CharacterRoster.characters.size})",
                        selected = selectedAlignment == null && selectedTier == null && selectedRole == null,
                        onClick = {
                            selectedAlignment = null
                            selectedTier = null
                            selectedRole = null
                        }
                    )
                    RoleChip(
                        label = "Protagonists",
                        selected = selectedAlignment == CharacterAlignment.PROTAGONIST,
                        onClick = {
                            selectedAlignment = if (selectedAlignment == CharacterAlignment.PROTAGONIST) null else CharacterAlignment.PROTAGONIST
                        }
                    )
                    RoleChip(
                        label = "Antagonists",
                        selected = selectedAlignment == CharacterAlignment.ANTAGONIST,
                        onClick = {
                            selectedAlignment = if (selectedAlignment == CharacterAlignment.ANTAGONIST) null else CharacterAlignment.ANTAGONIST
                        }
                    )
                    AnimeTier.entries.forEach { tier ->
                        RoleChip(
                            label = tier.displayName,
                            selected = selectedTier == tier,
                            onClick = { selectedTier = if (selectedTier == tier) null else tier }
                        )
                    }
                    CharacterClass.entries.forEach { role ->
                        RoleChip(
                            label = role.displayName,
                            selected = selectedRole == role,
                            onClick = { selectedRole = if (selectedRole == role) null else role }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 112.dp),
                    contentPadding = PaddingValues(bottom = 20.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = filteredRoster, key = { it.id }) { character ->
                        val isUnlocked = unlockedIds.contains(character.id)
                        val isEquipped = profile.selectedCharacterId == character.id

                        FighterGridTile(
                            character = character,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            onClick = {
                                if (isUnlocked) {
                                    onSelectCharacter(character.id)
                                }
                                inspectedCharacter = character
                            }
                        )
                    }
                }
            }
        }
    }

    inspectedCharacter?.let { char ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var previewAwakened by remember(char.id) { mutableStateOf(false) }
        val isUnlocked = unlockedIds.contains(char.id)
        val isEquipped = profile.selectedCharacterId == char.id
        val canAfford = profile.gold >= char.unlockCostGold
        val isVillain = char.alignment == CharacterAlignment.ANTAGONIST

        ModalBottomSheet(
            onDismissRequest = { inspectedCharacter = null },
            sheetState = sheetState,
            containerColor = palette.cardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(palette.stageSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(84.dp)) {
                            AnimeBallRenderer.drawPreviewAvatar(
                                scope = this,
                                character = char,
                                weaponAngle = if (previewAwakened) 1.2f else 0.7f,
                                isUltimatePreview = previewAwakened
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = if (isVillain) palette.primaryAccentSoft else palette.blueAccentSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = char.alignment.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isVillain) palette.primaryAccent else palette.blueAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = palette.elevatedSurface,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = char.popularityTier.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = char.name,
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "${char.title} • ${char.animeInspiration}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { previewAwakened = !previewAwakened },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, if (previewAwakened) palette.primaryAccent else palette.border)
                        ) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = if (previewAwakened) palette.primaryAccent else palette.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (previewAwakened) "Awakened Form" else "Preview Ultimate Form",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (previewAwakened) palette.primaryAccent else palette.textPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SheetMetric("ROLE", char.classType.displayName, Modifier.weight(1f))
                    SheetMetric("DAMAGE", "${char.baseDamage}", Modifier.weight(1f))
                    SheetMetric("SPEED", "${String.format("%.2fx", char.speedRating)}", Modifier.weight(1f))
                    SheetMetric("ULT CD", "${char.ultimateCooldownSeconds}s", Modifier.weight(1f))
                }

                HorizontalDivider(color = palette.border)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Passive • ${char.passiveName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.blueAccent
                    )
                    Text(char.passiveDescription, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Ultimate • ${char.ultimateName} (${char.ultimateDurationSeconds}s • ${char.ultimateCooldownSeconds}s CD)",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.primaryAccent
                    )
                    Text(char.ultimateDescription, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                }

                Text(
                    text = "\"${char.voiceLine}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = palette.textMuted
                )

                if (isUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isEquipped) {
                            OutlinedButton(
                                onClick = { onSelectCharacter(char.id) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, palette.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text("Equip", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                            }
                        }
                        Button(
                            onClick = {
                                onSelectCharacter(char.id)
                                inspectedCharacter = null
                                onStartBattle()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primaryAccent, contentColor = Color.White),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(52.dp)
                                .testTag("sheet_start_clash_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Battle with ${char.name}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onUnlockCharacter(char)
                        },
                        enabled = canAfford,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.goldAccent,
                            contentColor = Color(0xFF14161C),
                            disabledContainerColor = palette.elevatedSurface,
                            disabledContentColor = palette.textMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("sheet_unlock_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (canAfford) {
                                "Unlock ${char.name} (${char.unlockCostGold} Gold)"
                            } else {
                                "Need ${char.unlockCostGold} Gold (Have ${profile.gold})"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RoleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val palette = AppTheme.palette
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = palette.blueAccentSoft,
            selectedLabelColor = palette.blueAccent,
            containerColor = palette.cardSurface,
            labelColor = palette.textSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = palette.border,
            selectedBorderColor = palette.blueAccent
        )
    )
}

@Composable
private fun FighterGridTile(
    character: CharacterData,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    val palette = AppTheme.palette
    val isVillain = character.alignment == CharacterAlignment.ANTAGONIST
    val borderColor = when {
        isEquipped -> palette.primaryAccent
        isUnlocked -> palette.border
        else -> palette.border.copy(alpha = 0.6f)
    }
    val borderWidth = if (isEquipped) 1.8.dp else 1.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = palette.cardSurface,
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fighter_card_${character.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(palette.stageSurface),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(64.dp)) {
                    AnimeBallRenderer.drawPreviewAvatar(
                        scope = this,
                        character = character,
                        weaponAngle = 0.65f
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = character.name,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${character.alignment.badgeLabel} • ${character.animeInspiration}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isVillain) palette.primaryAccent else palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            when {
                isEquipped -> {
                    Surface(color = palette.greenAccentSoft, shape = RoundedCornerShape(50)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = palette.greenAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Equipped", style = MaterialTheme.typography.labelSmall, color = palette.greenAccent)
                        }
                    }
                }
                isUnlocked -> {
                    Surface(color = palette.blueAccentSoft, shape = RoundedCornerShape(50)) {
                        Text(
                            text = "Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.blueAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                else -> {
                    Surface(color = palette.goldAccentSoft, shape = RoundedCornerShape(50)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = palette.goldAccent, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${character.unlockCostGold} G", style = MaterialTheme.typography.labelSmall, color = palette.goldAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetMetric(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = AppTheme.palette
    Surface(
        color = palette.elevatedSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.labelLarge, color = palette.textPrimary, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textMuted, maxLines = 1)
        }
    }
}
