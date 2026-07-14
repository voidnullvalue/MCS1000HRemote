package com.mcs1000h.remote.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------------------------
// Aero palette - cool blue-gray glass over a blue/cyan accent, in the spirit of Windows Vista's
// Aero Glass theme. Semantic colors are defined per theme (rather than one hue shared by both)
// because a single saturated tone can't clear WCAG AA text contrast against both a near-white
// and a near-navy surface at once - see AeroPaletteLight/Dark in Aero.kt for how these pair up.
// ---------------------------------------------------------------------------------------------

// Light theme - deep enough to read as text/icons on pale glass and to hold white content on
// top of it as a solid fill.
val AeroAccentLight = Color(0xFF1E6FBF)
val AeroGreenLight = Color(0xFF0C7C42)
val AeroRedLight = Color(0xFFC0392B)
val AeroAmberLight = Color(0xFF995400)

// Dark theme - light enough to read on near-navy glass; paired with dark [OnFillDark] content
// when used as a solid fill, since white text on these doesn't clear contrast either.
val AeroAccentDark = Color(0xFF4FA8E8)
val AeroGreenDark = Color(0xFF4EE092)
val AeroRedDark = Color(0xFFFF7A7D)
val AeroAmberDark = Color(0xFFFFC46B)

// Content color for text/icons placed on a solid semantic fill (buttons, selected segments).
val OnFillLight = Color(0xFFFFFFFF)
val OnFillDark = Color(0xFF0A1826)

// Light "frost" scheme - pale steel-blue desktop, near-opaque frosted content, white glass.
val FrostDesktopTop = Color(0xFFC9DCEF)
val FrostDesktopBottom = Color(0xFFEAF2FA)
val FrostSurface = Color(0xFFF3F8FD)
val FrostSurfaceVariant = Color(0xFFE1EBF5)
val FrostGlassTint = Color(0xFFFFFFFF)
val FrostOnSurface = Color(0xFF152230)
val FrostOnSurfaceVariant = Color(0xFF48607A)
val FrostBorderHighlight = Color(0x99FFFFFF)
val FrostBorderShade = Color(0x33163A5C)
val FrostShadow = Color(0xFF6E8CAD)

// Dark "obsidian glass" scheme - deep navy desktop, dark frosted content, blue glass.
val ObsidianDesktopTop = Color(0xFF0A1826)
val ObsidianDesktopBottom = Color(0xFF16283C)
val ObsidianSurface = Color(0xFF1B2C40)
val ObsidianSurfaceVariant = Color(0xFF24384F)
val ObsidianGlassTint = Color(0xFF1E3A5A)
val ObsidianOnSurface = Color(0xFFE7F0FA)
val ObsidianOnSurfaceVariant = Color(0xFFA9BDD4)
val ObsidianBorderHighlight = Color(0x66FFFFFF)
val ObsidianBorderShade = Color(0x66000814)
val ObsidianShadow = Color(0xFF000814)
