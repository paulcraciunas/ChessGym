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
        abstract val controls: Controls
    }

    @Immutable
    data class ReLoad(
        override val data: BoardState,
        override val streakCount: Int,
    ) : WithBoard() {
        override val controls = Controls.None
    }

    @Immutable
    data class Playing(
        override val data: BoardState,
        override val streakCount: Int,
        val hintEnabled: Boolean = true,
        val showAbandonDialog: Boolean = false,
        val isAwaitingNextPuzzle: Boolean = false,
    ) : WithBoard() {
        override val controls = if (isAwaitingNextPuzzle) Controls.WaitingForNext else Controls.Playing
    }

    @Immutable
    data class StreakEnded(
        override val data: BoardState,
        override val streakCount: Int,
        val isNewHighScore: Boolean,
        val showSummary: Boolean,
    ) : WithBoard() {
        override val controls = Controls.Ended
    }

    enum class Controls {
        Playing,
        WaitingForNext,
        Ended,
        None,
    }
}
