package com.mcs1000h.remote.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.location.LocationManagerCompat
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Scanning : ConnectionState
    data object Connecting : ConnectionState
    data object DiscoveringServices : ConnectionState
    data object Connected : ConnectionState
    data object Disconnected : ConnectionState
    data class Unsupported(val reason: String) : ConnectionState
}

/**
 * Owns the BLE connection to a single MCS-1000H cushion: scan, connect,
 * subscribe to status notifications, and write command frames. There is no
 * server component and nothing here ever leaves the phone - the cushion talks
 * BLE GATT only, so "the cloud" isn't part of this protocol at all.
 */
@SuppressLint("MissingPermission") // callers are required to check permissions first; see MainActivity
class ChairBleManager(context: Context) {

    private val appContext = context.applicationContext
    private val bluetoothManager =
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private val mainHandler = Handler(Looper.getMainLooper())

    private var gatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var scanning = false

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _status = MutableStateFlow<ChairStatus?>(null)
    val status: StateFlow<ChairStatus?> = _status

    private val _rawFrames = MutableSharedFlow<ByteArray>(extraBufferCapacity = 16)
    val rawFrames: SharedFlow<ByteArray> = _rawFrames

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // scanRecord.deviceName comes straight off the advertisement/scan-response bytes
            // already delivered in this callback. device.getName() instead can trigger a
            // remote name lookup that silently needs BLUETOOTH_CONNECT on API 31+ and may
            // just return null - which was making scans never match on some phones.
            val advertisedName = result.scanRecord?.deviceName ?: runCatching { result.device.name }.getOrNull()
            val name = advertisedName ?: return
            Log.d(TAG, "Scan result: \"$name\" (${result.device.address}, rssi=${result.rssi})")
            // Contains rather than exact-equals: the cushion's complete local name can get
            // truncated to a shortened form if it doesn't fit a legacy 31-byte adv packet.
            if (!name.contains(ChairGatt.DEVICE_NAME_MATCH, ignoreCase = true)) return
            stopScan()
            connectTo(result.device.address)
        }

        override fun onScanFailed(errorCode: Int) {
            scanning = false
            _connectionState.value = ConnectionState.Unsupported("Bluetooth scan failed (code $errorCode)")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.DiscoveringServices
                    g.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    g.close()
                    gatt = null
                    writeCharacteristic = null
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val service = g.getService(ChairGatt.SERVICE)
            val write = service?.getCharacteristic(ChairGatt.CHARACTERISTIC_WRITE)
            val notify = service?.getCharacteristic(ChairGatt.CHARACTERISTIC_NOTIFY)
            if (service == null || write == null || notify == null) {
                _connectionState.value = ConnectionState.Unsupported("Cushion did not expose the expected BLE service")
                return
            }
            writeCharacteristic = write
            g.setCharacteristicNotification(notify, true)
            val cccd = notify.getDescriptor(ChairGatt.CLIENT_CHARACTERISTIC_CONFIG)
            if (cccd != null) {
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                g.writeDescriptor(cccd)
            }
            _connectionState.value = ConnectionState.Connected
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value ?: return
            handleIncoming(value)
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            handleIncoming(value)
        }
    }

    private fun handleIncoming(value: ByteArray) {
        _rawFrames.tryEmit(value)
        ChairStatus.parse(value)?.let { _status.value = it }
    }

    /** Scans for the cushion by name and connects as soon as it's found. Times out after 15s. */
    fun startScan() {
        val bleScanner = adapter?.bluetoothLeScanner ?: run {
            _connectionState.value = ConnectionState.Unsupported("Bluetooth is not available or disabled")
            return
        }
        // Below Android 12, BLE scans silently return zero results (no error callback at all)
        // if the phone's system Location toggle is off, regardless of app permissions - this
        // is the single most common reason a scan just never finds anything.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isLocationEnabled()) {
            _connectionState.value =
                ConnectionState.Unsupported("Turn on Location in system settings - Android requires it for Bluetooth scanning")
            return
        }
        if (scanning) return
        scanning = true
        _connectionState.value = ConnectionState.Scanning
        // Explicit LOW_LATENCY: the no-args startScan() overload defaults to a duty-cycled
        // low-power scan window that can easily miss an infrequent advertiser inside our
        // timeout, unlike the OEM app's always-on legacy startLeScan().
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        bleScanner.startScan(null, settings, scanCallback)
        mainHandler.postDelayed({
            if (scanning) {
                stopScan()
                if (_connectionState.value is ConnectionState.Scanning) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }, SCAN_TIMEOUT_MS)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    fun stopScan() {
        if (!scanning) return
        scanning = false
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private fun connectTo(address: String) {
        val device = adapter?.getRemoteDevice(address) ?: return
        _connectionState.value = ConnectionState.Connecting
        gatt = device.connectGatt(appContext, false, gattCallback)
    }

    fun disconnect() {
        stopScan()
        gatt?.disconnect()
    }

    @Suppress("DEPRECATION")
    fun send(command: ChairCommand) = sendFrame(ChairProtocol.buildCommand(command))

    /** Pushes a whole combined [ChairScene] in one frame; see [ChairScene] for why this needs program mode. */
    fun sendScene(scene: ChairScene) = sendFrame(scene.toFrame())

    @Suppress("DEPRECATION")
    fun sendFrame(frame: ByteArray) {
        val characteristic = writeCharacteristic ?: return
        val g = gatt ?: return
        characteristic.value = frame
        g.writeCharacteristic(characteristic)
    }

    /**
     * Closed-loop drive to an exact back-spot position (0-100), nudging
     * [ChairCommand.BACK_SPOT_UP]/[ChairCommand.BACK_SPOT_DOWN] and watching
     * [ChairStatus.backPosition] settle - the OEM app only ever sent raw up/down
     * while a finger dragged the slider, with no notion of a target position.
     */
    suspend fun seekBackPosition(target: Int, tolerance: Int = 3, maxSteps: Int = 40) {
        seekPosition(
            target = target,
            tolerance = tolerance,
            maxSteps = maxSteps,
            currentPosition = { status.value?.backPosition ?: -1 },
            stepUp = { send(ChairCommand.BACK_SPOT_UP) },
            stepDown = { send(ChairCommand.BACK_SPOT_DOWN) },
        )
    }

    /** Same closed-loop seek as [seekBackPosition] but for the neck ball. */
    suspend fun seekNeckPosition(target: Int, tolerance: Int = 3, maxSteps: Int = 40) {
        seekPosition(
            target = target,
            tolerance = tolerance,
            maxSteps = maxSteps,
            currentPosition = { status.value?.neckPosition ?: -1 },
            stepUp = { send(ChairCommand.NECK_SPOT_UP) },
            stepDown = { send(ChairCommand.NECK_SPOT_DOWN) },
        )
    }

    private suspend fun seekPosition(
        target: Int,
        tolerance: Int,
        maxSteps: Int,
        currentPosition: () -> Int,
        stepUp: () -> Unit,
        stepDown: () -> Unit,
    ) {
        repeat(maxSteps) {
            val pos = currentPosition()
            if (pos < 0) return
            val diff = target - pos
            if (abs(diff) <= tolerance) return
            if (diff > 0) stepUp() else stepDown()
            delay(280)
        }
    }

    companion object {
        private const val TAG = "ChairBleManager"
        private const val SCAN_TIMEOUT_MS = 15_000L
    }
}
