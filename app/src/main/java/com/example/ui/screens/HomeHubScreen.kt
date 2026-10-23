package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppScreen
import com.example.model.BotDifficulty
import com.example.model.CharacterRoster
import com.example.model.CombatBall
import com.example.model.EngineSettings
import com.example.model.GameMode
import com.example.model.PlayerProfile
import com.example.model.Vector2
import com.example.model.VerletHairNode
import com.example.ui.components.AnimeBallRenderer
import com.example.ui.components.GraphicsConfigDialog
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
fun HomeHubScreen(
    profile: PlayerProfile,
    engineSettings: EngineSettings,
    onNavigate: (AppScreen) -> Unit,
    onCycleCharacter: (Int) -> Unit,
    onStartBattle: (GameMode, BotDifficulty) -> Unit,
    onUpdateUsername: (String) -> Unit,
    onSaveEngineSettings: (EngineSettings) -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newUsernameText by remember(profile.username) { mutableStateOf(profile.username) }

    var selectedMode by remember { mutableStateOf(GameMode.PRACTICE_BOT) }
    var selectedBotDiff by remember { mutableStateOf(BotDifficulty.MEDIUM) }

    var tapJumpImpulse by remember { mutableFloatStateOf(0f) }
    var showVoiceBanner by remember { mutableStateOf(false) }
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = nanos
                animTime += dt
                if (tapJumpImpulse > 0f) {
                    tapJumpImpulse = (tapJumpImpulse - dt * 2.8f).coerceAtLeast(0f)
                }
            }
        }
    }

    val selectedChar = remember(profile.selectedCharacterId) {
        CharacterRoster.getById(profile.selectedCharacterId)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hubHover")
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverY"
    )

    val jumpOffset by animateFloatAsState(
        targetValue = if (tapJumpImpulse > 0f) -26f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "jumpOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .testTag("home_hub_screen")
    ) {
        // 1. TOP COMMAND HEADER
        Surface(
            color = SlateAbyss,
            border = BorderStroke(1.dp, HairlineSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player Identity Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CutCornerShape(8.dp))
                            .clickable { showEditNameDialog = true }
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CutCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(SolarFlareOrange, CrimsonFinisher))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LV.${profile.level}",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrispWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CrispWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Callsign",
                                    tint = CyberVoltCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "${profile.rankTier} • ${profile.rankedRating} MMR",
                                style = MaterialTheme.typography.labelSmall,
                                color = AwakeningGold
                            )
                        }
                    }

                    // Currency Capsules + Engine Gear
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrencyPill(
                            icon = Icons.Default.MonetizationOn,
                            amount = "${profile.gold}",
                            tint = AwakeningGold
                        )
                        CurrencyPill(
                            icon = Icons.Default.Diamond,
                            amount = "${profile.gems}",
                            tint = CyberVoltCyan
                        )
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CutCornerShape(8.dp))
                                .background(ElevatedPanel)
                                .testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Engine Settings",
                                tint = CrispWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sleek XP Progress Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ElevatedPanel)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((profile.exp.toFloat() / profile.maxExp).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(CyberVoltCyan, EmeraldVitality)))
                    )
                }
            }
        }

        // 2. SCROLLABLE STAGE SHOWCASE + ARCADE BATTLE DECK
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HERO TOURNAMENT STAGE CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .clip(CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp))
                    .border(1.5.dp, HairlineSteel, CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp))
                    .clickable {
                        tapJumpImpulse = 1f
                        showVoiceBanner = true
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_stage_1792790796382),
                    contentDescription = "Tournament Showcase Stage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianVoid.copy(alpha = 0.65f),
                                    Color.Transparent,
                                    ObsidianVoid.copy(alpha = 0.90f)
                                )
                            )
                        )
                )

                // Top Row: Element + Class Tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(selectedChar.element.badgeColor).copy(alpha = 0.22f),
                        shape = CutCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(selectedChar.element.badgeColor))
                    ) {
                        Text(
                            text = "${selectedChar.element.displayName} • ${selectedChar.classType.displayName.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrispWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = ObsidianVoid.copy(alpha = 0.65f),
                        shape = CutCornerShape(6.dp),
                        border = BorderStroke(1.dp, HairlineSteel)
                    ) {
                        Text(
                            text = "ATK ${selectedChar.baseDamage} • SPD x${selectedChar.speedRating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AwakeningGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Center Hovering Combat Sphere + Quick Cycle Arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onCycleCharacter(-1) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ObsidianVoid.copy(alpha = 0.65f))
                            .border(1.dp, HairlineSteel, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Sphere",
                            tint = CrispWhite
                        )
                    }

                    Canvas(modifier = Modifier.size(150.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f + hoverY + jumpOffset
                        val r = size.minDimension * 0.30f
                        val ball = CombatBall(
                            id = "hub_showcase",
                            name = selectedChar.name,
                            position = Vector2(cx, cy),
                            velocity = Vector2.Zero,
                            radius = r,
                            character = selectedChar,
                            isUltimateActive = tapJumpImpulse > 0.3f,
                            weaponAngle = animTime * 2.5f,
                            hairNodes = List(5) { idx ->
                                VerletHairNode(
                                    cx,
                                    cy - r * 0.5f - idx * (r * 0.30f),
                                    cx,
                                    cy - r * 0.5f - idx * (r * 0.30f)
                                )
                            }
                        )
                        AnimeBallRenderer.drawBall(
                            scope = this,
                            ball = ball,
                            renderMode = engineSettings.renderMode,
                            physicsMesh = engineSettings.physicsMesh,
                            globalTime = animTime
                        )
                    }

                    IconButton(
                        onClick = { onCycleCharacter(1) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ObsidianVoid.copy(alpha = 0.65f))
                            .border(1.dp, HairlineSteel, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Sphere",
                            tint = CrispWhite
                        )
                    }
                }

                // Bottom Fighter Nameplate & Voice Callout
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    AnimatedVisibility(
                        visible = showVoiceBanner && tapJumpImpulse > 0f,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            color = SolarFlareOrange,
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "\"${selectedChar.voiceLine}\"",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrispWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedChar.name.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = CrispWhite
                            )
                            Text(
                                text = "PASSIVE: ${selectedChar.passiveName} • ULT: ${selectedChar.ultimateName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberVoltCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "TAP SPHERE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSilver
                        )
                    }
                }
            }

            // 3. UNIFIED ARCADE BATTLE COMMAND DECK
            Surface(
                color = GunmetalSurface,
                shape = CutCornerShape(14.dp),
                border = BorderStroke(1.dp, HairlineSteel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mode Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GameMode.values().forEach { mode ->
                            val active = selectedMode == mode
                            val testTag = when (mode) {
                                GameMode.ONLINE_MATCH -> "mode_online_button"
                                GameMode.FRIENDS_LOBBY -> "mode_friends_button"
                                GameMode.PRACTICE_BOT -> "mode_bot_tab"
                            }
                            Surface(
                                onClick = { selectedMode = mode },
                                color = if (active) CyberVoltCyan.copy(alpha = 0.18f) else ElevatedPanel,
                                shape = CutCornerShape(8.dp),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (active) CyberVoltCyan else HairlineSteel
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag(testTag)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (active) CyberVoltCyan else CrispWhite,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = mode.tagLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) AwakeningGold else MutedSilver,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = selectedMode.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSilver
                    )

                    // AI Difficulty Selector Row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "RIVAL AI DIFFICULTY & BANK-SHOT AGGRESSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = AwakeningGold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BotDifficulty.values().forEach { diff ->
                                val isSel = selectedBotDiff == diff
                                Surface(
                                    onClick = { selectedBotDiff = diff },
                                    color = if (isSel) AwakeningGold else ElevatedPanel,
                                    shape = CutCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isSel) AwakeningGold else HairlineSteel),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = diff.shortLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSel) ObsidianVoid else CrispWhite
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PRIMARY LAUNCH BATTLE CTA
                    Button(
                        onClick = { onStartBattle(selectedMode, selectedBotDiff) },
                        shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolarFlareOrange),
                        border = BorderStroke(1.5.dp, AwakeningGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_practice_bot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = AwakeningGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENTER ARENA CLASH",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CrispWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = CrispWhite
                        )
                    }
                }
            }
        }

        // 4. SAFE-AREA BOTTOM NAVIGATION DOCK
        Surface(
            color = SlateAbyss,
            border = BorderStroke(1.dp, HairlineSteel),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HubDockButton("FIGHTERS", Icons.Default.Group, SolarFlareOrange, "nav_roster") {
                    onNavigate(AppScreen.CHARACTER_SELECT)
                }
                HubDockButton("ARMORY", Icons.Default.ShoppingBag, AwakeningGold, "nav_shop") {
                    onNavigate(AppScreen.SHOP)
                }
                HubDockButton("MISSIONS", Icons.Default.Assignment, CyberVoltCyan, "nav_quests") {
                    onNavigate(AppScreen.QUESTS)
                }
                HubDockButton("LADDER", Icons.Default.EmojiEvents, EmeraldVitality, "nav_leaderboard") {
                    onNavigate(AppScreen.LEADERBOARD)
                }
            }
        }
    }

    if (showSettingsDialog) {
        GraphicsConfigDialog(
            currentSettings = engineSettings,
            onSaveSettings = onSaveEngineSettings,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = GunmetalSurface,
            title = {
                Text("FIGHTER CALLSIGN", style = MaterialTheme.typography.headlineMedium, color = CrispWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your arena display name (up to 16 characters):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSilver
                    )
                    OutlinedTextField(
                        value = newUsernameText,
                        onValueChange = { newUsernameText = it.take(16) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CrispWhite,
                            unfocusedTextColor = CrispWhite,
                            focusedBorderColor = CyberVoltCyan,
                            unfocusedBorderColor = HairlineSteel
                        ),
                        modifier = Modifier.testTag("username_edit_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUsername(newUsernameText)
                        showEditNameDialog = false
                    },
                    shape = CutCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberVoltCyan),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("SAVE", style = MaterialTheme.typography.labelLarge, color = ObsidianVoid)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = MutedSilver)
                }
            }
        )
    }
}

@Composable
private fun CurrencyPill(icon: ImageVector, amount: String, tint: Color) {
    Surface(
        color = ElevatedPanel,
        shape = CutCornerShape(6.dp),
        border = BorderStroke(1.dp, HairlineSteel)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = amount, style = MaterialTheme.typography.labelMedium, color = CrispWhite)
        }
    }
}

@Composable
private fun HubDockButton(
    label: String,
    icon: ImageVector,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = ElevatedPanel.copy(alpha = 0.65f),
        shape = CutCornerShape(8.dp),
        border = BorderStroke(1.dp, HairlineSteel),
        modifier = Modifier
            .height(52.dp)
            .width(82.dp)
            .testTag(tag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = CrispWhite, fontSize = 9.sp)
        }
    }
}
