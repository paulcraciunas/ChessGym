package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.model.v2.PuzzleData2
import com.paulcraciunas.screens.common.model.v2.PuzzleViewModelHelper2

@Immutable
sealed class PuzzleStreakUiState2 {
    @Immutable
    data object Loading : PuzzleStreakUiState2()
    @Immutable
    data object Failed : PuzzleStreakUiState2()

    @Immutable
    abstract class BoardState : PuzzleStreakUiState2() {
        abstract val data: PuzzleData2
    }

    @Immutable
    data class Playing(
        override val data: PuzzleData2,
        val streakCount: Int,
        val hintEnabled: Boolean = true,
        val showAbandonDialog: Boolean = false,
        val isShowingSolution: Boolean = false,
        val isAwaitingNextPuzzle: Boolean = false,
        val isAnimating: Boolean = false,
        val promotion: PuzzleViewModelHelper2.Promotion2?,
    ) : BoardState()

    @Immutable
    data class StreakEnded(
        override val data: PuzzleData2,
        val finalStreakCount: Int,
        val isNewHighScore: Boolean,
        val showSummary: Boolean,
    ) : BoardState()
}
