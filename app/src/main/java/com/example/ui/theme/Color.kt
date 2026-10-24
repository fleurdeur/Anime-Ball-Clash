package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppPalette(
    val isDark: Boolean,
    val background: Color,
    val cardSurface: Color,
    val elevatedSurface: Color,
    val border: Color,
    val strongBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val primaryAccentSoft: Color,
    val blueAccent: Color,
    val blueAccentSoft: Color,
    val goldAccent: Color,
    val goldAccentSoft: Color,
    val greenAccent: Color,
    val greenAccentSoft: Color,
    val stageBackground: Color,
    val stageSurface: Color,
    val stageLine: Color,
    val stageOnSurface: Color,
    val stageMuted: Color,
    val overlaySurface: Color,
    val aimReticle: Color,
    val damageTextDefault: Color
)

// Deep Obsidian Dark Palette — Crisp contrast, rich slate surfaces, zero harsh glare
val DarkAppPalette = AppPalette(
    isDark = true,
    background = Color(0xFF0D1117),
    cardSurface = Color(0xFF161B24),
    elevatedSurface = Color(0xFF1F2735),
    border = Color(0xFF2B3548),
    strongBorder = Color(0xFF42506B),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF9BA6BA),
    textMuted = Color(0xFF6B768C),
    primaryAccent = Color(0xFFE5484D),
    primaryAccentSoft = Color(0xFF361A1E),
    blueAccent = Color(0xFF4F8FF7),
    blueAccentSoft = Color(0xFF192946),
    goldAccent = Color(0xFFF0B429),
    goldAccentSoft = Color(0xFF362A14),
    greenAccent = Color(0xFF30C474),
    greenAccentSoft = Color(0xFF153225),
    stageBackground = Color(0xFF0A0E14),
    stageSurface = Color(0xFF121722),
    stageLine = Color(0xFF28334A),
    stageOnSurface = Color(0xFFF1F5F9),
    stageMuted = Color(0xFF94A3B8),
    overlaySurface = Color(0xE6141924),
    aimReticle = Color(0xFFE2E8F0),
    damageTextDefault = Color(0xFFF8FAFC)
)

// Clean Daylight Palette — Soft cool alabaster, crisp white cards, and bright readable arena court
val LightAppPalette = AppPalette(
    isDark = false,
    background = Color(0xFFF1F4F9),
    cardSurface = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFE6ECF5),
    border = Color(0xFFCFD8E6),
    strongBorder = Color(0xFF94A3B8),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    primaryAccent = Color(0xFFDC2626),
    primaryAccentSoft = Color(0xFFFEE2E2),
    blueAccent = Color(0xFF2563EB),
    blueAccentSoft = Color(0xFFDBEAFE),
    goldAccent = Color(0xFFB45309),
    goldAccentSoft = Color(0xFFFEF3C7),
    greenAccent = Color(0xFF15803D),
    greenAccentSoft = Color(0xFFDCFCE7),
    stageBackground = Color(0xFFE2E8F2),
    stageSurface = Color(0xFFEAF0F8),
    stageLine = Color(0xFFCBD5E1),
    stageOnSurface = Color(0xFF0F172A),
    stageMuted = Color(0xFF475569),
    overlaySurface = Color(0xF2FFFFFF),
    aimReticle = Color(0xFF1E293B),
    damageTextDefault = Color(0xFF0F172A)
)
