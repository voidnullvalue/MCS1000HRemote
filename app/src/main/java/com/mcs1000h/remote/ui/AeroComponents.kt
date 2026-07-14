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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ui.theme.AeroCornerMedium
import com.mcs1000h.remote.ui.theme.AeroCornerPill
import com.mcs1000h.remote.ui.theme.AeroCornerSmall
import com.mcs1000h.remote.ui.theme.AeroPalette
import com.mcs1000h.remote.ui.theme.LocalAeroPalette
import com.mcs1000h.remote.ui.theme.LocalHazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// -------------------------------------------------------------------------------------------
// Aero surfaces - flat, near-opaque fills with a single-tone border and a plain drop shadow.
// Earlier drafts layered a gradient sheen + gradient border on every surface; that's gone -
// it made background lightness vary behind text (inconsistent contrast) for no functional
// gain. Glass/blur is reserved for the top bar only, per the brief.
// -------------------------------------------------------------------------------------------

/** Mostly-opaque content panel: readable first, "glass" second. */
fun Modifier.aeroPanel(
    palette: AeroPalette,
    shape: Shape = AeroCornerMedium,
    baseAlpha: Float = if (palette.isDark) 0.94f else 0.97f,
    elevation: Dp = 2.dp,
    accentWash: Color? = null,
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = palette.shadowColor.copy(alpha = 0.18f),
        spotColor = palette.shadowColor.copy(alpha = 0.22f),
    )
    .clip(shape)
    .background(palette.cardSurface.copy(alpha = baseAlpha))
    .then(
        // Kept faint enough that accent-colored text/icons drawn on top (e.g. an active
        // toggle's own label) still clear WCAG AA against the tinted background.
        if (accentWash != null) {
            Modifier.background(accentWash.copy(alpha = if (palette.isDark) 0.08f else 0.04f))
        } else {
            Modifier
        },
    )
    .border(1.dp, palette.borderShade, shape)

/**
 * True frosted glass: blurs whatever is behind it via Haze. Falls back to a flat translucent
 * scrim when no [LocalHazeState] is available - matches Haze's own fallback behavior on
 * devices/API levels it can't blur on. Reserved for the top bar per the design brief; everything
 * else uses [aeroPanel] so on-screen text sits on a predictable, readable background.
 */
@Composable
fun Modifier.aeroGlassChrome(
    shape: Shape = AeroCornerMedium,
    elevation: Dp = 6.dp,
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
                noiseFactor = if (palette.isDark) 0.05f else 0.03f,
            ),
        )
    } else {
        // No blur available on this API level - still lay down an opaque base first so text
        // contrast doesn't depend on whatever wallpaper color happens to sit behind it.
        Modifier
            .background(palette.cardSurface)
            .background(palette.glassTint.copy(alpha = tintAlpha))
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = palette.shadowColor.copy(alpha = 0.22f),
            spotColor = palette.shadowColor.copy(alpha = 0.26f),
        )
        .clip(shape)
        .then(glassLayer)
        .border(1.dp, palette.borderHighlight, shape)
}

// -------------------------------------------------------------------------------------------
// AeroCard - the standard content section container.
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
// GlossyIconBadge - a small dimensional icon badge: flat fill, thin border, one soft glint.
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
    val contentColor = if (active) palette.onFill else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (active) 4.dp else 1.dp,
                shape = CircleShape,
                ambientColor = if (active) accent.copy(alpha = 0.35f) else palette.shadowColor.copy(alpha = 0.15f),
                spotColor = if (active) accent.copy(alpha = 0.4f) else palette.shadowColor.copy(alpha = 0.18f),
            )
            .clip(CircleShape)
            .background(base)
            .border(1.dp, palette.borderShade, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // One restrained specular glint - enough to read as dimensional without a full gradient.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = size * 0.2f, y = size * 0.12f)
                .size(size * 0.34f)
                .blur(size * 0.2f)
                .background(Color.White.copy(alpha = if (active) 0.2f else 0.25f), CircleShape),
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.padding(iconPadding).size(size - iconPadding * 2),
        )
    }
}

// -------------------------------------------------------------------------------------------
// AeroIconToggle - icon + label toggle tile for boolean chair features (heat, spot, vibration).
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
                elevation = if (isActive) 3.dp else 1.dp,
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
            .clip(AeroCornerPill)
            .background(palette.cardSurfaceVariant)
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
                            Modifier.clip(AeroCornerPill).background(palette.accent)
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
                    color = if (selected) palette.onFill else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------
// AeroButton - flat-fill buttons for primary / secondary / destructive actions.
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
        AeroButtonStyle.Destructive -> palette.danger
        AeroButtonStyle.Secondary -> palette.cardSurfaceVariant
    }
    val contentColor = if (style == AeroButtonStyle.Secondary) MaterialTheme.colorScheme.onSurface else palette.onFill
    val alpha = if (enabled) 1f else 0.45f

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = if (style == AeroButtonStyle.Secondary) 1.dp else 3.dp,
                shape = AeroCornerPill,
                ambientColor = baseColor.copy(alpha = 0.22f * alpha),
                spotColor = baseColor.copy(alpha = 0.26f * alpha),
            )
            .clip(AeroCornerPill)
            .background(baseColor.copy(alpha = alpha))
            .border(1.dp, palette.borderShade.copy(alpha = alpha), AeroCornerPill)
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
// AeroSwitch - Material3 Switch retinted to the Aero accent.
// -------------------------------------------------------------------------------------------

@Composable
fun AeroSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAeroPalette.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = palette.onFill,
            checkedTrackColor = palette.accent,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = palette.cardSurface,
            uncheckedTrackColor = palette.cardSurfaceVariant,
            uncheckedBorderColor = palette.borderShade,
        ),
    )
}

// -------------------------------------------------------------------------------------------
// StatusDot - small solid indicator dot used in status bars / headers.
// -------------------------------------------------------------------------------------------

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier, size: Dp = 9.dp) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(2.dp, CircleShape, ambientColor = color.copy(alpha = 0.4f), spotColor = color.copy(alpha = 0.45f))
            .clip(CircleShape)
            .background(color),
    )
}
