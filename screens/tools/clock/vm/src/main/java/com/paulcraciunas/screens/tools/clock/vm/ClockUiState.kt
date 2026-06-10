package com.paulcraciunas.screens.tools.clock.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side

@Immutable
sealed class ClockUiState {
    abstract val whiteTime: String
    abstract val blackTime: String

    @Immutable
    data class Setup(
        override val whiteTime: String,
        override val blackTime: String,
        val selectedMinutes: Int,
        val selectedIncrement: Int,
    ) : ClockUiState()

    @Immutable
    data class Playing(
        override val whiteTime: String,
        override val blackTime: String,
        val activePlayer: Side,
    ) : ClockUiState()

    @Immutable
    data class Finished(
        override val whiteTime: String,
        override val blackTime: String,
        val loser: Side,
    ) : ClockUiState()

    companion object {
        val AVAILABLE_MINUTES: List<Int> = listOf(1, 3, 5, 10, 15, 30, 60, 95)
        val AVAILABLE_INCREMENTS: List<Int> = listOf(0, 1, 3, 5, 10, 15, 30, 60)
    }
}
