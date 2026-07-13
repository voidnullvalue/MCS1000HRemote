package com.mcs1000h.remote.ble

/**
 * Parsed form of the status frame the cushion pushes over its notify
 * characteristic. Field/bit mapping recovered from the OEM app's fragments
 * (each one only read the couple of bits it cared about; this combines all
 * of them into one snapshot).
 */
data class ChairStatus(
    val powerOn: Boolean,

    val backHeat: Boolean,
    val backSpotUp: Boolean,
    val backSpotDown: Boolean,
    val backSpot: Boolean,
    val backZoneFull: Boolean,
    val backZoneUpper: Boolean,
    val backZoneLower: Boolean,
    val backShiatsu: Boolean,
    val backRolling: Boolean,
    val backTapping: Boolean,

    val neckForward: Boolean,
    val neckReverse: Boolean,
    val neckSpot: Boolean,
    val neckSpotUp: Boolean,
    val neckSpotDown: Boolean,
    /** Neck ball renders red instead of teal when neck heat is on - the OEM app's only tell for this bit. */
    val ballIsRed: Boolean,

    val seatVibration: Boolean,
    val seatHeat: Boolean,

    val programRunning: Boolean,
    val resetBitSet: Boolean,

    /** Position of the neck massage ball along its track, or -1 if unknown. */
    val neckPosition: Int,
    /** Position of the back massage ball along its track, or -1 if unknown. */
    val backPosition: Int,
    /** Minutes remaining in a running program (frame includes a 9th byte), else null. */
    val programMinutesRemaining: Int?,
) {
    val neckHeat: Boolean get() = ballIsRed
    val backMassageActive: Boolean get() = backShiatsu || backRolling || backTapping
    val backWorking: Boolean get() = backPosition > 1 || backMassageActive
    val neckWorking: Boolean get() = neckForward || neckReverse
    val seatWorking: Boolean get() = seatVibration || seatHeat

    companion object {
        fun parse(data: ByteArray): ChairStatus? {
            if (data.size < 6) return null
            fun bit(index: Int, b: Int) = getBit(data, index, b)
            return ChairStatus(
                powerOn = bit(3, 1),
                backHeat = bit(3, 2),
                backSpotUp = bit(3, 3),
                backSpotDown = bit(3, 4),
                backSpot = bit(3, 5),
                backZoneFull = bit(3, 6),
                backZoneUpper = bit(3, 7),
                backZoneLower = bit(3, 8),
                backShiatsu = bit(4, 1),
                backRolling = bit(4, 2),
                backTapping = bit(4, 3),
                neckForward = bit(4, 4),
                neckReverse = bit(4, 5),
                neckSpot = bit(4, 7),
                neckSpotUp = bit(5, 1),
                neckSpotDown = bit(5, 2),
                ballIsRed = bit(5, 3),
                seatVibration = bit(5, 4),
                seatHeat = bit(5, 5),
                programRunning = bit(5, 6),
                resetBitSet = bit(5, 7),
                neckPosition = if (data.size > 6) data[6].toInt() and 0xFF else -1,
                backPosition = if (data.size > 7) data[7].toInt() and 0xFF else -1,
                programMinutesRemaining = if (data.size > 8) data[8].toInt() and 0xFF else null,
            )
        }
    }
}
