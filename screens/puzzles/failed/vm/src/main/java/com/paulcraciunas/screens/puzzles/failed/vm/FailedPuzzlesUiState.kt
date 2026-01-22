package com.paulcraciunas.screens.puzzles.failed.vm

import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper

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
        val promotion: PuzzleViewModelHelper.Promotion?,
    ) : BoardState()

    data class Finished(
        override val data: PuzzleData,
        override val progress: Progress,
        override val results: List<PuzzleResult>,
        val showCompletionDialog: Boolean,
    ) : BoardState()

    data class Progress(
        val solved: Int,
        val total: Int,
    )
}
