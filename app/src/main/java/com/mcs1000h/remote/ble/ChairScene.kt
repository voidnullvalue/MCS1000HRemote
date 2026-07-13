package com.mcs1000h.remote.ble

enum class BackZone { OFF, UPPER, LOWER, FULL }
enum class MassageType { OFF, SHIATSU, ROLLING, TAPPING, SHIATSU_TAPPING }
enum class NeckDirection { OFF, FORWARD, REVERSE }

/**
 * A complete state snapshot - zone, massage type, heat, neck direction, seat - encoded
 * into a single 8-byte frame. Recovered from the OEM app's Program mode, which is the
 * only place it drives more than one feature per BLE write: FragmentProgram.sendProgramData()
 * builds exactly this frame (opcode 0, bit-packed byte3/byte4) once per simulated minute.
 * The single-toggle [ChairCommand] opcodes can only flip one feature relative to whatever
 * the cushion is already doing; this sets the whole state at once instead.
 *
 * Requires the cushion to be in program mode (see [ChairCommand.PROGRAM_START]) - firmware
 * only appears to interpret opcode-0 frames as state pushes in that mode, matching how the
 * OEM app always sends PROGRAM_START before ever writing one of these.
 */
data class ChairScene(
    val zone: BackZone = BackZone.OFF,
    val massageType: MassageType = MassageType.OFF,
    val backHeat: Boolean = false,
    val neckDirection: NeckDirection = NeckDirection.OFF,
    val neckHeat: Boolean = false,
    val seatVibration: Boolean = false,
    val seatHeat: Boolean = false,
) {
    fun toFrame(): ByteArray {
        var byte3 = 0
        var byte4 = 0

        if (backHeat) byte3 = byte3 or BIT1

        val wantsShiatsu = massageType == MassageType.SHIATSU || massageType == MassageType.SHIATSU_TAPPING
        val wantsRolling = massageType == MassageType.ROLLING
        val wantsTapping = massageType == MassageType.TAPPING || massageType == MassageType.SHIATSU_TAPPING
        if (wantsShiatsu) byte3 = byte3 or BIT2
        if (wantsRolling) byte3 = byte3 or BIT3
        if (wantsTapping) byte3 = byte3 or BIT4

        when (zone) {
            BackZone.FULL -> byte3 = byte3 or BIT5
            BackZone.UPPER -> byte3 = byte3 or BIT6
            BackZone.LOWER -> byte4 = byte4 or BIT3
            BackZone.OFF -> {}
        }

        when (neckDirection) {
            NeckDirection.FORWARD -> byte4 = byte4 or BIT1
            NeckDirection.REVERSE -> byte4 = byte4 or BIT2
            NeckDirection.OFF -> {}
        }
        if (neckHeat) byte4 = byte4 or BIT4
        if (seatVibration) byte4 = byte4 or BIT5
        if (seatHeat) byte4 = byte4 or BIT6

        // Bit 7 of both bytes is an "apply this frame" flag the OEM app sets
        // unconditionally on every program-mode state frame, on/off minutes included.
        byte3 = byte3 or BIT7
        byte4 = byte4 or BIT7

        return ChairProtocol.buildFrame(opcode = 0, byte3 = byte3, byte4 = byte4)
    }

    companion object {
        private const val BIT1 = 1 shl 0
        private const val BIT2 = 1 shl 1
        private const val BIT3 = 1 shl 2
        private const val BIT4 = 1 shl 3
        private const val BIT5 = 1 shl 4
        private const val BIT6 = 1 shl 5
        private const val BIT7 = 1 shl 6
    }
}
