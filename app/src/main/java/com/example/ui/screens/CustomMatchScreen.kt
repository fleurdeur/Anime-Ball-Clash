package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BotDifficulty
import com.example.model.CharacterAlignment
import com.example.model.CharacterData
import com.example.model.CharacterRoster
import com.example.model.ElementTag
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AppTheme

private enum class CustomSlotTab {
    RIVAL,
    PLAYER
}

private enum class CustomRoleFilter(val label: String) {
    ALL("All"),
    HERO("Heroes"),
    VILLAIN("Villains")
}

@Composable
fun CustomMatchScreen(
    profile: PlayerProfile,
    unlockedIds: Set<String>,
    selectedOpponent: CharacterData,
    selectedDifficulty: BotDifficulty,
    onSelectPlayerCharacter: (String) -> Unit,
    onSelectOpponentCharacter: (CharacterData) -> Unit,
    onSelectDifficulty: (BotDifficulty) -> Unit,
    onStartCustomFight: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val palette = AppTheme.palette
    var activeTab by remember { mutableStateOf(CustomSlotTab.RIVAL) }
    var searchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf(CustomRoleFilter.ALL) }
    var elementFilter by remember { mutableStateOf<ElementTag?>(null) }
    var previewUlt by remember { mutableStateOf(false) }

    val playerChar = remember(profile.selectedCharacterId) {
        CharacterRoster.getById(profile.selectedCharacterId)
    }

    val filteredCharacters by remember(activeTab, unlockedIds, searchQuery, roleFilter, elementFilter) {
        derivedStateOf {
            val basePool = if (activeTab == CustomSlotTab.PLAYER) {
                CharacterRoster.characters.filter { unlockedIds.contains(it.id) }
            } else {
                CharacterRoster.characters
            }
            val q = searchQuery.trim().lowercase()
            basePool.filter { char ->
                val matchesRole = when (roleFilter) {
                    CustomRoleFilter.ALL -> true
                    CustomRoleFilter.HERO -> char.alignment == CharacterAlignment.PROTAGONIST
                    CustomRoleFilter.VILLAIN -> char.alignment == CharacterAlignment.ANTAGONIST
                }
                val matchesElem = elementFilter == null || char.element == elementFilter
                val matchesSearch = q.isEmpty() ||
                    char.name.lowercase().contains(q) ||
                    char.animeInspiration.lowercase().contains(q) ||
                    char.title.lowercase().contains(q)
                matchesRole && matchesElem && matchesSearch
            }
        }
    }

    Scaffold(
        containerColor = palette.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.cardSurface)
                            .border(1.dp, palette.border, CircleShape)
                            .testTag("custom_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Custom Match",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Pick your fighter & exact rival",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            val randomRival = CharacterRoster.characters
                                .filter { it.id != selectedOpponent.id }
                                .random()
                            onSelectOpponentCharacter(randomRival)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.cardSurface)
                            .border(1.dp, palette.border, CircleShape)
                            .testTag("random_rival_button")
                    ) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Random Rival",
                            tint = palette.blueAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Surface(
                        onClick = { previewUlt = !previewUlt },
                        shape = RoundedCornerShape(12.dp),
                        color = if (previewUlt) palette.goldAccentSoft else palette.cardSurface,
                        border = BorderStroke(1.dp, if (previewUlt) palette.goldAccent else palette.border),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("custom_ult_preview_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (previewUlt) palette.goldAccent else palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (previewUlt) "Ult ON" else "Show Ult",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (previewUlt) palette.goldAccent else palette.textPrimary
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onStartCustomFight,
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primaryAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_custom_match_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fight ${selectedOpponent.name} (${selectedDifficulty.title})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("custom_match_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Live VS Matchup Stage
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MatchupSlotCard(
                                roleLabel = "YOU",
                                character = playerChar,
                                isEditing = activeTab == CustomSlotTab.PLAYER,
                                isUltPreview = previewUlt,
                                accent = palette.blueAccent,
                                onClick = { activeTab = CustomSlotTab.PLAYER },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_player_tab")
                            )

                            Surface(
                                shape = CircleShape,
                                color = palette.primaryAccent,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "VS",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }

                            MatchupSlotCard(
                                roleLabel = "RIVAL",
                                character = selectedOpponent,
                                isEditing = activeTab == CustomSlotTab.RIVAL,
                                isUltPreview = previewUlt,
                                accent = palette.primaryAccent,
                                onClick = { activeTab = CustomSlotTab.RIVAL },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_rival_tab")
                            )
                        }

                        // Difficulty Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "AI Level",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textSecondary
                            )
                            BotDifficulty.entries.forEach { diff ->
                                val active = selectedDifficulty == diff
                                val label = when (diff) {
                                    BotDifficulty.EASY -> "Easy"
                                    BotDifficulty.MEDIUM -> "Normal (Hard)"
                                    BotDifficulty.HARD -> "Hard (Extreme)"
                                }
                                Surface(
                                    onClick = { onSelectDifficulty(diff) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (active) palette.goldAccentSoft else palette.elevatedSurface,
                                    border = BorderStroke(1.dp, if (active) palette.goldAccent else palette.border),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("custom_diff_${diff.name.lowercase()}")
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (active) palette.goldAccent else palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Search & Filter Strip
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it.take(32) },
                    singleLine = true,
                    placeholder = {
                        Text(
                            if (activeTab == CustomSlotTab.RIVAL) {
                                "Search all ${CharacterRoster.characters.size} rivals or anime..."
                            } else {
                                "Search your ${unlockedIds.size} unlocked fighters..."
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = palette.textMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_search_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CustomRoleFilter.entries.forEach { rf ->
                        val active = roleFilter == rf
                        FilterChipPill(
                            text = rf.label,
                            selected = active,
                            onClick = { roleFilter = rf }
                        )
                    }
                    ElementTag.entries.filter { it != ElementTag.ALL }.forEach { elem ->
                        val active = elementFilter == elem
                        FilterChipPill(
                            text = elem.name,
                            selected = active,
                            onClick = { elementFilter = if (active) null else elem }
                        )
                    }
                }

                // 3. Fast Fighter Picker Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 104.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("custom_fighter_grid")
                ) {
                    items(
                        items = filteredCharacters,
                        key = { it.id },
                        contentType = { "custom_fighter_card" }
                    ) { char ->
                        val isSelected = if (activeTab == CustomSlotTab.PLAYER) {
                            char.id == playerChar.id
                        } else {
                            char.id == selectedOpponent.id
                        }
                        val accent = if (activeTab == CustomSlotTab.PLAYER) palette.blueAccent else palette.primaryAccent

                        Surface(
                            onClick = {
                                if (activeTab == CustomSlotTab.PLAYER) {
                                    onSelectPlayerCharacter(char.id)
                                } else {
                                    onSelectOpponentCharacter(char)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) accent.copy(alpha = 0.14f) else palette.cardSurface,
                            border = BorderStroke(
                                if (isSelected) 1.8.dp else 1.dp,
                                if (isSelected) accent else palette.border
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_pick_${char.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(palette.stageSurface)
                                        .border(1.dp, palette.stageLine, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(56.dp)) {
                                        AnimeBallRenderer.drawPreviewAvatar(
                                            scope = this,
                                            character = char,
                                            weaponAngle = 0.72f,
                                            isUltimatePreview = previewUlt
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = char.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = char.animeInspiration,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchupSlotCard(
    roleLabel: String,
    character: CharacterData,
    isEditing: Boolean,
    isUltPreview: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = AppTheme.palette
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isEditing) accent.copy(alpha = 0.12f) else palette.elevatedSurface,
        border = BorderStroke(if (isEditing) 1.8.dp else 1.dp, if (isEditing) accent else palette.border),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(palette.stageSurface)
                    .border(1.dp, palette.stageLine, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(46.dp)) {
                    AnimeBallRenderer.drawPreviewAvatar(
                        scope = this,
                        character = character,
                        weaponAngle = if (isUltPreview) 1.15f else 0.7f,
                        isUltimatePreview = isUltPreview
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditing) "$roleLabel • PICKING" else "$roleLabel • TAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    maxLines = 1
                )
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${character.maxHp} HP • ${character.baseDamage} DMG",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = AppTheme.palette
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) palette.blueAccent else palette.cardSurface,
        border = BorderStroke(1.dp, if (selected) palette.blueAccent else palette.border),
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else palette.textSecondary
            )
        }
    }
}
