package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ConnectionState
import kotlin.math.roundToInt

@Composable
fun ConnectionStatusBar(
    state: ConnectionState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    val statusColor = when (state) {
        ConnectionState.Connected -> Color(0xFF4CAF50)
        ConnectionState.Disconnected -> Color(0xFFf44336)
        ConnectionState.Idle -> Color(0xFF2196F3)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            if (state != ConnectionState.Connected) {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.height(40.dp),
                    enabled = state !in listOf(
                        ConnectionState.Scanning,
                        ConnectionState.Connecting,
                        ConnectionState.DiscoveringServices,
                    ),
                ) { Text("Connect") }
            } else {
                Button(
                    onClick = onDisconnectClick,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf44336)),
                ) { Text("Disconnect") }
            }
        }
    }
}

@Composable
fun ControlSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            content()
        }
    }
}

@Composable
fun CommandButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) { Text(label, style = MaterialTheme.typography.labelSmall) }
}

@Composable
fun ChoiceRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEachIndexed { index, option ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    label = { Text(option, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
    var dragValue by remember(currentPosition) {
        mutableFloatStateOf((currentPosition ?: 0).coerceIn(0, 100).toFloat())
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = currentPosition?.let { "$it%" } ?: "—",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = dragValue,
            onValueChange = { dragValue = it },
            onValueChangeFinished = { onSeek(dragValue.roundToInt()) },
            valueRange = 0f..100f,
            enabled = enabled,
        )
    }
}
