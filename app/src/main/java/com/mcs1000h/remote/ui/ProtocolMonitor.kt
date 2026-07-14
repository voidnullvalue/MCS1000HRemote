package com.mcs1000h.remote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ChairBleManager
import com.mcs1000h.remote.ble.FrameDirection
import com.mcs1000h.remote.ble.ProtocolFrame
import com.mcs1000h.remote.ui.theme.CornerControl
import com.mcs1000h.remote.ui.theme.LocalAppPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_FRAMES = 300

/**
 * Live BLE frame log - not linked from anywhere in the UI. Reachable by long-pressing the
 * connection status dot. Reuses [ChairBleManager.protocolFrames], which every send/receive
 * already fed into but nothing ever consumed.
 */
@Composable
fun ProtocolMonitor(chairManager: ChairBleManager, onExit: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    val frames = remember { mutableStateListOf<ProtocolFrame>() }

    LaunchedEffect(Unit) {
        chairManager.protocolFrames.collect { frame ->
            frames.add(0, frame)
            while (frames.size > MAX_FRAMES) frames.removeAt(frames.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Protocol Monitor", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "${frames.size} frame${if (frames.size == 1) "" else "s"} captured this session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .panelSurface(palette, CornerControl)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onExit)
                    .padding(10.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close monitor", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxSize().panelSurface(palette)) {
            if (frames.isEmpty()) {
                Text(
                    "Nothing sent or received yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                    items(frames, key = { it.atMillis to it.direction }) { frame ->
                        FrameRow(frame)
                    }
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

@Composable
private fun FrameRow(frame: ProtocolFrame) {
    val palette = LocalAppPalette.current
    val hex = frame.bytes.joinToString(" ") { "%02X".format(it) }
    val isSent = frame.direction == FrameDirection.SENT
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isSent) "TX" else "RX",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSent) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp),
        )
        Text(
            text = timeFormat.format(Date(frame.atMillis)),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(78.dp),
        )
        Text(
            text = hex,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
