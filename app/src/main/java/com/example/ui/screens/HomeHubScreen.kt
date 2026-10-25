package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
    onResetAccount: () -> Unit,
    onSignOut: (() -> Unit)? = null
) {
    val palette = AppTheme.palette
    var selectedMode by remember { mutableStateOf(GameMode.PRACTICE_BOT) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var previewUltOnStage by remember(profile.selectedCharacterId) { mutableStateOf(false) }
    var draftName by remember(profile.username) { mutableStateOf(profile.username) }
    var lastCycleDirection by remember { mutableIntStateOf(1) }

    val activeCharacter = remember(profile.selectedCharacterId) {
        CharacterRoster.getById(profile.selectedCharacterId)
    }

    // Live 60fps continuous stage animations (orbiting weapon/Stand, floating bob, pulsing aura & CTA)
    val stageTransition = rememberInfiniteTransition(label = "home_stage_motion")
    val liveOrbitAngle by stageTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2.0).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liveOrbitAngle"
    )
    val floatBobPx by stageTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatBobPx"
    )
    val auraPulse by stageTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraPulse"
    )
    val ctaPulse by stageTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.022f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 960, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ctaPulse"
    )

    val ultPreviewScale by animateFloatAsState(
        targetValue = if (previewUltOnStage) 1.07f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ultPreviewScale"
    )

    val entranceLift = remember { Animatable(0.95f) }
    LaunchedEffect(Unit) {
        entranceLift.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    BackHandler(enabled = showSettingsDialog || showRenameDialog) {
        if (showSettingsDialog) showSettingsDialog = false
        else if (showRenameDialog) showRenameDialog = false
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isCompactPhone = maxWidth < 360.dp || maxHeight < 660.dp
            val showcaseAvatarSize = if (isCompactPhone) 108.dp else 134.dp

            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isCompactPhone) 12.dp else 16.dp, vertical = 12.dp)
                    .testTag("home_hub_screen"),
                verticalArrangement = Arrangement.spacedBy(if (isCompactPhone) 11.dp else 14.dp)
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

                // 2. Atmospheric Animated Fighter Showcase Stage Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = entranceLift.value
                            scaleY = entranceLift.value
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(palette.stageSurface)
                                .border(1.dp, palette.stageLine, RoundedCornerShape(18.dp))
                                .drawBehind {
                                    val center = Offset(size.width * 0.5f, size.height * 0.52f)
                                    val baseR = size.minDimension * 0.48f
                                    drawCircle(
                                        color = activeCharacter.auraColor.copy(
                                            alpha = if (previewUltOnStage) 0.26f else 0.16f
                                        ),
                                        radius = baseR * auraPulse,
                                        center = center
                                    )
                                    drawCircle(
                                        color = activeCharacter.auraColor.copy(
                                            alpha = if (previewUltOnStage) 0.42f else 0.22f
                                        ),
                                        radius = baseR * (0.72f + (auraPulse - 0.88f) * 0.85f),
                                        center = center,
                                        style = Stroke(width = if (previewUltOnStage) 2.6f else 1.6f)
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
                                            color = if (isVillain) palette.primaryAccentSoft else palette.blueAccentSoft,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = activeCharacter.alignment.badgeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isVillain) palette.primaryAccent else palette.blueAccent,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            color = palette.cardSurface,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, palette.border)
                                        ) {
                                            Text(
                                                text = if (activeUpgrades.totalUpgrades > 0) {
                                                    "${activeCharacter.classType.displayName} • +${activeUpgrades.totalUpgrades} Up"
                                                } else {
                                                    "${activeCharacter.classType.displayName} • ${activeCharacter.popularityTier.displayName}"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.textSecondary,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            onClick = { previewUltOnStage = !previewUltOnStage },
                                            color = if (previewUltOnStage) palette.goldAccentSoft else palette.cardSurface,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (previewUltOnStage) palette.goldAccent else palette.border),
                                            modifier = Modifier.testTag("home_ult_preview_toggle")
                                        ) {
                                            Text(
                                                text = if (previewUltOnStage) "Ult ON" else "Show Ult",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (previewUltOnStage) palette.goldAccent else palette.textSecondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = { onNavigate(AppScreen.CHARACTER_SELECT) },
                                            color = palette.cardSurface,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, palette.border),
                                            modifier = Modifier.testTag("roster_shortcut_button")
                                        ) {
                                            Text(
                                                text = "$unlockedCount/${CharacterRoster.characters.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.blueAccent,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = {
                                            lastCycleDirection = -1
                                            onCycleCharacter(-1)
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(palette.cardSurface, CircleShape)
                                            .border(1.dp, palette.border, CircleShape)
                                            .testTag("prev_fighter_button")
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Previous Fighter",
                                            tint = palette.stageOnSurface
                                        )
                                    }

                                    AnimatedContent(
                                        targetState = activeCharacter,
                                        transitionSpec = {
                                            val dir = if (lastCycleDirection >= 0) 1 else -1
                                            (slideInHorizontally(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                ),
                                                initialOffsetX = { fullW -> dir * (fullW / 2) }
                                            ) + fadeIn(tween(200)) + scaleIn(initialScale = 0.84f, animationSpec = tween(220)))
                                                .togetherWith(
                                                    slideOutHorizontally(
                                                        animationSpec = tween(180, easing = FastOutSlowInEasing),
                                                        targetOffsetX = { fullW -> -dir * (fullW / 2) }
                                                    ) + fadeOut(tween(150)) + scaleOut(targetScale = 0.86f, animationSpec = tween(160))
                                                )
                                                .using(SizeTransform(clip = false))
                                        },
                                        label = "HomeShowcaseAvatar"
                                    ) { shownChar ->
                                        Canvas(
                                            modifier = Modifier
                                                .size(showcaseAvatarSize)
                                                .graphicsLayer {
                                                    translationY = floatBobPx
                                                    scaleX = ultPreviewScale
                                                    scaleY = ultPreviewScale
                                                }
                                                .clip(CircleShape)
                                                .clickable { previewUltOnStage = !previewUltOnStage }
                                        ) {
                                            val spinMultiplier = if (previewUltOnStage) 2.0f else 1.0f
                                            AnimeBallRenderer.drawPreviewAvatar(
                                                scope = this,
                                                character = shownChar,
                                                weaponAngle = (liveOrbitAngle * spinMultiplier) % (Math.PI * 2.0).toFloat(),
                                                isUltimatePreview = previewUltOnStage
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            lastCycleDirection = 1
                                            onCycleCharacter(1)
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(palette.cardSurface, CircleShape)
                                            .border(1.dp, palette.border, CircleShape)
                                            .testTag("next_fighter_button")
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Next Fighter",
                                            tint = palette.stageOnSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                AnimatedContent(
                                    targetState = activeCharacter,
                                    transitionSpec = {
                                        val dir = if (lastCycleDirection >= 0) 1 else -1
                                        (slideInHorizontally(
                                            animationSpec = tween(210, easing = FastOutSlowInEasing),
                                            initialOffsetX = { w -> dir * (w / 5) }
                                        ) + fadeIn(tween(190)))
                                            .togetherWith(
                                                slideOutHorizontally(
                                                    animationSpec = tween(160, easing = FastOutSlowInEasing),
                                                    targetOffsetX = { w -> -dir * (w / 5) }
                                                ) + fadeOut(tween(130))
                                            )
                                    },
                                    label = "HomeFighterIdentity"
                                ) { charInfo ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = charInfo.name,
                                            style = MaterialTheme.typography.displayMedium,
                                            color = palette.stageOnSurface
                                        )
                                        Text(
                                            text = "${charInfo.title} • ${charInfo.animeInspiration}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = palette.stageMuted
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Surface(
                                            color = palette.cardSurface.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(1.dp, palette.border)
                                        ) {
                                            Text(
                                                text = "${charInfo.animeRace.simpleName} • ${charInfo.worldScaleTier.simpleName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.blueAccent,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                        if (charInfo.hasStandSpirit) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                color = palette.goldAccentSoft,
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.45f))
                                            ) {
                                                Text(
                                                    text = "STAND: ${charInfo.joJoStandStyle.standName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.goldAccent,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatPillCard("HP", "$effHp", Modifier.weight(1f))
                            StatPillCard("POWER", "$effDmg", Modifier.weight(1f))
                            StatPillCard("SPEED", String.format(Locale.US, "%.2fx", activeCharacter.speedRating), Modifier.weight(1f))
                            StatPillCard("ULT WAIT", String.format(Locale.US, "%.1fs", effCd), Modifier.weight(1f))
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
                                text = "Upgrades & Fighter Shop",
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
                            desc = activeCharacter.simplePassiveSummary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TraitLine(
                            badge = "COUNTER",
                            badgeBg = palette.greenAccentSoft,
                            badgeFg = palette.greenAccent,
                            title = "${activeCharacter.counterArchetype.simpleName} (Random)",
                            desc = activeCharacter.simpleAutoCounterSummary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TraitLine(
                            badge = "ULTIMATE",
                            badgeBg = palette.primaryAccentSoft,
                            badgeFg = palette.primaryAccent,
                            title = activeCharacter.ultimateName,
                            desc = activeCharacter.simpleUltimateSummary
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
                                text = "Game Mode",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Text(
                                text = selectedMode.tagLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.blueAccent
                            )
                        }

                        val modeRows = remember { GameMode.entries.chunked(2) }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            modeRows.forEach { rowModes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowModes.forEach { mode ->
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
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 12.dp)
                                            ) {
                                                Text(
                                                    text = mode.title,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (selected) palette.blueAccent else palette.textPrimary,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = mode.tagLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (selected) palette.blueAccent else palette.textMuted,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedContent(
                            targetState = selectedMode,
                            transitionSpec = {
                                fadeIn(tween(180)).togetherWith(fadeOut(tween(120)))
                            },
                            label = "HomeModeSubtitle"
                        ) { mode ->
                            Text(
                                text = mode.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                        }

                        AnimatedVisibility(
                            visible = selectedMode == GameMode.PRACTICE_BOT,
                            enter = expandVertically(animationSpec = tween(220)) + fadeIn(tween(180)),
                            exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(tween(140))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Bot",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textSecondary
                                )
                                BotDifficulty.entries.forEach { diff ->
                                    val active = selectedDifficulty == diff
                                    val diffLabel = when (diff) {
                                        BotDifficulty.EASY -> "Easy"
                                        BotDifficulty.MEDIUM -> "Normal • Hard"
                                        BotDifficulty.HARD -> "Hard • Extreme"
                                    }
                                    Surface(
                                        onClick = { onSelectDifficulty(diff) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (active) palette.goldAccentSoft else palette.elevatedSurface,
                                        border = BorderStroke(1.dp, if (active) palette.goldAccent else Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("hub_diff_${diff.name.lowercase()}")
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                                            Text(
                                                text = diffLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (active) palette.goldAccent else palette.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val actionLabel = when (selectedMode) {
                            GameMode.PRACTICE_BOT -> "Play Now"
                            GameMode.CUSTOM_MATCH -> "Pick Any 2 Fighters"
                            GameMode.MULTIPLAYER -> "Play With Friends"
                            GameMode.ONLINE_MATCH -> "Find Online Match"
                        }

                        Button(
                            onClick = {
                                when (selectedMode) {
                                    GameMode.CUSTOM_MATCH -> onNavigate(AppScreen.CUSTOM_MATCH)
                                    GameMode.MULTIPLAYER -> onNavigate(AppScreen.MULTIPLAYER_LOBBY)
                                    else -> onStartBattle(selectedMode, selectedDifficulty)
                                }
                            },
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primaryAccent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    scaleX = ctaPulse
                                    scaleY = ctaPulse
                                }
                                .testTag("enter_arena_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
            onSignOut = onSignOut,
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
