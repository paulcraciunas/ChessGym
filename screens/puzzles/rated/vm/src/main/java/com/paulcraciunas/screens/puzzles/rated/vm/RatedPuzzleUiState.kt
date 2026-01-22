package com.paulcraciunas.screens.puzzles.rated.vm

import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper

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
        val promotion: PuzzleViewModelHelper.Promotion?,
    ) : BoardState()

    data class Finished(
        override val data: PuzzleData,
        val success: Boolean,
        val ratingChange: Int
    ) : BoardState()
}
