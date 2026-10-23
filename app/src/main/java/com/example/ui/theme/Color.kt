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
    val stageLine: Color
)

// Velvet Matte Dark Palette — Zero harsh glare, deep studio carbon & slate
val DarkAppPalette = AppPalette(
    isDark = true,
    background = Color(0xFF0F1218),
    cardSurface = Color(0xFF171B24),
    elevatedSurface = Color(0xFF212734),
    border = Color(0xFF2A3142),
    strongBorder = Color(0xFF3E4860),
    textPrimary = Color(0xFFE8ECF4),
    textSecondary = Color(0xFF9BA4B8),
    textMuted = Color(0xFF697388),
    primaryAccent = Color(0xFFE04F44),
    primaryAccentSoft = Color(0xFF341C1E),
    blueAccent = Color(0xFF5A91F2),
    blueAccentSoft = Color(0xFF1A2842),
    goldAccent = Color(0xFFE2A73B),
    goldAccentSoft = Color(0xFF332917),
    greenAccent = Color(0xFF3CC27B),
    greenAccentSoft = Color(0xFF163226),
    stageBackground = Color(0xFF0B0E14),
    stageSurface = Color(0xFF131822),
    stageLine = Color(0xFF262E40)
)

// Soft Warm Limestone Light Palette — Gentle muted parchment, zero blinding #FFFFFF
val LightAppPalette = AppPalette(
    isDark = false,
    background = Color(0xFFE6E2D9),
    cardSurface = Color(0xFFF0EDE5),
    elevatedSurface = Color(0xFFDDD8CD),
    border = Color(0xFFCBC5B8),
    strongBorder = Color(0xFF2C313D),
    textPrimary = Color(0xFF1B1E26),
    textSecondary = Color(0xFF525868),
    textMuted = Color(0xFF7B8191),
    primaryAccent = Color(0xFFC83E33),
    primaryAccentSoft = Color(0xFFEAD3CF),
    blueAccent = Color(0xFF2B62C4),
    blueAccentSoft = Color(0xFFD3DFEE),
    goldAccent = Color(0xFFA86D12),
    goldAccentSoft = Color(0xFFE8DEC5),
    greenAccent = Color(0xFF1D834E),
    greenAccentSoft = Color(0xFFCEE4D7),
    stageBackground = Color(0xFF141822),
    stageSurface = Color(0xFF1B202E),
    stageLine = Color(0xFF2D354A)
)
