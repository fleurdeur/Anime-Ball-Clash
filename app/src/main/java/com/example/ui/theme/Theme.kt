package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcadeDarkColorScheme = darkColorScheme(
    primary = SolarFlareOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B190B),
    onPrimaryContainer = CrispWhite,
    secondary = CyberVoltCyan,
    onSecondary = ObsidianVoid,
    secondaryContainer = Color(0xFF082E38),
    onSecondaryContainer = CyberVoltCyan,
    tertiary = AwakeningGold,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = CrispWhite,
    surface = GunmetalSurface,
    onSurface = CrispWhite,
    surfaceVariant = ElevatedPanel,
    onSurfaceVariant = MutedSilver,
    outline = HairlineSteel,
    error = CrimsonFinisher,
    onError = Color.White
)

@Composable
fun AnimeBallClashTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcadeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
