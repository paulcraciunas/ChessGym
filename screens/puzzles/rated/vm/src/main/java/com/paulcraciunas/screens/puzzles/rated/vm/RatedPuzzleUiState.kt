package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState

@Immutable
sealed class RatedPuzzleUiState {
    data object Loading : RatedPuzzleUiState()
    data object Failed : RatedPuzzleUiState()
    @Immutable
    abstract class WithBoard : RatedPuzzleUiState() {
        abstract val rating: Int
        abstract val data: BoardState
    }

    @Immutable
    data class Playing(
        override val rating: Int,
        override val data: BoardState,
        val hintEnabled: Boolean,
        val showAbandonDialog: Boolean,
        val isShowingSolution: Boolean = false,
    ) : WithBoard()

    @Immutable
    data class Finished(
        override val rating: Int,
        override val data: BoardState,
        val success: Boolean,
        val ratingChange: Int
    ) : WithBoard()
}
