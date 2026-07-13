package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ChairBleManager
import com.mcs1000h.remote.ble.ChairCommand
import com.mcs1000h.remote.ble.ChairStatus
import kotlinx.coroutines.launch

@Composable
fun ManualScreen(
    status: ChairStatus,
    chairManager: ChairBleManager,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val onCommand: (ChairCommand) -> Unit = { chairManager.send(it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ControlSection("Power") {
            CommandButton(
                label = "Power",
                isActive = status.powerOn,
                onClick = { onCommand(ChairCommand.POWER_TOGGLE) },
            )
        }

        ControlSection("Back") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CommandButton("Heat", status.backHeat, { onCommand(ChairCommand.BACK_HEAT_TOGGLE) }, Modifier.weight(1f))
                    CommandButton("Spot", status.backSpot, { onCommand(ChairCommand.BACK_SPOT) }, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CommandButton("Full", status.backZoneFull, { onCommand(ChairCommand.BACK_ZONE_FULL) }, Modifier.weight(1f))
                    CommandButton("Upper", status.backZoneUpper, { onCommand(ChairCommand.BACK_ZONE_UPPER) }, Modifier.weight(1f))
                    CommandButton("Lower", status.backZoneLower, { onCommand(ChairCommand.BACK_ZONE_LOWER) }, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CommandButton("Shiatsu", status.backShiatsu, { onCommand(ChairCommand.BACK_MASSAGE_SHIATSU) }, Modifier.weight(1f))
                    CommandButton("Rolling", status.backRolling, { onCommand(ChairCommand.BACK_MASSAGE_ROLLING) }, Modifier.weight(1f))
                    CommandButton("Tapping", status.backTapping, { onCommand(ChairCommand.BACK_MASSAGE_TAPPING) }, Modifier.weight(1f))
                }
                PositionSeekSlider(
                    label = "Spot position (drag to seek)",
                    currentPosition = status.backPosition.takeIf { it in 0..100 },
                    enabled = status.backSpot,
                    onSeek = { target -> scope.launch { chairManager.seekBackPosition(target) } },
                )
            }
        }

        ControlSection("Neck") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CommandButton("Spot", status.neckSpot, { onCommand(ChairCommand.NECK_SPOT) }, Modifier.weight(1f))
                    CommandButton("Heat", status.neckHeat, { onCommand(ChairCommand.NECK_HEAT_TOGGLE) }, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CommandButton("Forward", status.neckForward, { onCommand(ChairCommand.NECK_MASSAGE_FORWARD) }, Modifier.weight(1f))
                    CommandButton("Reverse", status.neckReverse, { onCommand(ChairCommand.NECK_MASSAGE_REVERSE) }, Modifier.weight(1f))
                }
                PositionSeekSlider(
                    label = "Spot position (drag to seek)",
                    currentPosition = status.neckPosition.takeIf { it in 0..100 },
                    enabled = status.neckSpot,
                    onSeek = { target -> scope.launch { chairManager.seekNeckPosition(target) } },
                )
            }
        }

        ControlSection("Seat") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CommandButton("Vibration", status.seatVibration, { onCommand(ChairCommand.SEAT_VIBRATION_TOGGLE) }, Modifier.weight(1f))
                CommandButton("Heat", status.seatHeat, { onCommand(ChairCommand.SEAT_HEAT_TOGGLE) }, Modifier.weight(1f))
            }
        }

        if (status.programRunning) {
            Text(
                text = "Program running" + (status.programMinutesRemaining?.let { " – $it min left" } ?: ""),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
