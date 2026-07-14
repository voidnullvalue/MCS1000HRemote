package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ConnectionState
import com.mcs1000h.remote.ui.theme.AeroAmber
import com.mcs1000h.remote.ui.theme.AeroGreen
import com.mcs1000h.remote.ui.theme.AeroRed
import com.mcs1000h.remote.ui.theme.LocalAeroPalette
import kotlin.math.roundToInt

@Composable
fun ConnectionStatusBar(
    state: ConnectionState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    val palette = LocalAeroPalette.current
    val statusColor = when (state) {
        ConnectionState.Connected -> AeroGreen
        ConnectionState.Disconnected -> AeroRed
        ConnectionState.Idle -> palette.accent
        else -> AeroAmber
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .aeroPanel(palette, elevation = 3.dp, accentWash = statusColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            StatusDot(color = statusColor)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Connection",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = when (state) {
                        ConnectionState.Idle -> "Idle"
                        ConnectionState.Scanning -> "Scanning"
                        ConnectionState.Connecting -> "Connecting"
                        ConnectionState.DiscoveringServices -> "Discovering services"
                        ConnectionState.Connected -> "Connected"
                        ConnectionState.Disconnected -> "Disconnected"
                        is ConnectionState.Unsupported -> state.reason
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
            }
        }

        if (state != ConnectionState.Connected) {
            AeroButton(
                text = "Connect",
                onClick = onConnectClick,
                enabled = state !in listOf(
                    ConnectionState.Scanning,
                    ConnectionState.Connecting,
                    ConnectionState.DiscoveringServices,
                ),
            )
        } else {
            AeroButton(text = "Disconnect", onClick = onDisconnectClick, style = AeroButtonStyle.Destructive)
        }
    }
}

/** Standard "mostly opaque" content section - see [AeroCard]. */
@Composable
fun ControlSection(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AeroCard(title = title, modifier = modifier, icon = icon, content = content)
}

/** Grouped single-select control for mutually exclusive modes (zone, massage type, direction). */
@Composable
fun ChoiceRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        AeroSegmentedControl(options = options, selectedIndex = selectedIndex, onSelect = onSelect)
    }
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        AeroSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Slider that only commits a target once the user lifts their finger, then hands off to a
 * closed-loop seek (nudge-and-poll) rather than the OEM app's raw hold-to-nudge dragging.
 */
@Composable
fun PositionSeekSlider(
    label: String,
    currentPosition: Int?,
    enabled: Boolean,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAeroPalette.current
    var dragValue by remember(currentPosition) {
        mutableFloatStateOf((currentPosition ?: 0).coerceIn(0, 100).toFloat())
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = currentPosition?.let { "$it%" } ?: "—",
                style = MaterialTheme.typography.labelMedium,
                color = palette.accent,
                fontWeight = FontWeight.Bold,
            )
        }
        Slider(
            value = dragValue,
            onValueChange = { dragValue = it },
            onValueChangeFinished = { onSeek(dragValue.roundToInt()) },
            valueRange = 0f..100f,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = palette.accent,
                activeTrackColor = palette.accent,
                inactiveTrackColor = palette.cardSurfaceVariant,
            ),
        )
    }
}
