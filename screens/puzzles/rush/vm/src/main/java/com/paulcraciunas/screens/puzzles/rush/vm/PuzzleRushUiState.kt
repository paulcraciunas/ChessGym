package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper

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
        val promotion: PuzzleViewModelHelper.Promotion?,
    ) : BoardState()

    data class Finished(
        override val data: PuzzleData,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
        val showSummaryDialog: Boolean,
        val isNewHighScore: Boolean,
    ) : BoardState()
}
