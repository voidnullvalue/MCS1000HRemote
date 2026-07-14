package com.mcs1000h.remote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AeroAccent,
    onPrimary = Color(0xFF04121F),
    secondary = AeroCyan,
    tertiary = AeroAmber,
    background = ObsidianDesktopBottom,
    onBackground = ObsidianOnSurface,
    surface = ObsidianSurface,
    onSurface = ObsidianOnSurface,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianOnSurfaceVariant,
    surfaceContainer = ObsidianSurface,
    outline = ObsidianBorderShade,
    error = AeroRed,
)

private val LightColorScheme = lightColorScheme(
    primary = AeroAccent,
    onPrimary = Color.White,
    secondary = AeroCyan,
    tertiary = AeroAmber,
    background = FrostDesktopBottom,
    onBackground = FrostOnSurface,
    surface = FrostSurface,
    onSurface = FrostOnSurface,
    surfaceVariant = FrostSurfaceVariant,
    onSurfaceVariant = FrostOnSurfaceVariant,
    surfaceContainer = FrostSurface,
    outline = FrostBorderShade,
    error = AeroRed,
)

@Composable
fun MCS1000HRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val aeroPalette = if (darkTheme) AeroPaletteDark else AeroPaletteLight

    CompositionLocalProvider(LocalAeroPalette provides aeroPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AeroShapes,
            content = content,
        )
    }
}
