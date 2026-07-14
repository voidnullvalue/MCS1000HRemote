package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ConnectionState
import com.mcs1000h.remote.ui.theme.LocalAppPalette
import kotlin.math.roundToInt

/** Compact inline connection status row - lives directly in the header, not its own card. */
@Composable
fun ConnectionStatusRow(
    state: ConnectionState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    val statusColor = when (state) {
        ConnectionState.Connected -> palette.success
        ConnectionState.Disconnected -> palette.danger
        ConnectionState.Idle -> palette.accent
        else -> palette.warning
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = statusColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (state) {
                    ConnectionState.Idle -> "Idle"
                    ConnectionState.Scanning -> "Scanning…"
                    ConnectionState.Connecting -> "Connecting…"
                    ConnectionState.DiscoveringServices -> "Discovering services…"
                    ConnectionState.Connected -> "Connected"
                    ConnectionState.Disconnected -> "Disconnected"
                    is ConnectionState.Unsupported -> state.reason
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor,
            )
        }

        if (state != ConnectionState.Connected) {
            SolidButton(
                text = "Connect",
                onClick = onConnectClick,
                enabled = state !in listOf(
                    ConnectionState.Scanning,
                    ConnectionState.Connecting,
                    ConnectionState.DiscoveringServices,
                ),
            )
        } else {
            SolidButton(text = "Disconnect", onClick = onDisconnectClick, style = ButtonStyle.Destructive)
        }
    }
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
        SegmentedControl(options = options, selectedIndex = selectedIndex, onSelect = onSelect)
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
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
    val palette = LocalAppPalette.current
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
                inactiveTrackColor = palette.surfaceVariant,
            ),
        )
    }
}
