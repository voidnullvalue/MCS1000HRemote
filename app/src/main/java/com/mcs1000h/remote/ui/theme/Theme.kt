package com.mcs1000h.remote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = AeroAccentDark,
    onPrimary = OnFillDark,
    secondary = AeroAccentDark,
    tertiary = AeroAmberDark,
    background = ObsidianDesktopBottom,
    onBackground = ObsidianOnSurface,
    surface = ObsidianSurface,
    onSurface = ObsidianOnSurface,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianOnSurfaceVariant,
    surfaceContainer = ObsidianSurface,
    outline = ObsidianBorderShade,
    error = AeroRedDark,
    onError = OnFillDark,
)

private val LightColorScheme = lightColorScheme(
    primary = AeroAccentLight,
    onPrimary = OnFillLight,
    secondary = AeroAccentLight,
    tertiary = AeroAmberLight,
    background = FrostDesktopBottom,
    onBackground = FrostOnSurface,
    surface = FrostSurface,
    onSurface = FrostOnSurface,
    surfaceVariant = FrostSurfaceVariant,
    onSurfaceVariant = FrostOnSurfaceVariant,
    surfaceContainer = FrostSurface,
    outline = FrostBorderShade,
    error = AeroRedLight,
    onError = OnFillLight,
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
