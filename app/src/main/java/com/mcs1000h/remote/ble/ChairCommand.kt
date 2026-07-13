package com.mcs1000h.remote.ble

/**
 * Single-byte opcodes accepted by the MCS-1000H cushion's BLE command
 * characteristic. Recovered from the OEM app's manual-control fragments:
 * every button maps to exactly one opcode in a contiguous 0x33..0x47 run,
 * sent inside an 8-byte frame (see [ChairProtocol.buildCommand]).
 */
enum class ChairCommand(val opcode: Int) {
    POWER_TOGGLE(0x33),

    BACK_HEAT_TOGGLE(0x34),
    BACK_SPOT_UP(0x35),
    BACK_SPOT_DOWN(0x36),
    BACK_SPOT(0x37),

    BACK_ZONE_FULL(0x38),
    BACK_ZONE_UPPER(0x39),
    BACK_ZONE_LOWER(0x3A),

    BACK_MASSAGE_SHIATSU(0x3B),
    BACK_MASSAGE_ROLLING(0x3C),
    BACK_MASSAGE_TAPPING(0x3D),

    NECK_MASSAGE_FORWARD(0x3E),
    NECK_MASSAGE_REVERSE(0x3F),

    BACK_MASSAGE_SHIATSU_TAPPING(0x40),

    NECK_SPOT(0x41),
    NECK_SPOT_UP(0x42),
    NECK_SPOT_DOWN(0x43),
    NECK_HEAT_TOGGLE(0x44),

    SEAT_VIBRATION_TOGGLE(0x45),
    SEAT_HEAT_TOGGLE(0x46),

    /** Tells the cushion a 15-minute program is about to start driving it minute-by-minute. */
    PROGRAM_START(0x47),
}
