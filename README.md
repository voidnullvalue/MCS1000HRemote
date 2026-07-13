# MCS-1000H Remote

An unofficial Android BLE remote for the Homedics MCS-1000H massage cushion, built from
protocol details recovered by reverse-engineering the OEM app.

Not affiliated with or endorsed by Homedics.

## Features

- **Manual** — every individual toggle the OEM app exposes (zones, massage types, heat,
  neck, seat), plus closed-loop position seeking: drag a slider to a target % and the app
  nudges the motor and watches status feedback settle there, instead of holding a button
  and eyeballing it.
- **Scene** — set back zone + massage type + heat + neck + seat all in a single BLE write.
  The OEM app never exposed this; it's built on the same direct-state frame their Program
  mode uses internally, decoded from `FragmentProgram.sendProgramData()`.
- **Programs** — full 15-minute custom sequencing (one scene per minute), plus the three
  built-in presets (Relax, Invigorate, Fitness) decoded from the OEM app's seeded SQLite
  bitmasks.

## Protocol notes

- GATT service `fff0`, write to `fff5`, notifications on `fff4`.
- Command frames are 8 bytes: `[0xA5, 0x5A, opcode, byte3, byte4, 0, 0, checksum]`, checksum
  = sum of `~byte` for bytes 0..6, truncated to a byte.
- Two frame styles: single-opcode toggles (`0x33`-`0x47`, one feature flips per write) and a
  direct-state frame (opcode `0`, bit-packed `byte3`/`byte4`) that sets several features at
  once — only used by the OEM's Program mode, repurposed here as a general "Scene" write.
- See `ble/ChairProtocol.kt`, `ble/ChairStatus.kt`, and `ble/ChairScene.kt` for the exact
  bit layouts.

## Building

```
./gradlew assembleDebug
```

Releases are built and published automatically by CI when a `v*` tag is pushed (see
`.github/workflows/release.yml`); they're debug-signed since this is sideloaded, not
distributed through a store.
