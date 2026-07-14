package com.mcs1000h.remote.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------------------------
// Plain grayscale control-panel palette. No hue anywhere - "active/selected" is communicated by
// value (how dark/light) and weight, not color. Semantic roles (accent/success/danger/warning)
// are kept as separate tokens for the rest of the codebase to key off, but they're all shades of
// gray; danger is pushed to the extreme end (pure black/white) since it's the one state that
// most needs to read as distinct at a glance.
// ---------------------------------------------------------------------------------------------

val AccentLight = Color(0xFF2E2E2E)
val SuccessLight = Color(0xFF2E2E2E)
val DangerLight = Color(0xFF000000)
val WarningLight = Color(0xFF4D4D4D)

val AccentDark = Color(0xFFD0D0D0)
val SuccessDark = Color(0xFFD0D0D0)
val DangerDark = Color(0xFFFFFFFF)
val WarningDark = Color(0xFF8C8C8C)

val OnFillLight = Color(0xFFFFFFFF)
val OnFillDark = Color(0xFF141414)

// Light scheme.
val PaperBackground = Color(0xFFEAEAEA)
val PaperSurface = Color(0xFFF7F7F7)
val PaperSurfaceVariant = Color(0xFFDDDDDD)
val PaperOnSurface = Color(0xFF1C1C1C)
val PaperOnSurfaceVariant = Color(0xFF5A5A5A)
val PaperBorder = Color(0x26000000)
val PaperShadow = Color(0xFF000000)

// Dark scheme.
val EspressoBackground = Color(0xFF161616)
val EspressoSurface = Color(0xFF212121)
val EspressoSurfaceVariant = Color(0xFF2E2E2E)
val EspressoOnSurface = Color(0xFFEDEDED)
val EspressoOnSurfaceVariant = Color(0xFFA8A8A8)
val EspressoBorder = Color(0x26FFFFFF)
val EspressoShadow = Color(0xFF000000)
