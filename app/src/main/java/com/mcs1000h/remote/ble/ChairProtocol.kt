package com.mcs1000h.remote.ble

import java.util.UUID

/** GATT UUIDs recovered from the OEM app's BluetoothConst/BluetoothLeService. */
object ChairGatt {
    val SERVICE: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    val CHARACTERISTIC_WRITE: UUID = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb")
    val CHARACTERISTIC_NOTIFY: UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")
    val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    const val DEVICE_NAME = "MCS-1000H Massage Cushion"
    /** Substring used to match scan results - the OEM's full local name can get truncated in the advertisement. */
    const val DEVICE_NAME_MATCH = "MCS-1000H"
}

/**
 * Builds the 8-byte command frames the cushion expects:
 * `[0xA5, 0x5A, opcode, byte3, byte4, 0, 0, checksum]`.
 *
 * The checksum is the sum of the bitwise-NOT of bytes 0..6, truncated to a
 * byte - ported as-is from the OEM app's DataUtils.ChuSum(), which every
 * manual-control button in the original relied on to be accepted by the
 * cushion's firmware.
 */
object ChairProtocol {
    private const val HEADER1 = 0xA5
    private const val HEADER2 = 0x5A

    fun buildCommand(command: ChairCommand): ByteArray = buildFrame(opcode = command.opcode)

    fun buildFrame(opcode: Int = 0, byte3: Int = 0, byte4: Int = 0): ByteArray {
        val frame = IntArray(8)
        frame[0] = HEADER1
        frame[1] = HEADER2
        frame[2] = opcode and 0xFF
        frame[3] = byte3 and 0xFF
        frame[4] = byte4 and 0xFF
        frame[5] = 0
        frame[6] = 0
        frame[7] = checksum(frame)
        return ByteArray(8) { frame[it].toByte() }
    }

    private fun checksum(frame: IntArray): Int {
        var sum = 0
        for (i in 0..6) sum += (frame[i] xor 0xFF) and 0xFF
        return sum and 0xFF
    }
}

/** Bit `bit` (1 = LSB) of `data[index]`, matching the OEM app's DataUtils.get_bit_value(). */
fun getBit(data: ByteArray, index: Int, bit: Int): Boolean {
    if (index < 0 || index >= data.size) return false
    val num = data[index].toInt()
    val mask = 1 shl (bit - 1)
    return (num and mask) == mask
}
