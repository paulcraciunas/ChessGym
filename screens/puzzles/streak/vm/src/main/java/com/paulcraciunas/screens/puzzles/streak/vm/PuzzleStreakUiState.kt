package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.BoardState

@Immutable
sealed class PuzzleStreakUiState {
    @Immutable
    data object Loading : PuzzleStreakUiState()
    @Immutable
    data object Failed : PuzzleStreakUiState()

    @Immutable
    abstract class WithBoard : PuzzleStreakUiState() {
        abstract val data: BoardState
        abstract val streakCount: Int
    }

    @Immutable
    data class Playing(
        override val data: BoardState,
        override val streakCount: Int,
        val hintEnabled: Boolean = true,
        val showAbandonDialog: Boolean = false,
        val isAwaitingNextPuzzle: Boolean = false,
    ) : WithBoard()

    @Immutable
    data class StreakEnded(
        override val data: BoardState,
        override val streakCount: Int,
        val isNewHighScore: Boolean,
        val showSummary: Boolean,
    ) : WithBoard()
}
