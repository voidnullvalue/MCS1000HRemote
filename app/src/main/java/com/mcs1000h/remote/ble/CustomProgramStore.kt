package com.mcs1000h.remote.ble

import android.content.Context

private const val PREFS_NAME = "custom_programs"
private const val KEY_PROGRAMS = "programs"

// Control characters, not punctuation a user would type into a program name.
private const val RECORD_SEP = ""
private const val NAME_SEP = ""
private const val SCENE_SEP = ";"
private const val FIELD_SEP = ","

/**
 * Local persistence for user-authored custom programs - the one real feature gap versus the
 * OEM app, which lets you save/name/delete your own 15-minute sequences (SQLite-backed) while
 * this app's Custom Program editor used to be `remember`-only and forgot everything on exit.
 * A flat SharedPreferences string is enough for this: a handful of 15-scene programs, no
 * queries, no need for a real database dependency.
 */
class CustomProgramStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadAll(): List<ChairProgramDef> {
        val raw = prefs.getString(KEY_PROGRAMS, null)
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull(::decodeProgram)
    }

    fun saveAll(programs: List<ChairProgramDef>) {
        prefs.edit().putString(KEY_PROGRAMS, programs.joinToString(RECORD_SEP, transform = ::encodeProgram)).apply()
    }

    private fun encodeProgram(program: ChairProgramDef): String {
        val scenes = program.steps.joinToString(SCENE_SEP, transform = ::encodeScene)
        return "${program.name}$NAME_SEP$scenes"
    }

    private fun decodeProgram(record: String): ChairProgramDef? {
        val parts = record.split(NAME_SEP)
        if (parts.size != 2 || parts[0].isBlank()) return null
        val steps = parts[1].split(SCENE_SEP).mapNotNull(::decodeScene)
        if (steps.size != ChairProgramDef.MINUTES) return null
        return ChairProgramDef(parts[0], steps)
    }

    private fun encodeScene(scene: ChairScene): String = listOf(
        scene.zone.ordinal,
        scene.massageType.ordinal,
        if (scene.backHeat) 1 else 0,
        scene.neckDirection.ordinal,
        if (scene.neckHeat) 1 else 0,
        if (scene.seatVibration) 1 else 0,
        if (scene.seatHeat) 1 else 0,
    ).joinToString(FIELD_SEP)

    private fun decodeScene(record: String): ChairScene? {
        val f = record.split(FIELD_SEP).map { it.toIntOrNull() }
        if (f.size != 7 || f.any { it == null }) return null
        val zone = BackZone.entries.getOrNull(f[0]!!) ?: return null
        val massageType = MassageType.entries.getOrNull(f[1]!!) ?: return null
        val neckDirection = NeckDirection.entries.getOrNull(f[3]!!) ?: return null
        return ChairScene(
            zone = zone,
            massageType = massageType,
            backHeat = f[2] == 1,
            neckDirection = neckDirection,
            neckHeat = f[4] == 1,
            seatVibration = f[5] == 1,
            seatHeat = f[6] == 1,
        )
    }
}
