package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.data.PlayableData
import com.paulcraciunas.screens.data.Promotion

@Immutable
sealed class PuzzleStreakUiState {
    @Immutable
    data object Loading : PuzzleStreakUiState()
    @Immutable
    data object Failed : PuzzleStreakUiState()

    @Immutable
    abstract class BoardState : PuzzleStreakUiState() {
        abstract val data: PlayableData
    }

    @Immutable
    data class Playing(
        override val data: PlayableData,
        val streakCount: Int,
        val hintEnabled: Boolean = true,
        val showAbandonDialog: Boolean = false,
        val isShowingSolution: Boolean = false,
        val isAwaitingNextPuzzle: Boolean = false,
        val isAnimating: Boolean = false,
        val promotion: Promotion?,
    ) : BoardState()

    @Immutable
    data class StreakEnded(
        override val data: PlayableData,
        val finalStreakCount: Int,
        val isNewHighScore: Boolean,
        val showSummary: Boolean,
    ) : BoardState()
}
