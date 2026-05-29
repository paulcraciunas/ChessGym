package com.paulcraciunas.screens.puzzles.failed.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.model.PlayableData
import com.paulcraciunas.screens.common.model.Promotion

@Immutable
sealed class FailedPuzzlesUiState {
    data object Loading : FailedPuzzlesUiState()
    data object Failed : FailedPuzzlesUiState()
    data object Empty : FailedPuzzlesUiState()

    @Immutable
    abstract class BoardState : FailedPuzzlesUiState() {
        abstract val data: PlayableData
        abstract val progress: Progress
        abstract val results: List<PuzzleResult>
    }

    @Immutable
    data class Playing(
        override val data: PlayableData,
        override val progress: Progress,
        override val results: List<PuzzleResult>,
        val isAnimating: Boolean = false,
        val promotion: Promotion?,
    ) : BoardState()

    @Immutable
    data class Finished(
        override val data: PlayableData,
        override val progress: Progress,
        override val results: List<PuzzleResult>,
        val showCompletionDialog: Boolean,
    ) : BoardState()

    data class Progress(
        val solved: Int,
        val total: Int,
    )
}
