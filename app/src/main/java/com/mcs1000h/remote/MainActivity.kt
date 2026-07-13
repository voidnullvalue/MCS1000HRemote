package com.mcs1000h.remote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mcs1000h.remote.ble.ChairBleManager
import com.mcs1000h.remote.ble.ChairProgramRunner
import com.mcs1000h.remote.ble.ConnectionState
import com.mcs1000h.remote.ui.ConnectionStatusBar
import com.mcs1000h.remote.ui.ManualScreen
import com.mcs1000h.remote.ui.ProgramScreen
import com.mcs1000h.remote.ui.SceneScreen
import com.mcs1000h.remote.ui.theme.MCS1000HRemoteTheme

private enum class ChairTab(val label: String) {
    MANUAL("Manual"),
    SCENE("Scene"),
    PROGRAMS("Programs"),
}

class MainActivity : ComponentActivity() {
    private lateinit var chairManager: ChairBleManager
    private lateinit var programRunner: ChairProgramRunner

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startBleConnection()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chairManager = ChairBleManager(this)
        programRunner = ChairProgramRunner(chairManager)

        setContent {
            MCS1000HRemoteTheme {
                val connectionState by chairManager.connectionState.collectAsState()
                val status by chairManager.status.collectAsState()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ChairApp(
                        connectionState = connectionState,
                        status = status,
                        chairManager = chairManager,
                        programRunner = programRunner,
                        onConnectClick = { requestPermissionsAndConnect() },
                        onDisconnectClick = { chairManager.disconnect() },
                    )
                }
            }
        }

        requestPermissionsAndConnect()
    }

    private fun requestPermissionsAndConnect() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }.toTypedArray()

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isEmpty()) {
            startBleConnection()
        } else {
            permissionLauncher.launch(missingPermissions)
        }
    }

    private fun startBleConnection() {
        chairManager.startScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        programRunner.stop()
        chairManager.disconnect()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChairApp(
    connectionState: ConnectionState,
    status: com.mcs1000h.remote.ble.ChairStatus?,
    chairManager: ChairBleManager,
    programRunner: ChairProgramRunner,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(ChairTab.MANUAL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("MCS-1000H Remote", style = MaterialTheme.typography.titleLarge)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            ConnectionStatusBar(
                state = connectionState,
                onConnectClick = onConnectClick,
                onDisconnectClick = onDisconnectClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (connectionState == ConnectionState.Connected && status != null) {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                ) {
                    ChairTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.label, fontWeight = FontWeight.Medium) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    ChairTab.MANUAL -> ManualScreen(status = status, chairManager = chairManager)
                    ChairTab.SCENE -> SceneScreen(runner = programRunner)
                    ChairTab.PROGRAMS -> ProgramScreen(runner = programRunner)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (connectionState) {
                                ConnectionState.Idle -> "Ready to connect"
                                ConnectionState.Scanning -> "Scanning for chair…"
                                ConnectionState.Connecting -> "Connecting…"
                                ConnectionState.DiscoveringServices -> "Discovering services…"
                                ConnectionState.Connected -> "Connected"
                                ConnectionState.Disconnected -> "Disconnected"
                                is ConnectionState.Unsupported -> "Error: ${connectionState.reason}"
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Looking for \"MCS-1000H Massage Cushion\"",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
