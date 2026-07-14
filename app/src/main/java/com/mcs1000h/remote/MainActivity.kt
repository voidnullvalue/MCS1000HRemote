package com.mcs1000h.remote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.mcs1000h.remote.ble.ChairBleManager
import com.mcs1000h.remote.ble.ChairProgramRunner
import com.mcs1000h.remote.ble.ConnectionState
import com.mcs1000h.remote.ui.AeroSegmentedControl
import com.mcs1000h.remote.ui.ConnectionStatusBar
import com.mcs1000h.remote.ui.ManualScreen
import com.mcs1000h.remote.ui.ProgramScreen
import com.mcs1000h.remote.ui.SceneScreen
import com.mcs1000h.remote.ui.aeroGlassChrome
import com.mcs1000h.remote.ui.theme.LocalAeroPalette
import com.mcs1000h.remote.ui.theme.LocalHazeState
import com.mcs1000h.remote.ui.theme.MCS1000HRemoteTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

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
        enableEdgeToEdge()

        chairManager = ChairBleManager(this)
        programRunner = ChairProgramRunner(chairManager)

        setContent {
            MCS1000HRemoteTheme {
                val darkTheme = isSystemInDarkTheme()
                LaunchedEffect(darkTheme) {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                val connectionState by chairManager.connectionState.collectAsState()
                val status by chairManager.status.collectAsState()

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
    val palette = LocalAeroPalette.current
    val hazeState = remember { HazeState() }

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Aero "desktop" wallpaper gradient - also the blur source the glass title bar reads.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(palette.desktopTop, palette.desktopBottom)))
                    .hazeSource(state = hazeState),
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aeroGlassChrome(shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                    ) {
                        Text(
                            "MCS-1000H Remote",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Massage cushion control",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    ConnectionStatusBar(
                        state = connectionState,
                        onConnectClick = onConnectClick,
                        onDisconnectClick = onDisconnectClick,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (connectionState == ConnectionState.Connected && status != null) {
                        AeroSegmentedControl(
                            options = ChairTab.entries.map { it.label },
                            selectedIndex = selectedTab.ordinal,
                            onSelect = { selectedTab = ChairTab.entries[it] },
                        )

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
                                    color = MaterialTheme.colorScheme.onBackground,
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
    }
}
