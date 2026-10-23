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
import com.example.model.AppThemeMode

val LocalAppPalette = staticCompositionLocalOf { DarkAppPalette }

private val DarkM3Scheme = darkColorScheme(
    primary = DarkAppPalette.primaryAccent,
    onPrimary = Color(0xFFF8FAFC),
    primaryContainer = DarkAppPalette.primaryAccentSoft,
    onPrimaryContainer = DarkAppPalette.primaryAccent,
    secondary = DarkAppPalette.blueAccent,
    onSecondary = Color(0xFFF8FAFC),
    secondaryContainer = DarkAppPalette.blueAccentSoft,
    onSecondaryContainer = DarkAppPalette.blueAccent,
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
    outline = DarkAppPalette.border,
    error = DarkAppPalette.primaryAccent,
    onError = Color.White
)

private val LightM3Scheme = lightColorScheme(
    primary = LightAppPalette.primaryAccent,
    onPrimary = Color(0xFFF8FAFC),
    primaryContainer = LightAppPalette.primaryAccentSoft,
    onPrimaryContainer = LightAppPalette.primaryAccent,
    secondary = LightAppPalette.blueAccent,
    onSecondary = Color(0xFFF8FAFC),
    secondaryContainer = LightAppPalette.blueAccentSoft,
    onSecondaryContainer = LightAppPalette.blueAccent,
    tertiary = LightAppPalette.goldAccent,
    onTertiary = Color.White,
    tertiaryContainer = LightAppPalette.goldAccentSoft,
    onTertiaryContainer = LightAppPalette.goldAccent,
    background = LightAppPalette.background,
    onBackground = LightAppPalette.textPrimary,
    surface = LightAppPalette.cardSurface,
    onSurface = LightAppPalette.textPrimary,
    surfaceVariant = LightAppPalette.elevatedSurface,
    onSurfaceVariant = LightAppPalette.textSecondary,
    outline = LightAppPalette.border,
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

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
