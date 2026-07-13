package com.mcs1000h.remote.ble

/** A 15-minute sequence of [ChairScene] snapshots, one per minute. */
data class ChairProgramDef(
    val name: String,
    val steps: List<ChairScene>,
) {
    init {
        require(steps.size == MINUTES) { "Program must have exactly $MINUTES steps, got ${steps.size}" }
    }

    companion object {
        const val MINUTES = 15

        fun blank(name: String): ChairProgramDef = ChairProgramDef(name, List(MINUTES) { ChairScene() })
    }
}

private fun bitSet(value: Int, minuteIndex: Int): Boolean = (value shr minuteIndex) and 1 == 1

/** Mirrors the OEM app's ProgramBean field set: one 15-bit mask per feature, bit i = minute i. */
private class RawProgramFields(
    val neckForward: Int, val neckReverse: Int, val neckHeat: Int,
    val shiatsuUpper: Int, val shiatsuLower: Int, val shiatsuFull: Int,
    val rollingUpper: Int, val rollingLower: Int, val rollingFull: Int,
    val tappingUpper: Int, val tappingLower: Int, val tappingFull: Int,
    val shiatsuTappingUpper: Int, val shiatsuTappingLower: Int, val shiatsuTappingFull: Int,
    val backHeat: Int, val seatHeat: Int, val seatVibration: Int,
)

private fun buildProgram(name: String, f: RawProgramFields): ChairProgramDef {
    val steps = (0 until ChairProgramDef.MINUTES).map { i ->
        val neckDir = when {
            bitSet(f.neckForward, i) -> NeckDirection.FORWARD
            bitSet(f.neckReverse, i) -> NeckDirection.REVERSE
            else -> NeckDirection.OFF
        }
        val (zone, type) = when {
            bitSet(f.shiatsuTappingUpper, i) -> BackZone.UPPER to MassageType.SHIATSU_TAPPING
            bitSet(f.shiatsuTappingLower, i) -> BackZone.LOWER to MassageType.SHIATSU_TAPPING
            bitSet(f.shiatsuTappingFull, i) -> BackZone.FULL to MassageType.SHIATSU_TAPPING
            bitSet(f.shiatsuUpper, i) -> BackZone.UPPER to MassageType.SHIATSU
            bitSet(f.shiatsuLower, i) -> BackZone.LOWER to MassageType.SHIATSU
            bitSet(f.shiatsuFull, i) -> BackZone.FULL to MassageType.SHIATSU
            bitSet(f.rollingUpper, i) -> BackZone.UPPER to MassageType.ROLLING
            bitSet(f.rollingLower, i) -> BackZone.LOWER to MassageType.ROLLING
            bitSet(f.rollingFull, i) -> BackZone.FULL to MassageType.ROLLING
            bitSet(f.tappingUpper, i) -> BackZone.UPPER to MassageType.TAPPING
            bitSet(f.tappingLower, i) -> BackZone.LOWER to MassageType.TAPPING
            bitSet(f.tappingFull, i) -> BackZone.FULL to MassageType.TAPPING
            else -> BackZone.OFF to MassageType.OFF
        }
        ChairScene(
            zone = zone,
            massageType = type,
            backHeat = bitSet(f.backHeat, i),
            neckDirection = neckDir,
            neckHeat = bitSet(f.neckHeat, i),
            seatVibration = bitSet(f.seatVibration, i),
            seatHeat = bitSet(f.seatHeat, i),
        )
    }
    return ChairProgramDef(name, steps)
}

/** The three built-in programs recovered from the OEM app's ProgramDB seed data. */
object ChairPresetPrograms {
    val RELAX = buildProgram(
        "Relax",
        RawProgramFields(
            neckForward = 7939, neckReverse = 24828, neckHeat = 32767,
            shiatsuUpper = 224, shiatsuLower = 1792, shiatsuFull = 28,
            rollingUpper = 6144, rollingLower = 0, rollingFull = 24579,
            tappingUpper = 0, tappingLower = 0, tappingFull = 0,
            shiatsuTappingUpper = 0, shiatsuTappingLower = 0, shiatsuTappingFull = 0,
            backHeat = 8191, seatHeat = 2044, seatVibration = 0,
        ),
    )

    val INVIGORATE = buildProgram(
        "Invigorate",
        RawProgramFields(
            neckForward = 195, neckReverse = 828, neckHeat = 1008,
            shiatsuUpper = 0, shiatsuLower = 0, shiatsuFull = 3,
            rollingUpper = 0, rollingLower = 0, rollingFull = 0,
            tappingUpper = 48, tappingLower = 192, tappingFull = 12,
            shiatsuTappingUpper = 0, shiatsuTappingLower = 0, shiatsuTappingFull = 768,
            backHeat = 1023, seatHeat = 0, seatVibration = 1023,
        ),
    )

    val FITNESS = buildProgram(
        "Fitness",
        RawProgramFields(
            neckForward = 63, neckReverse = 4032, neckHeat = 4095,
            shiatsuUpper = 0, shiatsuLower = 0, shiatsuFull = 56,
            rollingUpper = 0, rollingLower = 0, rollingFull = 7,
            tappingUpper = 0, tappingLower = 0, tappingFull = 448,
            shiatsuTappingUpper = 0, shiatsuTappingLower = 0, shiatsuTappingFull = 3584,
            backHeat = 4095, seatHeat = 4095, seatVibration = 3640,
        ),
    )

    val ALL = listOf(RELAX, INVIGORATE, FITNESS)
}
