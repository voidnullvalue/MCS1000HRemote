package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ChairBleManager
import com.mcs1000h.remote.ble.ChairCommand
import com.mcs1000h.remote.ble.ChairStatus
import com.mcs1000h.remote.ui.theme.LocalAppPalette
import kotlinx.coroutines.launch

private val ZONE_COMMANDS = listOf(ChairCommand.BACK_ZONE_FULL, ChairCommand.BACK_ZONE_UPPER, ChairCommand.BACK_ZONE_LOWER)
private val ZONE_LABELS = listOf("Full", "Upper", "Lower")
private val MASSAGE_COMMANDS = listOf(ChairCommand.BACK_MASSAGE_SHIATSU, ChairCommand.BACK_MASSAGE_ROLLING, ChairCommand.BACK_MASSAGE_TAPPING)
private val MASSAGE_LABELS = listOf("Shiatsu", "Rolling", "Tapping")
private val NECK_COMMANDS = listOf(ChairCommand.NECK_MASSAGE_FORWARD, ChairCommand.NECK_MASSAGE_REVERSE)
private val NECK_LABELS = listOf("Forward", "Reverse")

@Composable
fun ManualScreen(
    status: ChairStatus,
    chairManager: ChairBleManager,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val palette = LocalAppPalette.current
    val onCommand: (ChairCommand) -> Unit = { chairManager.send(it) }

    Column(modifier = modifier.fillMaxWidth()) {
        Section("Power") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (status.powerOn) "On" else "Off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status.powerOn) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppSwitch(checked = status.powerOn, onCheckedChange = { onCommand(ChairCommand.POWER_TOGGLE) })
            }
        }
        SectionDivider()

        Section("Back") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconToggle("Heat", Icons.Filled.Whatshot, status.backHeat, { onCommand(ChairCommand.BACK_HEAT_TOGGLE) }, Modifier.weight(1f))
                    IconToggle("Spot", Icons.Filled.GpsFixed, status.backSpot, { onCommand(ChairCommand.BACK_SPOT) }, Modifier.weight(1f))
                }
                ChoiceRow(
                    label = "Zone",
                    options = ZONE_LABELS,
                    selectedIndex = when {
                        status.backZoneFull -> 0
                        status.backZoneUpper -> 1
                        status.backZoneLower -> 2
                        else -> -1
                    },
                    onSelect = { onCommand(ZONE_COMMANDS[it]) },
                )
                ChoiceRow(
                    label = "Massage type",
                    options = MASSAGE_LABELS,
                    selectedIndex = when {
                        status.backShiatsu -> 0
                        status.backRolling -> 1
                        status.backTapping -> 2
                        else -> -1
                    },
                    onSelect = { onCommand(MASSAGE_COMMANDS[it]) },
                )
                PositionSeekSlider(
                    label = "Spot position (drag to seek)",
                    currentPosition = status.backPosition.takeIf { it in 0..100 },
                    enabled = status.backSpot,
                    onSeek = { target -> scope.launch { chairManager.seekBackPosition(target) } },
                )
            }
        }
        SectionDivider()

        Section("Neck") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconToggle("Spot", Icons.Filled.GpsFixed, status.neckSpot, { onCommand(ChairCommand.NECK_SPOT) }, Modifier.weight(1f))
                    IconToggle("Heat", Icons.Filled.Whatshot, status.neckHeat, { onCommand(ChairCommand.NECK_HEAT_TOGGLE) }, Modifier.weight(1f))
                }
                ChoiceRow(
                    label = "Direction",
                    options = NECK_LABELS,
                    selectedIndex = when {
                        status.neckForward -> 0
                        status.neckReverse -> 1
                        else -> -1
                    },
                    onSelect = { onCommand(NECK_COMMANDS[it]) },
                )
                PositionSeekSlider(
                    label = "Spot position (drag to seek)",
                    currentPosition = status.neckPosition.takeIf { it in 0..100 },
                    enabled = status.neckSpot,
                    onSeek = { target -> scope.launch { chairManager.seekNeckPosition(target) } },
                )
            }
        }
        SectionDivider()

        Section("Seat") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconToggle("Vibration", Icons.Filled.Vibration, status.seatVibration, { onCommand(ChairCommand.SEAT_VIBRATION_TOGGLE) }, Modifier.weight(1f))
                IconToggle("Heat", Icons.Filled.Whatshot, status.seatHeat, { onCommand(ChairCommand.SEAT_HEAT_TOGGLE) }, Modifier.weight(1f))
            }
        }

        if (status.programRunning) {
            SectionDivider()
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 10.dp)) {
                StatusDot(color = palette.accent, size = 7.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Program running" + (status.programMinutesRemaining?.let { " – $it min left" } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
