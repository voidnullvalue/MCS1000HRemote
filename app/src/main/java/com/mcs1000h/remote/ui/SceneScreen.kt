package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ChairProgramDef
import com.mcs1000h.remote.ble.ChairProgramRunner
import com.mcs1000h.remote.ble.ChairScene
import com.mcs1000h.remote.ui.theme.LocalAppPalette

/**
 * Builds one [ChairScene] and applies it as a 15-minute program where every
 * minute is that same scene - a single BLE write's worth of "set exactly this
 * combination now", which the OEM app has no equivalent for (its manual mode
 * only toggles one feature at a time).
 */
@Composable
fun SceneScreen(
    runner: ChairProgramRunner,
    modifier: Modifier = Modifier,
) {
    var scene by remember { mutableStateOf(ChairScene()) }
    val runState by runner.state.collectAsState()
    val isApplied = runState?.isRunning == true
    val scope = rememberCoroutineScope()
    val palette = LocalAppPalette.current

    Section("Quick Scene", modifier = modifier.fillMaxWidth()) {
        Text(
            "Set every feature at once in a single write, instead of toggling them one by one.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        SceneEditor(scene = scene, onSceneChange = { scene = it })

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SolidButton(
                text = "Apply",
                onClick = {
                    runner.start(scope, ChairProgramDef("Quick Scene", List(ChairProgramDef.MINUTES) { scene }))
                },
                modifier = Modifier.weight(1f),
            )
            SolidButton(
                text = "Stop",
                onClick = { runner.stop() },
                modifier = Modifier.weight(1f),
                style = ButtonStyle.Secondary,
                enabled = isApplied,
            )
        }

        if (isApplied) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(color = palette.success, size = 7.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Applied – holds for up to 15 minutes unless stopped.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.success,
                )
            }
        }
    }
}
