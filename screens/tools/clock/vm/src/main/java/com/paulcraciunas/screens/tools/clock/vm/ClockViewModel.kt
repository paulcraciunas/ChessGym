package com.paulcraciunas.screens.tools.clock.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.general.BlackTimer
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.CountdownTimer.Remainder
import com.paulcraciunas.domain.api.general.WhiteTimer
import com.paulcraciunas.game.logic.api.Side
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    @param:WhiteTimer private val whiteTimer: CountdownTimer,
    @param:BlackTimer private val blackTimer: CountdownTimer,
) : ViewModel(), ClockScreenInteractor {
    private val _uiState = MutableStateFlow<ClockUiState>(ClockUiState.Setup())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()

    private var selectedMinutes: Int = ClockUiState.DEFAULT_MINUTES
    private var selectedIncrement: Int = ClockUiState.DEFAULT_INCREMENT

    init {
        whiteTimer.setInterval(TICK_INTERVAL_MS.toInt())
        blackTimer.setInterval(TICK_INTERVAL_MS.toInt())

        combine(whiteTimer.remaining, blackTimer.remaining) { white, black ->
            updateFromTimers(white, black)
        }.launchIn(viewModelScope)
    }

    override fun onWhiteTapped() = onPlayerTapped(Side.WHITE)
    override fun onBlackTapped() = onPlayerTapped(Side.BLACK)

    private fun onPlayerTapped(side: Side) {
        _uiState.update { state ->
            when (state) {
                is ClockUiState.Setup -> startPlaying(state, side)
                is ClockUiState.Playing -> updatePlayingState(state, side)
                else -> state
            }
        }
    }

    override fun onStop() = resetToSetup()
    override fun onNewGame() = resetToSetup()

    override fun onTimeSelected(minutes: Int) {
        selectedMinutes = minutes
        _uiState.update { state ->
            if (state is ClockUiState.Setup) state.copy(selectedMinutes = minutes) else state
        }
    }

    override fun onIncrementSelected(increment: Int) {
        selectedIncrement = increment
        _uiState.update { state ->
            if (state is ClockUiState.Setup) state.copy(selectedIncrement = increment) else state
        }
    }

    private fun updatePlayingState(state: ClockUiState.Playing, side: Side): ClockUiState.Playing = if (state.activePlayer == side) {
        val activeTimer = if (side == Side.WHITE) whiteTimer else blackTimer
        val nextTimer = if (side == Side.WHITE) blackTimer else whiteTimer

        activeTimer.stop()
        val newTime = activeTimer.remaining.value + selectedIncrement
        activeTimer.set(newTime)

        nextTimer.start(viewModelScope)

        state.copy(
            whiteTime = whiteTimer.remaining.value,
            blackTime = blackTimer.remaining.value,
            activePlayer = side.other()
        )
    } else state

    private fun startPlaying(state: ClockUiState.Setup, side: Side): ClockUiState.Playing {
        selectedMinutes = state.selectedMinutes
        selectedIncrement = state.selectedIncrement

        whiteTimer.set(state.whiteTime)
        blackTimer.set(state.blackTime)

        if (side == Side.WHITE) whiteTimer.start(viewModelScope)
        else blackTimer.start(viewModelScope)

        return ClockUiState.Playing(state.whiteTime, state.blackTime, activePlayer = side)
    }

    private fun resetToSetup() {
        whiteTimer.stop()
        blackTimer.stop()
        _uiState.value = ClockUiState.Setup(selectedMinutes, selectedIncrement)
    }

    private fun updateFromTimers(white: Remainder, black: Remainder) {
        _uiState.update { state ->
            if (state is ClockUiState.Playing) {
                if (!white.isPositive() || !black.isPositive()) {
                    whiteTimer.stop()
                    blackTimer.stop()
                    ClockUiState.Finished(white, black, loser = state.activePlayer)
                } else {
                    state.copy(whiteTime = white, blackTime = black)
                }
            } else state
        }
    }

    companion object {
        internal const val TICK_INTERVAL_MS = 100L
    }
}
