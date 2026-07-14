package com.mcs1000h.remote.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens Material3's [androidx.compose.material3.ColorScheme] has no room for: panel/border/
 * shadow colors and the semantic accents used by the control primitives in `ui/Controls.kt`.
 * Every color here is pre-checked to clear WCAG AA (4.5:1) as text against [panelSurface], and
 * where used as a solid fill, against [onFill].
 */
@Immutable
data class AppPalette(
    val background: Color,
    val panelSurface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val shadow: Color,
    val accent: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val onFill: Color,
    val isDark: Boolean,
)

val LightPalette = AppPalette(
    background = PaperBackground,
    panelSurface = PaperSurface,
    surfaceVariant = PaperSurfaceVariant,
    border = PaperBorder,
    shadow = PaperShadow,
    accent = AccentLight,
    success = SuccessLight,
    danger = DangerLight,
    warning = WarningLight,
    onFill = OnFillLight,
    isDark = false,
)

val DarkPalette = AppPalette(
    background = EspressoBackground,
    panelSurface = EspressoSurface,
    surfaceVariant = EspressoSurfaceVariant,
    border = EspressoBorder,
    shadow = EspressoShadow,
    accent = AccentDark,
    success = SuccessDark,
    danger = DangerDark,
    warning = WarningDark,
    onFill = OnFillDark,
    isDark = true,
)

val LocalAppPalette = staticCompositionLocalOf { LightPalette }
