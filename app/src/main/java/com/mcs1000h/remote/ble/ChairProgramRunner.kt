package com.mcs1000h.remote.ble

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProgramRunState(
    val program: ChairProgramDef,
    val currentMinute: Int,
    val isRunning: Boolean,
)

/**
 * Drives a [ChairProgramDef] over BLE, minute by minute. The cushion's firmware only
 * counts down an internal one-minute timer and reports the remainder in the status
 * frame's 9th byte (see [ChairStatus.programMinutesRemaining]) - it does not store a
 * sequence itself. Matching the OEM app's FragmentProgram, this class watches that
 * countdown and pushes the next minute's [ChairScene] the instant it ticks over; if
 * the phone stops driving it, the cushion just keeps whatever combo was last sent.
 */
class ChairProgramRunner(private val manager: ChairBleManager) {
    private var job: Job? = null

    private val _state = MutableStateFlow<ProgramRunState?>(null)
    val state: StateFlow<ProgramRunState?> = _state

    fun start(scope: CoroutineScope, program: ChairProgramDef) {
        stop()
        _state.value = ProgramRunState(program, currentMinute = 0, isRunning = true)
        job = scope.launch {
            manager.sendFrame(ChairProtocol.buildCommand(ChairCommand.PROGRAM_START))
            delay(200)
            manager.sendScene(program.steps[0])
            var lastMinute = 0
            manager.status.collect { status ->
                val remaining = status?.programMinutesRemaining ?: return@collect
                val elapsed = (ChairProgramDef.MINUTES - remaining).coerceIn(0, ChairProgramDef.MINUTES - 1)
                if (elapsed != lastMinute) {
                    lastMinute = elapsed
                    manager.sendScene(program.steps[elapsed])
                    _state.value = _state.value?.copy(currentMinute = elapsed)
                }
            }
        }
    }

    /** Cancels our per-minute pushes and tells the cushion to go idle right away. */
    fun stop() {
        job?.cancel()
        job = null
        if (_state.value?.isRunning == true) {
            manager.sendScene(ChairScene())
        }
        _state.value = _state.value?.copy(isRunning = false)
    }
}
