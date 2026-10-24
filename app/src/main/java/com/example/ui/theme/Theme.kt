package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.model.AppThemeMode

val LocalAppPalette = staticCompositionLocalOf { DarkAppPalette }

private val DarkM3Scheme = darkColorScheme(
    primary = DarkAppPalette.primaryAccent,
    onPrimary = Color.White,
    primaryContainer = DarkAppPalette.primaryAccentSoft,
    onPrimaryContainer = Color(0xFFFFB4B6),
    secondary = DarkAppPalette.blueAccent,
    onSecondary = Color.White,
    secondaryContainer = DarkAppPalette.blueAccentSoft,
    onSecondaryContainer = Color(0xFFBDD6FF),
    tertiary = DarkAppPalette.goldAccent,
    onTertiary = Color(0xFF14161C),
    tertiaryContainer = DarkAppPalette.goldAccentSoft,
    onTertiaryContainer = DarkAppPalette.goldAccent,
    background = DarkAppPalette.background,
    onBackground = DarkAppPalette.textPrimary,
    surface = DarkAppPalette.cardSurface,
    onSurface = DarkAppPalette.textPrimary,
    surfaceVariant = DarkAppPalette.elevatedSurface,
    onSurfaceVariant = DarkAppPalette.textSecondary,
    surfaceContainerLowest = DarkAppPalette.stageBackground,
    surfaceContainerLow = DarkAppPalette.background,
    surfaceContainer = DarkAppPalette.cardSurface,
    surfaceContainerHigh = DarkAppPalette.elevatedSurface,
    surfaceContainerHighest = Color(0xFF283244),
    outline = DarkAppPalette.border,
    outlineVariant = DarkAppPalette.border,
    error = DarkAppPalette.primaryAccent,
    onError = Color.White
)

private val LightM3Scheme = lightColorScheme(
    primary = LightAppPalette.primaryAccent,
    onPrimary = Color.White,
    primaryContainer = LightAppPalette.primaryAccentSoft,
    onPrimaryContainer = Color(0xFF7F1D1D),
    secondary = LightAppPalette.blueAccent,
    onSecondary = Color.White,
    secondaryContainer = LightAppPalette.blueAccentSoft,
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = LightAppPalette.goldAccent,
    onTertiary = Color.White,
    tertiaryContainer = LightAppPalette.goldAccentSoft,
    onTertiaryContainer = Color(0xFF78350F),
    background = LightAppPalette.background,
    onBackground = LightAppPalette.textPrimary,
    surface = LightAppPalette.cardSurface,
    onSurface = LightAppPalette.textPrimary,
    surfaceVariant = LightAppPalette.elevatedSurface,
    onSurfaceVariant = LightAppPalette.textSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = LightAppPalette.cardSurface,
    surfaceContainerHigh = LightAppPalette.elevatedSurface,
    surfaceContainerHighest = Color(0xFFDCE4F0),
    outline = LightAppPalette.border,
    outlineVariant = LightAppPalette.border,
    error = LightAppPalette.primaryAccent,
    onError = Color.White
)

object AppTheme {
    val palette: AppPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalAppPalette.current
}

@Composable
fun AnimeBallClashTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val palette = if (useDark) DarkAppPalette else LightAppPalette
    val colorScheme = if (useDark) DarkM3Scheme else LightM3Scheme
    val currentDensity = LocalDensity.current
    val safeDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale.coerceIn(0.88f, 1.18f)
    )

    CompositionLocalProvider(
        LocalAppPalette provides palette,
        LocalDensity provides safeDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
