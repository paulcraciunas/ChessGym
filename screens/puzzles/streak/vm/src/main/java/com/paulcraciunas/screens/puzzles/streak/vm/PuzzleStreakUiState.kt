package com.paulcraciunas.screens.puzzles.streak.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper

@Immutable
sealed class PuzzleStreakUiState {
    @Immutable
    data object Loading : PuzzleStreakUiState()
    @Immutable
    data object Failed : PuzzleStreakUiState()

    @Immutable
    abstract class BoardState : PuzzleStreakUiState() {
        abstract val data: PuzzleData
    }

    @Immutable
    data class Playing(
        override val data: PuzzleData,
        val streakCount: Int,
        val hintEnabled: Boolean = true,
        val showAbandonDialog: Boolean = false,
        val isShowingSolution: Boolean = false,
        val isAwaitingNextPuzzle: Boolean = false,
        val isAnimating: Boolean = false,
        val promotion: PuzzleViewModelHelper.Promotion2?,
    ) : BoardState()

    @Immutable
    data class StreakEnded(
        override val data: PuzzleData,
        val finalStreakCount: Int,
        val isNewHighScore: Boolean,
        val showSummary: Boolean,
    ) : BoardState()
}
