package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData

sealed class RatedPuzzleUiState {
    data object Loading : RatedPuzzleUiState()
    data object Failed : RatedPuzzleUiState()
    abstract class BoardState : RatedPuzzleUiState() {
        abstract val data: PuzzleData
    }

    data class Playing(
        override val data: PuzzleData,
        val hintEnabled: Boolean,
        val showAbandonDialog: Boolean,
        val promotion: Promotion?,
    ) : BoardState() {

        data class Promotion(
            val showChooser: Boolean,
            val at: Locus,
        )
    }

    data class Finished(
        override val data: PuzzleData,
        val success: Boolean,
        val ratingChange: Int
    ) : BoardState()

    data class PuzzleData(
        val rating: Int,
        val player: Side,
        val boardData: BoardViewData,
        val captured: Map<Side, List<Piece>>,
    )
}
