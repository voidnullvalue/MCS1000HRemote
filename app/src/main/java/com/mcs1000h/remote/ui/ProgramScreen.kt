package com.mcs1000h.remote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ble.ChairPresetPrograms
import com.mcs1000h.remote.ble.ChairProgramDef
import com.mcs1000h.remote.ble.ChairProgramRunner
import com.mcs1000h.remote.ble.ChairScene
import com.mcs1000h.remote.ble.CustomProgramStore
import com.mcs1000h.remote.ui.theme.LocalAppPalette

@Composable
fun ProgramScreen(
    runner: ChairProgramRunner,
    modifier: Modifier = Modifier,
) {
    val runState by runner.state.collectAsState()
    val scope = rememberCoroutineScope()
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    val store = remember { CustomProgramStore(context) }

    var customSteps by remember { mutableStateOf(List(ChairProgramDef.MINUTES) { ChairScene() }) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var savedPrograms by remember { mutableStateOf(store.loadAll()) }
    var newProgramName by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Section("Preset Programs") {
            Text(
                "Recovered from the OEM app's built-in database.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ChairPresetPrograms.ALL.forEach { preset ->
                    val isThisRunning = runState?.isRunning == true && runState?.program?.name == preset.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(preset.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            if (isThisRunning) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusDot(color = palette.success, size = 6.dp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        "Minute ${(runState?.currentMinute ?: 0) + 1} / ${ChairProgramDef.MINUTES}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.success,
                                    )
                                }
                            }
                        }
                        SolidButton(
                            text = if (isThisRunning) "Restart" else "Run",
                            onClick = { runner.start(scope, preset) },
                            style = if (isThisRunning) ButtonStyle.Secondary else ButtonStyle.Primary,
                        )
                    }
                }
            }
        }
        SectionDivider()

        Section("Custom Program") {
            Text(
                "Build your own 15-minute sequence - pick a minute below, set what it should do.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp),
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ChairProgramDef.MINUTES) { minute ->
                    val isSelected = minute == selectedMinute
                    val hasContent = customSteps[minute] != ChairScene()
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMinute = minute },
                        label = { Text("${minute + 1}", color = if (isSelected) palette.onFill else MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.accent,
                            selectedLabelColor = palette.onFill,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (hasContent) palette.success.copy(alpha = 0.18f) else palette.surfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = palette.border,
                            selectedBorderColor = palette.accent,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SceneEditor(
                scene = customSteps[selectedMinute],
                onSceneChange = { updated ->
                    customSteps = customSteps.toMutableList().also { it[selectedMinute] = updated }
                },
            )

            Spacer(modifier = Modifier.height(14.dp))

            val customRunning = runState?.isRunning == true && runState?.program?.name == "Custom"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SolidButton(
                    text = if (customRunning) "Restart" else "Run Custom",
                    onClick = { runner.start(scope, ChairProgramDef("Custom", customSteps)) },
                    modifier = Modifier.weight(1f),
                )
                SolidButton(
                    text = "Stop",
                    onClick = { runner.stop() },
                    modifier = Modifier.weight(1f),
                    style = ButtonStyle.Secondary,
                    enabled = runState?.isRunning == true,
                )
            }

            if (customRunning) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(color = palette.success, size = 7.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Running – minute ${(runState?.currentMinute ?: 0) + 1} / ${ChairProgramDef.MINUTES}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.success,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = newProgramName,
                    onValueChange = { newProgramName = it },
                    placeholder = { Text("Name this sequence to save it", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = palette.border,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = palette.accent,
                    ),
                )
                SolidButton(
                    text = "Save",
                    enabled = newProgramName.isNotBlank(),
                    onClick = {
                        val name = newProgramName.trim()
                        savedPrograms = savedPrograms.filterNot { it.name == name } + ChairProgramDef(name, customSteps)
                        store.saveAll(savedPrograms)
                        newProgramName = ""
                    },
                )
            }
        }

        if (savedPrograms.isNotEmpty()) {
            SectionDivider()
            Section("Saved Programs") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    savedPrograms.forEach { program ->
                        val isThisRunning = runState?.isRunning == true && runState?.program?.name == program.name
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(program.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                if (isThisRunning) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusDot(color = palette.success, size = 6.dp)
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            "Minute ${(runState?.currentMinute ?: 0) + 1} / ${ChairProgramDef.MINUTES}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.success,
                                        )
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SolidButton(
                                    text = if (isThisRunning) "Restart" else "Run",
                                    onClick = { runner.start(scope, program) },
                                    style = if (isThisRunning) ButtonStyle.Secondary else ButtonStyle.Primary,
                                )
                                SolidButton(
                                    text = "Delete",
                                    style = ButtonStyle.Destructive,
                                    onClick = {
                                        savedPrograms = savedPrograms.filterNot { it.name == program.name }
                                        store.saveAll(savedPrograms)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
