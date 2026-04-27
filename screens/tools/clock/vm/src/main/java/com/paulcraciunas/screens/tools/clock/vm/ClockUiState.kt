package com.paulcraciunas.screens.tools.clock.vm

import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.game.logic.api.Side

sealed class ClockUiState {
    abstract val whiteTime: CountdownTimer.Remainder
    abstract val blackTime: CountdownTimer.Remainder

    data class Setup(
        val selectedMinutes: Int = DEFAULT_MINUTES,
        val selectedIncrement: Int = DEFAULT_INCREMENT,
    ) : ClockUiState() {
        override val whiteTime: CountdownTimer.Remainder = CountdownTimer.Remainder(selectedMinutes * 60, 0)
        override val blackTime: CountdownTimer.Remainder = CountdownTimer.Remainder(selectedMinutes * 60, 0)
    }

    data class Playing(
        override val whiteTime: CountdownTimer.Remainder,
        override val blackTime: CountdownTimer.Remainder,
        val activePlayer: Side,
    ) : ClockUiState()

    data class Finished(
        override val whiteTime: CountdownTimer.Remainder,
        override val blackTime: CountdownTimer.Remainder,
        val loser: Side,
    ) : ClockUiState()

    companion object {
        const val DEFAULT_MINUTES = 5
        const val DEFAULT_INCREMENT = 1
        val AVAILABLE_MINUTES: List<Int> = listOf(1, 3, 5, 10, 15, 30, 60, 95)
        val AVAILABLE_INCREMENTS: List<Int> = listOf(0, 1, 3, 5, 10, 15, 30, 60)
    }
}
