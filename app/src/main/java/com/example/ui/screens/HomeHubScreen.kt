package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppScreen
import com.example.model.BotDifficulty
import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import com.example.model.EngineSettings
import com.example.model.GameMode
import com.example.model.PlayerProfile
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.GraphicsConfigDialog
import com.example.ui.components.HubNavigationBar
import com.example.ui.theme.AppTheme
import java.util.Locale

@Composable
fun HomeHubScreen(
    profile: PlayerProfile,
    activeUpgrades: CharacterUpgradeState,
    unlockedCount: Int,
    claimableQuestCount: Int,
    selectedDifficulty: BotDifficulty,
    engineSettings: EngineSettings,
    onNavigate: (AppScreen) -> Unit,
    onCycleCharacter: (Int) -> Unit,
    onSelectDifficulty: (BotDifficulty) -> Unit,
    onStartBattle: (GameMode, BotDifficulty) -> Unit,
    onUpdateUsername: (String) -> Unit,
    onSaveEngineSettings: (EngineSettings) -> Unit,
    onQuickToggleTheme: () -> Unit,
    onResetAccount: () -> Unit
) {
    val palette = AppTheme.palette
    var selectedMode by remember { mutableStateOf(GameMode.PRACTICE_BOT) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var draftName by remember(profile.username) { mutableStateOf(profile.username) }

    val activeCharacter = remember(profile.selectedCharacterId) {
        CharacterRoster.getById(profile.selectedCharacterId)
    }

    val effHp = activeUpgrades.effectiveMaxHp(activeCharacter.maxHp)
    val effDmg = activeUpgrades.effectiveBaseDamage(activeCharacter.baseDamage)
    val effCd = activeUpgrades.effectiveUltimateCooldown(activeCharacter.ultimateCooldownSeconds)

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            HubNavigationBar(
                currentScreen = AppScreen.HOME_HUB,
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
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("home_hub_screen"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Sleek Top Account & Theme Bar
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        draftName = profile.username
                                        showRenameDialog = true
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .testTag("edit_username_button")
                            ) {
                                Surface(
                                    color = palette.elevatedSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, palette.border)
                                ) {
                                    Text(
                                        text = "LV ${profile.level}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.textPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.username,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = "Edit Name",
                                            tint = palette.textMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "${profile.rankTier} • ${profile.wins}W ${profile.losses}L",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CurrencyPill(
                                    label = "${formatGold(profile.gold)} G",
                                    bgColor = palette.goldAccentSoft,
                                    textColor = palette.goldAccent
                                )
                                IconButton(
                                    onClick = onQuickToggleTheme,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(palette.elevatedSurface)
                                        .testTag("quick_theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (palette.isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                        contentDescription = "Toggle Dark or Light Mode",
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { showSettingsDialog = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(palette.elevatedSurface)
                                        .testTag("open_settings_button")
                                ) {
                                    Icon(
                                        Icons.Outlined.Tune,
                                        contentDescription = "Settings",
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = {
                                    (profile.exp.toFloat() / profile.maxExp.coerceAtLeast(1)).coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = palette.blueAccent,
                                trackColor = palette.elevatedSurface
                            )
                            Text(
                                text = "${profile.exp}/${profile.maxExp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted
                            )
                        }
                    }
                }

                // 2. Atmospheric Fighter Showcase Stage Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(palette.stageSurface)
                                .border(1.dp, palette.stageLine, RoundedCornerShape(18.dp))
                                .drawBehind {
                                    drawCircle(
                                        color = activeCharacter.auraColor.copy(alpha = 0.16f),
                                        radius = size.minDimension * 0.56f,
                                        center = Offset(size.width * 0.5f, size.height * 0.54f)
                                    )
                                }
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isVillain = activeCharacter.alignment == com.example.model.CharacterAlignment.ANTAGONIST
                                        Surface(
                                            color = if (isVillain) Color(0xFF3D1924) else Color(0xFF172D44),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = activeCharacter.alignment.badgeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isVillain) Color(0xFFFF8A9B) else Color(0xFF7CC4FA),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            color = Color(0xFF232A3B),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (activeUpgrades.totalUpgrades > 0) {
                                                    "${activeCharacter.classType.displayName} • +${activeUpgrades.totalUpgrades} Forged"
                                                } else {
                                                    "${activeCharacter.classType.displayName} • ${activeCharacter.popularityTier.displayName}"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFD6DEEE),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = { onNavigate(AppScreen.CHARACTER_SELECT) },
                                        color = Color(0xFF232A3B),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("roster_shortcut_button")
                                    ) {
                                        Text(
                                            text = "$unlockedCount/${CharacterRoster.characters.size} Roster",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF8AB4F8),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { onCycleCharacter(-1) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFF232A3B), CircleShape)
                                            .testTag("prev_fighter_button")
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Previous Fighter",
                                            tint = Color(0xFFE8ECF4)
                                        )
                                    }

                                    Canvas(
                                        modifier = Modifier
                                            .size(134.dp)
                                            .clip(CircleShape)
                                            .clickable { onNavigate(AppScreen.CHARACTER_SELECT) }
                                    ) {
                                        AnimeBallRenderer.drawPreviewAvatar(
                                            scope = this,
                                            character = activeCharacter,
                                            weaponAngle = 0.68f
                                        )
                                    }

                                    IconButton(
                                        onClick = { onCycleCharacter(1) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFF232A3B), CircleShape)
                                            .testTag("next_fighter_button")
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Next Fighter",
                                            tint = Color(0xFFE8ECF4)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = activeCharacter.name,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Color(0xFFF1F5F9)
                                )
                                Text(
                                    text = "${activeCharacter.title} • ${activeCharacter.animeInspiration}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Crisp 4-column upgraded tactical stat chips (HP, DMG, SPEED, ULT CD)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatPillCard("MAX HP", "$effHp", Modifier.weight(1f))
                            StatPillCard("POWER", "$effDmg", Modifier.weight(1f))
                            StatPillCard("SPEED", String.format(Locale.US, "%.2fx", activeCharacter.speedRating), Modifier.weight(1f))
                            StatPillCard("ULT CD", String.format(Locale.US, "%.1fs", effCd), Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.SHOP) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("upgrade_shortcut_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = palette.goldAccent, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Upgrade Health, Power & Lower CD • Recruit 220+ Fighters",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.goldAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = palette.border)
                        Spacer(modifier = Modifier.height(10.dp))

                        TraitLine(
                            badge = "PASSIVE",
                            badgeBg = palette.blueAccentSoft,
                            badgeFg = palette.blueAccent,
                            title = "${activeCharacter.passiveName} (${activeCharacter.passiveArchetype.shortTag})",
                            desc = activeCharacter.passiveDescription
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TraitLine(
                            badge = "ULTIMATE",
                            badgeBg = palette.primaryAccentSoft,
                            badgeFg = palette.primaryAccent,
                            title = activeCharacter.ultimateName,
                            desc = activeCharacter.ultimateDescription
                        )
                    }
                }

                // 3. Match Mode & Nonstop Clash Launcher
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Combat Mode",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Text(
                                text = selectedMode.tagLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.blueAccent
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameMode.entries.forEach { mode ->
                                val selected = selectedMode == mode
                                Surface(
                                    onClick = { selectedMode = mode },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) palette.blueAccentSoft else palette.elevatedSurface,
                                    border = BorderStroke(1.dp, if (selected) palette.blueAccent else palette.border),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("mode_${mode.name.lowercase()}")
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                        Text(
                                            text = mode.title.substringBefore(" "),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) palette.blueAccent else palette.textPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = selectedMode.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )

                        if (selectedMode == GameMode.PRACTICE_BOT) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "AI Tier",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textSecondary
                                )
                                BotDifficulty.entries.forEach { diff ->
                                    val active = selectedDifficulty == diff
                                    Surface(
                                        onClick = { onSelectDifficulty(diff) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (active) palette.goldAccentSoft else palette.elevatedSurface,
                                        border = BorderStroke(1.dp, if (active) palette.goldAccent else Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = diff.title,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (active) palette.goldAccent else palette.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { onStartBattle(selectedMode, selectedDifficulty) },
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primaryAccent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("enter_arena_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Launch Nonstop Clash",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        GraphicsConfigDialog(
            currentSettings = engineSettings,
            selectedCharacterId = profile.selectedCharacterId,
            onSaveSettings = onSaveEngineSettings,
            onResetAccount = onResetAccount,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = palette.cardSurface,
            title = {
                Text("Player Name", style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
            },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it.take(16) },
                    singleLine = true,
                    label = { Text("Display Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUsername(draftName)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primaryAccent, contentColor = Color.White)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun CurrencyPill(label: String, bgColor: Color, textColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatPillCard(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = AppTheme.palette
    Surface(
        color = palette.elevatedSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
        }
    }
}

@Composable
private fun TraitLine(
    badge: String,
    badgeBg: Color,
    badgeFg: Color,
    title: String,
    desc: String
) {
    val palette = AppTheme.palette
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
        }
        Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
    }
}
