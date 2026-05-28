package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper

@Immutable
sealed class RatedPuzzleUiState {
    data object Loading : RatedPuzzleUiState()
    data object Failed : RatedPuzzleUiState()
    @Immutable
    abstract class BoardState : RatedPuzzleUiState() {
        abstract val data: PuzzleData
    }

    @Immutable
    data class Playing(
        override val data: PuzzleData,
        val hintEnabled: Boolean,
        val showAbandonDialog: Boolean,
        val isShowingSolution: Boolean = false,
        val promotion: PuzzleViewModelHelper.Promotion2?,
    ) : BoardState()

    @Immutable
    data class Finished(
        override val data: PuzzleData,
        val success: Boolean,
        val ratingChange: Int
    ) : BoardState()
}
