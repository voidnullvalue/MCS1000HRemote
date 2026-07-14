package com.mcs1000h.remote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Aero glass leans on generous, consistent rounding - panels read as soft glass "pills"
// and "tiles" rather than sharp Material rectangles.
val AeroCornerSmall = RoundedCornerShape(10.dp)
val AeroCornerMedium = RoundedCornerShape(16.dp)
val AeroCornerLarge = RoundedCornerShape(22.dp)
val AeroCornerPill = RoundedCornerShape(50)

val AeroShapes = Shapes(
    extraSmall = AeroCornerSmall,
    small = AeroCornerSmall,
    medium = AeroCornerMedium,
    large = AeroCornerLarge,
    extraLarge = AeroCornerLarge,
)
