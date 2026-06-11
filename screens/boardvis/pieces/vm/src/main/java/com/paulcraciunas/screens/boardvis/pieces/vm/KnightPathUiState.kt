package com.paulcraciunas.screens.boardvis.pieces.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.data.BoardViewData

@Immutable
sealed class KnightPathUiState {
    abstract val boardData: BoardViewData

    @Immutable
    data object Setup : KnightPathUiState() {
        override val boardData: BoardViewData = BoardViewData.default()
    }

    @Immutable
    data class Playing(
        override val boardData: BoardViewData,
        val destination: Locus,
        val score: Int,
        val timeRemaining: String,
    ) : KnightPathUiState()

    @Immutable
    data class GameOver(
        override val boardData: BoardViewData,
        val score: Int,
        val isNewHighScore: Boolean,
        val wasWrongMove: Boolean,
    ) : KnightPathUiState()

    companion object {
        const val DEFAULT_DURATION_SECONDS = 30
    }
}
