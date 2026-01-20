package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.PuzzleResult

sealed class PuzzleRushUiState {
    data object Loading : PuzzleRushUiState()
    data object Failed : PuzzleRushUiState()

    abstract class BoardState : PuzzleRushUiState() {
        abstract val data: PuzzleData
        abstract val timeRemainingSeconds: Int
        abstract val results: List<PuzzleResult>
    }

    data class Ready(
        override val data: PuzzleData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult> = emptyList(),
    ) : BoardState()

    data class Playing(
        override val data: PuzzleData,
        override val timeRemainingSeconds: Int,
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
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
        val showSummaryDialog: Boolean,
        val isNewHighScore: Boolean,
    ) : BoardState()

    data class PuzzleData(
        val rating: Int,
        val player: Side,
        val boardData: BoardViewData,
    )
}
