package com.mcs1000h.remote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ui.theme.AeroCornerMedium
import com.mcs1000h.remote.ui.theme.AeroCornerPill
import com.mcs1000h.remote.ui.theme.AeroCornerSmall
import com.mcs1000h.remote.ui.theme.AeroPalette
import com.mcs1000h.remote.ui.theme.AeroRed
import com.mcs1000h.remote.ui.theme.LocalAeroPalette
import com.mcs1000h.remote.ui.theme.LocalHazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// -------------------------------------------------------------------------------------------
// Aero surfaces - the frosted-glass "panel" look shared by cards, bars and dialogs.
// -------------------------------------------------------------------------------------------

private fun Color.lighten(amount: Float) = lerp(this, Color.White, amount)
private fun Color.darken(amount: Float) = lerp(this, Color.Black, amount)

/**
 * Mostly-opaque content panel: readable, with just a hint of glass sheen and a lit top edge.
 * This is the workhorse surface for cards and control sections - per the brief, main content
 * stays legible rather than fully see-through.
 */
fun Modifier.aeroPanel(
    palette: AeroPalette,
    shape: Shape = AeroCornerMedium,
    baseAlpha: Float = if (palette.isDark) 0.86f else 0.92f,
    elevation: Dp = 4.dp,
    accentWash: Color? = null,
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = palette.shadowColor.copy(alpha = 0.28f),
        spotColor = palette.shadowColor.copy(alpha = 0.35f),
    )
    .clip(shape)
    .background(palette.cardSurface.copy(alpha = baseAlpha))
    .then(
        if (accentWash != null) {
            Modifier.background(accentWash.copy(alpha = if (palette.isDark) 0.22f else 0.14f))
        } else {
            Modifier
        },
    )
    .background(
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = if (palette.isDark) 0.05f else 0.45f),
            0.5f to Color.Transparent,
        ),
    )
    .border(1.dp, Brush.verticalGradient(listOf(palette.borderHighlight, palette.borderShade)), shape)

/**
 * True frosted glass: blurs whatever is behind it via Haze. Falls back to a flat translucent
 * scrim when no [LocalHazeState] is available - matches Haze's own fallback behavior on
 * devices/API levels it can't blur on, so the degraded look is still intentional and glassy.
 * Reserved for navigation bars, dialogs and overlays per the design brief.
 */
@Composable
fun Modifier.aeroGlassChrome(
    shape: Shape = AeroCornerMedium,
    elevation: Dp = 10.dp,
): Modifier {
    val palette = LocalAeroPalette.current
    val hazeState = LocalHazeState.current
    val tintAlpha = if (palette.isDark) 0.55f else 0.5f

    val glassLayer = if (hazeState != null) {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = palette.cardSurface,
                tint = HazeTint(palette.glassTint.copy(alpha = tintAlpha)),
                blurRadius = 26.dp,
                noiseFactor = if (palette.isDark) 0.08f else 0.05f,
            ),
        )
    } else {
        Modifier.background(palette.glassTint.copy(alpha = tintAlpha + 0.2f))
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = palette.shadowColor.copy(alpha = 0.3f),
            spotColor = palette.shadowColor.copy(alpha = 0.4f),
        )
        .clip(shape)
        .then(glassLayer)
        .background(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = if (palette.isDark) 0.10f else 0.55f),
                0.3f to Color.Transparent,
            ),
        )
        .border(1.dp, Brush.verticalGradient(listOf(palette.borderHighlight, palette.borderShade)), shape)
}

// -------------------------------------------------------------------------------------------
// AeroCard - the standard "mostly opaque" content section container.
// -------------------------------------------------------------------------------------------

@Composable
fun AeroCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aeroPanel(LocalAeroPalette.current)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                GlossyIconBadge(icon = icon, active = false, size = 28.dp, iconPadding = 6.dp)
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

// -------------------------------------------------------------------------------------------
// GlossyIconBadge - a small dimensional glass "orb" behind an icon.
// -------------------------------------------------------------------------------------------

@Composable
fun GlossyIconBadge(
    icon: ImageVector,
    active: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconPadding: Dp = 8.dp,
    activeColor: Color? = null,
) {
    val palette = LocalAeroPalette.current
    val accent = activeColor ?: palette.accent
    val base = if (active) accent else palette.cardSurfaceVariant
    val top = if (active) accent.lighten(0.35f) else base.lighten(if (palette.isDark) 0.18f else 0.5f)
    val bottom = if (active) accent.darken(0.15f) else base.darken(if (palette.isDark) 0.05f else 0.06f)

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (active) 8.dp else 2.dp,
                shape = CircleShape,
                ambientColor = if (active) accent.copy(alpha = 0.55f) else palette.shadowColor.copy(alpha = 0.2f),
                spotColor = if (active) accent.copy(alpha = 0.65f) else palette.shadowColor.copy(alpha = 0.25f),
            )
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Restrained specular highlight - a soft blurred glint near the top edge.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = size * 0.18f, y = size * 0.1f)
                .size(size * 0.42f)
                .blur(size * 0.22f)
                .background(Color.White.copy(alpha = if (active) 0.35f else 0.4f), CircleShape),
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(iconPadding).size(size - iconPadding * 2),
        )
    }
}

