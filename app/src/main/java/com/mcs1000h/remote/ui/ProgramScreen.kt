package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ChairPresetPrograms
import com.mcs1000h.remote.ble.ChairProgramDef
import com.mcs1000h.remote.ble.ChairProgramRunner
import com.mcs1000h.remote.ble.ChairScene

@Composable
fun ProgramScreen(
    runner: ChairProgramRunner,
    modifier: Modifier = Modifier,
) {
    val runState by runner.state.collectAsState()
    val scope = rememberCoroutineScope()

    var customSteps by remember { mutableStateOf(List(ChairProgramDef.MINUTES) { ChairScene() }) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ControlSection("Preset Programs") {
            Text(
                "Recovered from the OEM app's built-in database.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            ChairPresetPrograms.ALL.forEach { preset ->
                val isThisRunning = runState?.isRunning == true && runState?.program?.name == preset.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(preset.name, style = MaterialTheme.typography.bodyLarge)
                        if (isThisRunning) {
                            Text(
                                "Minute ${(runState?.currentMinute ?: 0) + 1} / ${ChairProgramDef.MINUTES}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                            )
                        }
                    }
                    Button(onClick = { runner.start(scope, preset) }) { Text(if (isThisRunning) "Restart" else "Run") }
                }
            }
        }

        ControlSection("Custom Program") {
            Text(
                "Build your own 15-minute sequence - pick a minute below, set what it should do.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ChairProgramDef.MINUTES) { minute ->
                    val isSelected = minute == selectedMinute
                    val hasContent = customSteps[minute] != ChairScene()
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMinute = minute },
                        label = { Text("${minute + 1}") },
                        colors = if (hasContent && !isSelected) {
                            FilterChipDefaults.filterChipColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f))
                        } else {
                            FilterChipDefaults.filterChipColors()
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SceneEditor(
                scene = customSteps[selectedMinute],
                onSceneChange = { updated ->
                    customSteps = customSteps.toMutableList().also { it[selectedMinute] = updated }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            val customRunning = runState?.isRunning == true && runState?.program?.name == "Custom"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { runner.start(scope, ChairProgramDef("Custom", customSteps)) },
                    modifier = Modifier.weight(1f),
                ) { Text(if (customRunning) "Restart" else "Run Custom") }
                OutlinedButton(
                    onClick = { runner.stop() },
                    modifier = Modifier.weight(1f),
                    enabled = runState?.isRunning == true,
                ) { Text("Stop") }
            }

            if (customRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Running – minute ${(runState?.currentMinute ?: 0) + 1} / ${ChairProgramDef.MINUTES}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                )
            }
        }
    }
}
