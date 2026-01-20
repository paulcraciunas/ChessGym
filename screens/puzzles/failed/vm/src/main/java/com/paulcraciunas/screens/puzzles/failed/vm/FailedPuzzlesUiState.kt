package com.paulcraciunas.screens.puzzles.failed.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.PuzzleResult

sealed class FailedPuzzlesUiState {
    data object Loading : FailedPuzzlesUiState()
    data object Failed : FailedPuzzlesUiState()
    data object Empty : FailedPuzzlesUiState()

    abstract class BoardState : FailedPuzzlesUiState() {
        abstract val data: PuzzleData
        abstract val progress: Progress
        abstract val results: List<PuzzleResult>
    }

    data class Playing(
        override val data: PuzzleData,
        override val progress: Progress,
        override val results: List<PuzzleResult>,
        val promotion: Promotion?,
    ) : BoardState() {

        data class Promotion(
            val showChooser: Boolean,
            val at: Locus,
        )
    }

    data class Finished(
        override val data: PuzzleData,
        override val progress: Progress,
        override val results: List<PuzzleResult>,
        val showCompletionDialog: Boolean,
    ) : BoardState()

    data class PuzzleData(
        val rating: Int,
        val player: Side,
        val boardData: BoardViewData,
    )

    data class Progress(
        val solved: Int,
        val total: Int,
    )
}
