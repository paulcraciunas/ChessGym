package com.paulcraciunas.screens.puzzles.rated.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.screens.common.model.Promotion

@Immutable
sealed class RatedPuzzleUiState {
    data object Loading : RatedPuzzleUiState()
    data object Failed : RatedPuzzleUiState()
    @Immutable
    abstract class BoardState : RatedPuzzleUiState() {
        abstract val data: PlayableData
    }

    @Immutable
    data class Playing(
        override val data: PlayableData,
        val hintEnabled: Boolean,
        val showAbandonDialog: Boolean,
        val isShowingSolution: Boolean = false,
        val promotion: Promotion?,
    ) : BoardState()

    @Immutable
    data class Finished(
        override val data: PlayableData,
        val success: Boolean,
        val ratingChange: Int
    ) : BoardState()
}
