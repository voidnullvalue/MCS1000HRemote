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
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ChairPresetPrograms
import com.mcs1000h.remote.ble.ChairProgramDef
import com.mcs1000h.remote.ble.ChairProgramRunner
import com.mcs1000h.remote.ble.ChairScene
import androidx.compose.ui.graphics.Color
import com.mcs1000h.remote.ui.theme.AeroCornerSmall
import com.mcs1000h.remote.ui.theme.AeroGreen
import com.mcs1000h.remote.ui.theme.LocalAeroPalette

@Composable
fun ProgramScreen(
    runner: ChairProgramRunner,
    modifier: Modifier = Modifier,
) {
    val runState by runner.state.collectAsState()
    val scope = rememberCoroutineScope()
    val palette = LocalAeroPalette.current

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChairPresetPrograms.ALL.forEach { preset ->
                    val isThisRunning = runState?.isRunning == true && runState?.program?.name == preset.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aeroPanel(
                                palette = palette,
                                shape = AeroCornerSmall,
                                elevation = if (isThisRunning) 4.dp else 1.dp,
                                accentWash = if (isThisRunning) palette.accent else null,
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(preset.name, style = MaterialTheme.typography.bodyLarge)
                            if (isThisRunning) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusDot(color = AeroGreen, size = 6.dp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        "Minute ${(runState?.currentMinute ?: 0) + 1} / ${ChairProgramDef.MINUTES}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AeroGreen,
                                    )
                                }
                            }
                        }
                        AeroButton(
                            text = if (isThisRunning) "Restart" else "Run",
                            onClick = { runner.start(scope, preset) },
                            style = if (isThisRunning) AeroButtonStyle.Secondary else AeroButtonStyle.Primary,
                        )
                    }
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
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.accent,
                            selectedLabelColor = Color.White,
                            containerColor = if (hasContent) AeroGreen.copy(alpha = 0.18f) else palette.cardSurfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = palette.borderShade,
                            selectedBorderColor = palette.accent,
                        ),
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
                AeroButton(
                    text = if (customRunning) "Restart" else "Run Custom",
                    onClick = { runner.start(scope, ChairProgramDef("Custom", customSteps)) },
                    modifier = Modifier.weight(1f),
                )
                AeroButton(
                    text = "Stop",
                    onClick = { runner.stop() },
                    modifier = Modifier.weight(1f),
                    style = AeroButtonStyle.Secondary,
                    enabled = runState?.isRunning == true,
                )
            }

            if (customRunning) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(color = AeroGreen, size = 7.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Running – minute ${(runState?.currentMinute ?: 0) + 1} / ${ChairProgramDef.MINUTES}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroGreen,
                    )
                }
            }
        }
    }
}
