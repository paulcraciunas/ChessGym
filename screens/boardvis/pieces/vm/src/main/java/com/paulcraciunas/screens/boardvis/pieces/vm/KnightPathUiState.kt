package com.paulcraciunas.screens.boardvis.pieces.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.RemainingTime

@Immutable
sealed class KnightPathUiState {
    abstract val boardData: BoardViewData
    abstract val timeRemaining: RemainingTime

    @Immutable
    data object Setup : KnightPathUiState() {
        override val boardData: BoardViewData = BoardViewData.default()
        override val timeRemaining: RemainingTime = RemainingTime(value = "$DEFAULT_DURATION_SECONDS.0", danger = false)
    }

    @Immutable
    data class Playing(
        override val boardData: BoardViewData,
        override val timeRemaining: RemainingTime,
        val destination: Locus,
        val score: Int,
    ) : KnightPathUiState()

    @Immutable
    data class GameOver(
        override val boardData: BoardViewData,
        override val timeRemaining: RemainingTime,
        val score: Int,
        val isNewHighScore: Boolean,
        val wasWrongMove: Boolean,
    ) : KnightPathUiState()

    companion object {
        const val DEFAULT_DURATION_SECONDS = 30
        const val DANGER_DURATION_SECONDS = 5
    }
}
