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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ui.theme.AppPalette
import com.mcs1000h.remote.ui.theme.CornerControl
import com.mcs1000h.remote.ui.theme.CornerPanel
import com.mcs1000h.remote.ui.theme.CornerTight
import com.mcs1000h.remote.ui.theme.LocalAppPalette

// -------------------------------------------------------------------------------------------
// Flat control-panel primitives. Deliberately plain: solid fills, thin single-tone borders,
// shadows are neutral (never tinted to a status color) and used sparingly for real separation
// (top bar over scrolling content, a pressed button) rather than decoration. No gradients, no
// blur, no glow.
// -------------------------------------------------------------------------------------------

fun Modifier.panelSurface(
    palette: AppPalette,
    shape: Shape = CornerPanel,
): Modifier = this
    .clip(shape)
    .background(palette.panelSurface)
    .border(1.dp, palette.border, shape)

/** A titled section within the continuous control panel - not a separate floating card. */
@Composable
fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SectionDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalAppPalette.current.border),
    )
}

// -------------------------------------------------------------------------------------------
// IconToggle - compact icon + label tile for boolean chair features. Active state is an
// outline + recolored icon/text rather than a solid fill: several of these are commonly on at
// once (heat, spot, vibration...), and a full-color block on every active tile at once reads as
// noise rather than signal. An outline stays legible without turning the panel into a mosaic.
// -------------------------------------------------------------------------------------------

@Composable
fun IconToggle(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    val content = if (isActive) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isActive) palette.accent else palette.border
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CornerControl)
            .background(palette.surfaceVariant)
            .border(if (isActive) 1.5.dp else 1.dp, borderColor, CornerControl)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// -------------------------------------------------------------------------------------------
// SegmentedControl - rocker-style single-select group for mutually-exclusive modes.
// -------------------------------------------------------------------------------------------

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(CornerControl)
            .background(palette.surfaceVariant),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            if (index != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(if (selected || index - 1 == selectedIndex) Color.Transparent else palette.border),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (selected) palette.accent else Color.Transparent)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
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
// SolidButton - flat-fill button for primary / secondary / destructive actions.
// -------------------------------------------------------------------------------------------

enum class ButtonStyle { Primary, Secondary, Destructive }

@Composable
fun SolidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val palette = LocalAppPalette.current
    val baseColor = when (style) {
        ButtonStyle.Primary -> palette.accent
        ButtonStyle.Destructive -> palette.danger
        ButtonStyle.Secondary -> palette.surfaceVariant
    }
    val contentColor = if (style == ButtonStyle.Secondary) MaterialTheme.colorScheme.onSurface else palette.onFill
    val alpha = if (enabled) 1f else 0.4f

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(40.dp)
            .shadow(if (style == ButtonStyle.Secondary) 0.dp else 1.dp, CornerControl, ambientColor = palette.shadow, spotColor = palette.shadow)
            .clip(CornerControl)
            .background(baseColor.copy(alpha = alpha))
            .then(
                if (style == ButtonStyle.Secondary) {
                    Modifier.border(1.dp, palette.border.copy(alpha = alpha), CornerControl)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = contentColor.copy(alpha = alpha))
    }
}

// -------------------------------------------------------------------------------------------
// AppSwitch - Material3 Switch retinted to the accent.
// -------------------------------------------------------------------------------------------

@Composable
fun AppSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = palette.onFill,
            checkedTrackColor = palette.accent,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = palette.panelSurface,
            uncheckedTrackColor = palette.surfaceVariant,
            uncheckedBorderColor = palette.border,
        ),
    )
}

// -------------------------------------------------------------------------------------------
// StatusDot - small solid indicator dot, no glow.
// -------------------------------------------------------------------------------------------

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier, size: Dp = 8.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}
