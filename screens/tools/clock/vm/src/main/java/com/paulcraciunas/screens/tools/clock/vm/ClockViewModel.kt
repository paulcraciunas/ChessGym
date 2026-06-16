package com.paulcraciunas.screens.tools.clock.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.PulseTimer
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

private data class VmState(
    val status: Status = Status.Setup,
    val selectedMinutes: Int = DEFAULT_MINUTES,
    val selectedIncrement: Int = DEFAULT_INCREMENT,
    val whiteTimeMs: Long = selectedMinutes.minutes.inWholeMilliseconds,
    val blackTimeMs: Long = selectedMinutes.minutes.inWholeMilliseconds,
    val activePlayer: Side? = null,
) {
    enum class Status { Setup, Running, Finished }

    companion object {
        const val DEFAULT_MINUTES = 5
        const val DEFAULT_INCREMENT = 1
    }
}

@HiltViewModel
class ClockViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val pulseTimer: PulseTimer,
) : ViewModel() {
    private var pulseJob: Job? = null
    private val vmState = MutableStateFlow(VmState())
    val uiState: StateFlow<ClockUiState> = vmState
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VmState().toUiState()
        )

    fun onWhiteTapped() = onPlayerTapped(Side.WHITE)
    fun onBlackTapped() = onPlayerTapped(Side.BLACK)

    private fun onPlayerTapped(side: Side) {
        when (vmState.value.status) {
            VmState.Status.Setup -> if (side == Side.WHITE) startPlaying()
            VmState.Status.Running -> onClockTapped(side)
            VmState.Status.Finished -> {}
        }
    }

    fun onStop() = resetToSetup()
    fun onNewGame() = resetToSetup()

    fun onTimeSelected(minutes: Int) {
        vmState.update {
            if (it.status == VmState.Status.Setup) {
                val timeMs = minutes.minutes.inWholeMilliseconds
                it.copy(selectedMinutes = minutes, whiteTimeMs = timeMs, blackTimeMs = timeMs)
            } else it
        }
    }

    fun onIncrementSelected(increment: Int) {
        vmState.update { if (it.status == VmState.Status.Setup) it.copy(selectedIncrement = increment) else it }
    }

    fun onClockTapped(playerTapping: Side) {
        if (vmState.value.activePlayer != playerTapping) return

        stopPulse()
        vmState.update {
            val incrementMs = it.selectedIncrement * 1000L
            it.copy(
                whiteTimeMs = it.whiteTimeMs + if (playerTapping == Side.WHITE) incrementMs else 0L,
                blackTimeMs = it.blackTimeMs + if (playerTapping == Side.BLACK) incrementMs else 0L,
                activePlayer = playerTapping.other(),
            )
        }
        startPulse()
    }

    private fun startPlaying() {
        vmState.update { it.copy(status = VmState.Status.Running, activePlayer = Side.BLACK) }
        startPulse()
    }

    private fun resetToSetup() {
        stopPulse()
        vmState.update { VmState(selectedMinutes = it.selectedMinutes, selectedIncrement = it.selectedIncrement) }
    }

    private fun startPulse() {
        pulseJob?.cancel()
        pulseJob = viewModelScope.launch(dispatcher) {
            pulseTimer.start(intervalMillis = TICK_INTERVAL_MS).collect { elapsedMs ->
                vmState.update { current ->
                    when (current.activePlayer) {
                        Side.WHITE -> {
                            val newTime = (current.whiteTimeMs - elapsedMs).coerceAtLeast(0L)
                            current.copy(whiteTimeMs = newTime, status = newTime.toState())
                        }
                        Side.BLACK -> {
                            val newTime = (current.blackTimeMs - elapsedMs).coerceAtLeast(0L)
                            current.copy(blackTimeMs = newTime, status = newTime.toState())
                        }
                        null -> current
                    }
                }
                if (vmState.value.status == VmState.Status.Finished) {
                    stopPulse()
                }
            }
        }
    }

    private fun stopPulse() {
        pulseJob?.cancel()
        pulseJob = null
    }

    private fun VmState.toUiState(): ClockUiState = when (status) {
        VmState.Status.Setup -> ClockUiState.Setup(
            whiteTime = formatTime(whiteTimeMs),
            blackTime = formatTime(blackTimeMs),
            selectedMinutes = selectedMinutes,
            selectedIncrement = selectedIncrement,
        )
        VmState.Status.Running -> ClockUiState.Playing(
            whiteTime = formatTime(whiteTimeMs),
            blackTime = formatTime(blackTimeMs),
            activePlayer = activePlayer ?: Side.WHITE,
        )
        VmState.Status.Finished -> ClockUiState.Finished(
            whiteTime = formatTime(whiteTimeMs),
            blackTime = formatTime(blackTimeMs),
            loser = activePlayer ?: Side.WHITE,
        )
    }

    private fun Long.toState(): VmState.Status = if (this == 0L) VmState.Status.Finished else VmState.Status.Running

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        val tenths = (millis % 1000) / 100

        return when {
            minutes > 0 -> "%d:%02d".format(minutes, seconds)
            else -> "%d.%d".format(seconds, tenths) // Show tenths only when under a minute
        }
    }

    companion object {
        internal const val TICK_INTERVAL_MS = 100L
    }
}
