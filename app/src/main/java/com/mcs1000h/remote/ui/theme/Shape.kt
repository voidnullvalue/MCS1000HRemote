package com.mcs1000h.remote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Tight, hardware-panel rounding rather than the maxed-out pill radius most mobile UI defaults
// to - these read as machined edges, not a template's border-radius: 9999px.
val CornerTight = RoundedCornerShape(3.dp)
val CornerPanel = RoundedCornerShape(7.dp)
val CornerControl = RoundedCornerShape(5.dp)

val AppShapes = Shapes(
    extraSmall = CornerTight,
    small = CornerControl,
    medium = CornerPanel,
    large = CornerPanel,
    extraLarge = CornerPanel,
)
