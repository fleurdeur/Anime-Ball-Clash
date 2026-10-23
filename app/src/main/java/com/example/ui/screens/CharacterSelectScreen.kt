package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CharacterRoster
import com.example.model.ElementTag
import com.example.model.EngineSettings
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.theme.AwakeningGold
import com.example.ui.theme.CrimsonFinisher
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyberVoltCyan
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.EmeraldVitality
import com.example.ui.theme.GunmetalSurface
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SlateAbyss
import com.example.ui.theme.SolarFlareOrange

@Composable
fun CharacterSelectScreen(
    currentSelectedId: String,
    unlockedIds: Set<String>,
    engineSettings: EngineSettings,
    onSelectCharacter: (String) -> Unit,
    onStartBattle: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedElementFilter by remember { mutableStateOf(ElementTag.ALL) }
    var previewCharacterId by remember { mutableStateOf(currentSelectedId) }
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                animTime += ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = nanos
            }
        }
    }

    val previewChar = remember(previewCharacterId) {
        CharacterRoster.getById(previewCharacterId)
    }

    val filteredCharacters = remember(searchQuery, selectedElementFilter) {
        CharacterRoster.characters.filter { char ->
            val matchesQuery = searchQuery.isBlank() ||
                char.name.contains(searchQuery, ignoreCase = true) ||
                char.animeInspiration.contains(searchQuery, ignoreCase = true) ||
                char.element.name.contains(searchQuery, ignoreCase = true) ||
                char.passiveName.contains(searchQuery, ignoreCase = true)
            val matchesElement = selectedElementFilter == ElementTag.ALL || char.element == selectedElementFilter
            matchesQuery && matchesElement
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .testTag("character_select_screen")
    ) {
        // 1. TOP BAR + INSTANT SEARCH + ELEMENT FILTER
        Surface(
            color = SlateAbyss,
            border = BorderStroke(1.dp, HairlineSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CrispWhite
                        )
                    }
                    Text(
                        text = "FIGHTER ROSTER",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CrispWhite
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("character_search_field"),
                    placeholder = {
                        Text(
                            text = "Search fighter, anime, element, or passive...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSilver
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CyberVoltCyan)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MutedSilver)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CrispWhite,
                        unfocusedTextColor = CrispWhite,
                        focusedBorderColor = CyberVoltCyan,
                        unfocusedBorderColor = HairlineSteel,
                        focusedContainerColor = GunmetalSurface,
                        unfocusedContainerColor = GunmetalSurface
                    ),
                    singleLine = true,
                    shape = CutCornerShape(8.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ElementTag.values().toList()) { tag ->
                        val active = selectedElementFilter == tag
                        Surface(
                            onClick = { selectedElementFilter = tag },
                            color = if (active) Color(tag.badgeColor) else ElevatedPanel,
                            shape = CutCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (active) Color(tag.badgeColor) else HairlineSteel),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = tag.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (active) ObsidianVoid else CrispWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. ROSTER RIBBON + SPEC SHEET
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(filteredCharacters) { char ->
                    val isLocked = !unlockedIds.contains(char.id)
                    val isEquipped = currentSelectedId == char.id
                    val isPreview = previewCharacterId == char.id

                    val borderColor = when {
                        isPreview -> CyberVoltCyan
                        isEquipped -> AwakeningGold
                        else -> HairlineSteel
                    }

                    Surface(
                        onClick = { previewCharacterId = char.id },
                        color = if (isPreview) ElevatedPanel else GunmetalSurface,
                        shape = CutCornerShape(10.dp),
                        border = BorderStroke(if (isPreview) 2.dp else 1.dp, borderColor),
                        modifier = Modifier
                            .width(108.dp)
                            .testTag("char_tile_${char.id}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(62.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    AnimeBallRenderer.drawPreviewAvatar(this, char, animTime)
                                }
                                if (isLocked) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(ObsidianVoid)
                                            .border(1.dp, AwakeningGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = AwakeningGold, modifier = Modifier.size(13.dp))
                                    }
                                } else if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(EmeraldVitality),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Equipped", tint = ObsidianVoid, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = char.name.substringBefore(" ").uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = CrispWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = char.element.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(char.element.badgeColor)
                            )
                        }
                    }
                }
            }

            // DETAILED FIGHTER SPEC CARD
            Surface(
                color = GunmetalSurface,
                shape = CutCornerShape(16.dp),
                border = BorderStroke(1.5.dp, HairlineSteel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = previewChar.animeInspiration.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = AwakeningGold
                            )
                            Text(
                                text = previewChar.name.uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = CrispWhite
                            )
                            Text(
                                text = previewChar.classType.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyberVoltCyan
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(ObsidianVoid)
                                .border(1.5.dp, previewChar.auraColor, CircleShape)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                AnimeBallRenderer.drawPreviewAvatar(this, previewChar, animTime)
                            }
                        }
                    }

                    // 4-Stat Tactical Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FighterStatTile("VITALITY", "100 HP", EmeraldVitality, Modifier.weight(1f))
                        FighterStatTile("IMPACT", "${previewChar.baseDamage} DMG", SolarFlareOrange, Modifier.weight(1f))
                        FighterStatTile("VELOCITY", "x${previewChar.speedRating}", CyberVoltCyan, Modifier.weight(1f))
                        FighterStatTile("MASS", "x${previewChar.massRating}", AwakeningGold, Modifier.weight(1f))
                    }

                    // Canon Passive Box
                    Surface(
                        color = ElevatedPanel,
                        shape = CutCornerShape(10.dp),
                        border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CANON PASSIVE • ${previewChar.passiveName.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AwakeningGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = previewChar.passiveDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrispWhite
                            )
                        }
                    }

                    // Timed Ultimate Box
                    Surface(
                        color = ElevatedPanel,
                        shape = CutCornerShape(10.dp),
                        border = BorderStroke(1.dp, CrimsonFinisher.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TIMED ULTIMATE • ${previewChar.ultimateName.uppercase()} (${previewChar.ultimateDurationSeconds.toInt()}S / ${previewChar.ultimateDamageMultiplier}X)",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrimsonFinisher
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = previewChar.ultimateDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrispWhite
                            )
                        }
                    }

                    // Action Buttons
                    val isUnlocked = unlockedIds.contains(previewChar.id)
                    val isEquipped = currentSelectedId == previewChar.id

                    if (isUnlocked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onSelectCharacter(previewChar.id) },
                                enabled = !isEquipped,
                                shape = CutCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AwakeningGold,
                                    disabledContainerColor = ElevatedPanel
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("lock_in_character_button")
                            ) {
                                Text(
                                    text = if (isEquipped) "EQUIPPED" else "LOCK IN",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isEquipped) MutedSilver else ObsidianVoid
                                )
                            }

                            Button(
                                onClick = {
                                    onSelectCharacter(previewChar.id)
                                    onStartBattle()
                                },
                                shape = CutCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SolarFlareOrange),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(50.dp)
                                    .testTag("start_battle_from_roster_button")
                            ) {
                                Text(
                                    text = "START CLASH",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CrispWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CrispWhite)
                            }
                        }
                    } else {
                        Surface(
                            color = ElevatedPanel,
                            shape = CutCornerShape(10.dp),
                            border = BorderStroke(1.dp, AwakeningGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LOCKED • UNLOCK IN ARMORY (${previewChar.unlockCostGold} GOLD)",
                                style = MaterialTheme.typography.labelMedium,
                                color = AwakeningGold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FighterStatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        color = ElevatedPanel,
        shape = CutCornerShape(8.dp),
        border = BorderStroke(1.dp, HairlineSteel),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedSilver)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.labelLarge, color = accent)
        }
    }
}
