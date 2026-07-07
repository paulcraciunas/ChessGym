package com.paulcraciunas.screens.boardvis.pieces.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.RemainingTime

@Immutable
sealed class KnightPathUiState {
    abstract val boardState: BoardState
    abstract val timeRemaining: RemainingTime

    @Immutable
    data object Setup : KnightPathUiState() {
        override val boardState: BoardState = BoardState.empty.copy(boardData = BoardViewData.singleKnight())
        override val timeRemaining: RemainingTime = RemainingTime(value = "$DEFAULT_DURATION_SECONDS.0", danger = false)
    }

    @Immutable
    data class Playing(
        override val boardState: BoardState,
        override val timeRemaining: RemainingTime,
        val showAbandonDialog: Boolean,
        val score: Int,
    ) : KnightPathUiState()

    @Immutable
    data class GameOver(
        override val boardState: BoardState,
        override val timeRemaining: RemainingTime,
        val score: Int,
        val isNewHighScore: Boolean,
        val previousHighScore: Int,
        val wasWrongMove: Boolean,
    ) : KnightPathUiState()

    companion object {
        const val DEFAULT_DURATION_SECONDS = 30
    }
}
