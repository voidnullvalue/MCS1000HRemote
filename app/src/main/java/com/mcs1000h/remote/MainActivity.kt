package com.mcs1000h.remote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.mcs1000h.remote.ble.ChairBleManager
import com.mcs1000h.remote.ble.ChairProgramRunner
import com.mcs1000h.remote.ble.ConnectionState
import com.mcs1000h.remote.ui.BreakoutGame
import com.mcs1000h.remote.ui.ConnectionStatusRow
import com.mcs1000h.remote.ui.ManualScreen
import com.mcs1000h.remote.ui.ProgramScreen
import com.mcs1000h.remote.ui.SceneScreen
import com.mcs1000h.remote.ui.SegmentedControl
import com.mcs1000h.remote.ui.panelSurface
import com.mcs1000h.remote.ui.theme.LocalAppPalette
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
    var showGame by remember { mutableStateOf(false) }
    val palette = LocalAppPalette.current

    if (showGame) {
        BreakoutGame(onExit = { showGame = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "MCS-1000H Remote",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ConnectionStatusRow(
            state = connectionState,
            onConnectClick = onConnectClick,
            onDisconnectClick = onDisconnectClick,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (connectionState == ConnectionState.Connected && status != null) {
            SegmentedControl(
                options = ChairTab.entries.map { it.label },
                selectedIndex = selectedTab.ordinal,
                onSelect = { selectedTab = ChairTab.entries[it] },
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .panelSurface(palette),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                ) {
                    when (selectedTab) {
                        ChairTab.MANUAL -> ManualScreen(status = status, chairManager = chairManager)
                        ChairTab.SCENE -> SceneScreen(runner = programRunner)
                        ChairTab.PROGRAMS -> ProgramScreen(runner = programRunner)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Looking for \"MCS-1000H Massage Cushion\"",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        AboutCard(onEasterEgg = { showGame = true })
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun AboutCard(onEasterEgg: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapAt by remember { mutableLongStateOf(0L) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .panelSurface(palette)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "build ${BuildConfig.VERSION_CODE}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    val now = SystemClock.elapsedRealtime()
                    tapCount = if (now - lastTapAt > 600L) 1 else tapCount + 1
                    lastTapAt = now
                    if (tapCount >= 3) {
                        tapCount = 0
                        onEasterEgg()
                    }
                },
            )
        }
        Text(
            text = "GitHub ↗",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = palette.accent,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/voidnullvalue/MCS1000HRemote")),
                )
            },
        )
    }
}