// -------------------------------------------------------------------------------------------
// AeroIconToggle - icon + label toggle tile for boolean chair features (heat, spot, vibration).
// Replaces bare text CommandButtons so on/off features read as dimensional controls rather
// than a flat row of identically-styled buttons.
// -------------------------------------------------------------------------------------------

@Composable
fun AeroIconToggle(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAeroPalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .aeroPanel(
                palette = palette,
                shape = AeroCornerSmall,
                elevation = if (isActive) 6.dp else 2.dp,
                accentWash = if (isActive) palette.accent else null,
            )
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
    ) {
        GlossyIconBadge(icon = icon, active = isActive, size = 34.dp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// -------------------------------------------------------------------------------------------
// AeroSegmentedControl - single-select pill group for mutually-exclusive modes (zone, massage
// type, direction) so they read as one grouped choice instead of a row of loose buttons.
// -------------------------------------------------------------------------------------------

@Composable
fun AeroSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAeroPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .shadow(1.dp, AeroCornerPill, ambientColor = palette.shadowColor.copy(alpha = 0.15f))
            .clip(AeroCornerPill)
            .background(palette.cardSurfaceVariant.copy(alpha = if (palette.isDark) 0.7f else 0.8f))
            .border(1.dp, palette.borderShade, AeroCornerPill)
            .padding(3.dp),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (selected) {
                            Modifier
                                .shadow(
                                    4.dp,
                                    AeroCornerPill,
                                    ambientColor = palette.accent.copy(alpha = 0.5f),
                                    spotColor = palette.accent.copy(alpha = 0.6f),
                                )
                                .clip(AeroCornerPill)
                                .background(Brush.verticalGradient(listOf(palette.accent.lighten(0.25f), palette.accent.darken(0.1f))))
                                .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)), AeroCornerPill)
                        } else {
                            Modifier.clip(AeroCornerPill)
                        },
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------
// AeroButton - raised glossy buttons for primary / secondary / destructive actions.
// -------------------------------------------------------------------------------------------

enum class AeroButtonStyle { Primary, Secondary, Destructive }

@Composable
fun AeroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AeroButtonStyle = AeroButtonStyle.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val palette = LocalAeroPalette.current
    val baseColor = when (style) {
        AeroButtonStyle.Primary -> palette.accent
        AeroButtonStyle.Destructive -> AeroRed
        AeroButtonStyle.Secondary -> palette.cardSurfaceVariant
    }
    val contentColor = if (style == AeroButtonStyle.Secondary) MaterialTheme.colorScheme.onSurface else Color.White
    val alpha = if (enabled) 1f else 0.45f

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = if (style == AeroButtonStyle.Secondary) 2.dp else 8.dp,
                shape = AeroCornerPill,
                ambientColor = baseColor.copy(alpha = 0.45f * alpha),
                spotColor = baseColor.copy(alpha = 0.55f * alpha),
            )
            .clip(AeroCornerPill)
            .background(
                if (style == AeroButtonStyle.Secondary) {
                    Brush.verticalGradient(listOf(baseColor.lighten(0.1f).copy(alpha = alpha), baseColor.copy(alpha = alpha)))
                } else {
                    Brush.verticalGradient(listOf(baseColor.lighten(0.3f).copy(alpha = alpha), baseColor.darken(0.12f).copy(alpha = alpha)))
                },
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.65f * alpha), palette.borderShade)),
                shape = AeroCornerPill,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = alpha), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor.copy(alpha = alpha),
        )
    }
}

// -------------------------------------------------------------------------------------------
// AeroSwitch - Material3 Switch retinted to the Aero accent, glass track.
// -------------------------------------------------------------------------------------------

@Composable
fun AeroSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAeroPalette.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = palette.accent,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = if (palette.isDark) palette.cardSurfaceVariant.lighten(0.3f) else Color.White,
            uncheckedTrackColor = palette.cardSurfaceVariant,
            uncheckedBorderColor = palette.borderShade,
        ),
    )
}

// -------------------------------------------------------------------------------------------
// StatusDot - small glowing indicator dot used in status bars / headers.
// -------------------------------------------------------------------------------------------

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier, size: Dp = 9.dp) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = color.copy(alpha = 0.8f), spotColor = color.copy(alpha = 0.9f))
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(color.lighten(0.4f), color))),
    )
}
