package com.paulcraciunas.screens.puzzles.rush.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.RemainingTime
import com.paulcraciunas.screens.data.SessionResult

@Immutable
sealed class PuzzleRushUiState {
    abstract val time: RemainingTime

    data object Loading : PuzzleRushUiState() {
        override val time: RemainingTime = RemainingTime()
    }

    data object Failed : PuzzleRushUiState() {
        override val time: RemainingTime = RemainingTime()
    }

    @Immutable
    abstract class WithBoard : PuzzleRushUiState() {
        abstract val data: BoardState
        abstract val results: List<SessionResult>
    }

    @Immutable
    data class Ready(
        override val time: RemainingTime = RemainingTime(),
        override val data: BoardState,
        override val results: List<SessionResult> = emptyList(),
    ) : WithBoard()

    @Immutable
    data class Playing(
        override val data: BoardState,
        override val time: RemainingTime,
        override val results: List<SessionResult>,
        val showAbandonDialog: Boolean,
    ) : WithBoard()

    @Immutable
    data class Finished(
        override val data: BoardState,
        override val time: RemainingTime,
        override val results: List<SessionResult>,
        val showSummaryDialog: Boolean,
        val isNewHighScore: Boolean,
    ) : WithBoard()
}
