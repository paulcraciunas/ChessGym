package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData

sealed class PuzzleStreakUiState {
    data object Loading : PuzzleStreakUiState()
    data object Failed : PuzzleStreakUiState()

    abstract class BoardState : PuzzleStreakUiState() {
        abstract val data: PuzzleData
    }

    data class Playing(
        override val data: PuzzleData,
        val streakCount: Int,
        val promotion: Promotion?,
    ) : BoardState() {

        data class Promotion(
            val showChooser: Boolean,
            val at: Locus,
        )
    }

    data class StreakEnded(
        override val data: PuzzleData,
        val finalStreakCount: Int,
        val isNewHighScore: Boolean,
        val showSummary: Boolean,
    ) : BoardState()

    data class PuzzleData(
        val rating: Int,
        val player: Side,
        val boardData: BoardViewData,
        val captured: Map<Side, List<Piece>>,
    )
}
