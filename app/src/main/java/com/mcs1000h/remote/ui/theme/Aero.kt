package com.mcs1000h.remote.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens that Material3's [androidx.compose.material3.ColorScheme] has no room for: the
 * desktop backdrop gradient, glass tint/border/shadow colors, and the glow accents used by
 * the Aero component set in `ui/AeroComponents.kt`.
 */
@Immutable
data class AeroPalette(
    val desktopTop: Color,
    val desktopBottom: Color,
    val glassTint: Color,
    val cardSurface: Color,
    val cardSurfaceVariant: Color,
    val borderHighlight: Color,
    val borderShade: Color,
    val shadowColor: Color,
    val accent: Color,
    val accentBright: Color,
    val cyan: Color,
    val isDark: Boolean,
)

val AeroPaletteLight = AeroPalette(
    desktopTop = FrostDesktopTop,
    desktopBottom = FrostDesktopBottom,
    glassTint = FrostGlassTint,
    cardSurface = FrostSurface,
    cardSurfaceVariant = FrostSurfaceVariant,
    borderHighlight = FrostBorderHighlight,
    borderShade = FrostBorderShade,
    shadowColor = FrostShadow,
    accent = AeroAccent,
    accentBright = AeroAccentBright,
    cyan = AeroCyan,
    isDark = false,
)

val AeroPaletteDark = AeroPalette(
    desktopTop = ObsidianDesktopTop,
    desktopBottom = ObsidianDesktopBottom,
    glassTint = ObsidianGlassTint,
    cardSurface = ObsidianSurface,
    cardSurfaceVariant = ObsidianSurfaceVariant,
    borderHighlight = ObsidianBorderHighlight,
    borderShade = ObsidianBorderShade,
    shadowColor = ObsidianShadow,
    accent = AeroAccent,
    accentBright = AeroAccentBright,
    cyan = AeroCyan,
    isDark = true,
)

val LocalAeroPalette = staticCompositionLocalOf { AeroPaletteLight }

/**
 * Shared blur source for the current screen's scrolling content, consumed by the glass
 * top bar / nav bar / overlays via `Modifier.hazeEffect`. Null on previews or if a screen
 * doesn't wire one up - glass surfaces fall back to a flat translucent tint in that case.
 */
val LocalHazeState = staticCompositionLocalOf<dev.chrisbanes.haze.HazeState?> { null }
