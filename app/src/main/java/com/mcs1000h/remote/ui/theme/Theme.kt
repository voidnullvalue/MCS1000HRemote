package com.mcs1000h.remote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = OnFillDark,
    secondary = AccentDark,
    tertiary = WarningDark,
    background = EspressoBackground,
    onBackground = EspressoOnSurface,
    surface = EspressoSurface,
    onSurface = EspressoOnSurface,
    surfaceVariant = EspressoSurfaceVariant,
    onSurfaceVariant = EspressoOnSurfaceVariant,
    surfaceContainer = EspressoSurface,
    outline = EspressoBorder,
    error = DangerDark,
    onError = OnFillDark,
)

private val LightColorScheme = lightColorScheme(
    primary = AccentLight,
    onPrimary = OnFillLight,
    secondary = AccentLight,
    tertiary = WarningLight,
    background = PaperBackground,
    onBackground = PaperOnSurface,
    surface = PaperSurface,
    onSurface = PaperOnSurface,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = PaperOnSurfaceVariant,
    surfaceContainer = PaperSurface,
    outline = PaperBorder,
    error = DangerLight,
    onError = OnFillLight,
)

@Composable
fun MCS1000HRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appPalette = if (darkTheme) DarkPalette else LightPalette

    CompositionLocalProvider(LocalAppPalette provides appPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
