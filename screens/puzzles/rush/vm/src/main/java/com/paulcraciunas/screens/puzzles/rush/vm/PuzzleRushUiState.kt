package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.PuzzleResult

@Immutable
sealed class PuzzleRushUiState {
    data object Loading : PuzzleRushUiState()
    data object Failed : PuzzleRushUiState()

    @Immutable
    abstract class WithBoard : PuzzleRushUiState() {
        abstract val data: BoardState
        abstract val timeRemainingSeconds: Int
        abstract val results: List<PuzzleResult>
    }

    @Immutable
    data class Ready(
        override val data: BoardState,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult> = emptyList(),
    ) : WithBoard()

    @Immutable
    data class Playing(
        override val data: BoardState,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
    ) : WithBoard()

    @Immutable
    data class Finished(
        override val data: BoardState,
        override val timeRemainingSeconds: Int,
        override val results: List<PuzzleResult>,
        val showSummaryDialog: Boolean,
        val isNewHighScore: Boolean,
    ) : WithBoard()
}
