package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.BackZone
import com.mcs1000h.remote.ble.ChairScene
import com.mcs1000h.remote.ble.MassageType
import com.mcs1000h.remote.ble.NeckDirection

private val ZONE_OPTIONS = listOf(BackZone.OFF, BackZone.UPPER, BackZone.LOWER, BackZone.FULL)
private val ZONE_LABELS = listOf("Off", "Upper", "Lower", "Full")
private val TYPE_OPTIONS = listOf(
    MassageType.OFF, MassageType.SHIATSU, MassageType.ROLLING, MassageType.TAPPING, MassageType.SHIATSU_TAPPING,
)
private val TYPE_LABELS = listOf("Off", "Shiatsu", "Rolling", "Tapping", "Shiatsu+Tap")
private val NECK_OPTIONS = listOf(NeckDirection.OFF, NeckDirection.FORWARD, NeckDirection.REVERSE)
private val NECK_LABELS = listOf("Off", "Forward", "Reverse")

/** Full editor for a single [ChairScene] - every field the direct-state frame can carry. */
@Composable
fun SceneEditor(
    scene: ChairScene,
    onSceneChange: (ChairScene) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChoiceRow(
            label = "Back zone",
            options = ZONE_LABELS,
            selectedIndex = ZONE_OPTIONS.indexOf(scene.zone),
            onSelect = { onSceneChange(scene.copy(zone = ZONE_OPTIONS[it])) },
        )
        ChoiceRow(
            label = "Massage type",
            options = TYPE_LABELS,
            selectedIndex = TYPE_OPTIONS.indexOf(scene.massageType),
            onSelect = { onSceneChange(scene.copy(massageType = TYPE_OPTIONS[it])) },
        )
        ToggleRow(
            label = "Back heat",
            checked = scene.backHeat,
            onCheckedChange = { onSceneChange(scene.copy(backHeat = it)) },
        )
        ChoiceRow(
            label = "Neck",
            options = NECK_LABELS,
            selectedIndex = NECK_OPTIONS.indexOf(scene.neckDirection),
            onSelect = { onSceneChange(scene.copy(neckDirection = NECK_OPTIONS[it])) },
        )
        ToggleRow(
            label = "Neck heat",
            checked = scene.neckHeat,
            onCheckedChange = { onSceneChange(scene.copy(neckHeat = it)) },
        )
        ToggleRow(
            label = "Seat vibration",
            checked = scene.seatVibration,
            onCheckedChange = { onSceneChange(scene.copy(seatVibration = it)) },
        )
        ToggleRow(
            label = "Seat heat",
            checked = scene.seatHeat,
            onCheckedChange = { onSceneChange(scene.copy(seatHeat = it)) },
        )
    }
}
