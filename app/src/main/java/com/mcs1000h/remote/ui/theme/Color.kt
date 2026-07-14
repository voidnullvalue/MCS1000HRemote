package com.mcs1000h.remote.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------------------------
// Warm, dense control-panel palette. No blue/purple "SaaS" accent, no glass. Terracotta reads
// as warmth (fits a heated massage cushion); neutrals are warm paper/espresso rather than cool
// gray. Semantic colors are theme-paired and each checked to clear WCAG AA (>=4.5:1) against
// its own panel surface, and against the onFill content color used when it's a solid fill.
// ---------------------------------------------------------------------------------------------

val AccentLight = Color(0xFFA03D1B)
val SuccessLight = Color(0xFF527035)
val DangerLight = Color(0xFFA83B32)
val WarningLight = Color(0xFF8A6318)

val AccentDark = Color(0xFFE2734A)
val SuccessDark = Color(0xFF8FB35E)
val DangerDark = Color(0xFFE2695E)
val WarningDark = Color(0xFFD9A73B)

val OnFillLight = Color(0xFFFFFFFF)
val OnFillDark = Color(0xFF140F0A)

// Light "paper" scheme.
val PaperBackground = Color(0xFFEFE6D8)
val PaperSurface = Color(0xFFFBF7F0)
val PaperSurfaceVariant = Color(0xFFE7DDCC)
val PaperOnSurface = Color(0xFF2A2018)
val PaperOnSurfaceVariant = Color(0xFF6B5D4C)
val PaperBorder = Color(0x33352418)
val PaperShadow = Color(0xFF3A2B1C)

// Dark "espresso" scheme.
val EspressoBackground = Color(0xFF14100C)
val EspressoSurface = Color(0xFF1E1811)
val EspressoSurfaceVariant = Color(0xFF2A2219)
val EspressoOnSurface = Color(0xFFF0E8DC)
val EspressoOnSurfaceVariant = Color(0xFFB3A28D)
val EspressoBorder = Color(0x26FFFFFF)
val EspressoShadow = Color(0xFF000000)
