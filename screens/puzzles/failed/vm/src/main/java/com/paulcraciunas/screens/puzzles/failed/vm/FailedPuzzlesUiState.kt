package com.paulcraciunas.screens.puzzles.failed.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.SessionResult

@Immutable
sealed class FailedPuzzlesUiState {
    data object Loading : FailedPuzzlesUiState()
    data object Failed : FailedPuzzlesUiState()
    data object Empty : FailedPuzzlesUiState()

    @Immutable
    abstract class WithBoard : FailedPuzzlesUiState() {
        abstract val data: BoardState
        abstract val progress: Progress
        abstract val results: List<SessionResult>
    }

    @Immutable
    data class Playing(
        override val data: BoardState,
        override val progress: Progress,
        override val results: List<SessionResult>,
    ) : WithBoard()

    @Immutable
    data class Finished(
        override val data: BoardState,
        override val progress: Progress,
        override val results: List<SessionResult>,
        val showCompletionDialog: Boolean,
    ) : WithBoard()

    data class Progress(
        val solved: Int,
        val total: Int,
    )
}
