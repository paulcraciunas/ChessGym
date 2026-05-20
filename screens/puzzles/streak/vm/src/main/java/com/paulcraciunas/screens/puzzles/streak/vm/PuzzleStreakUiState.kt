package com.paulcraciunas.screens.puzzles.streak.vm

import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleViewModelHelper

sealed class PuzzleStreakUiState {
    data object Loading : PuzzleStreakUiState()
    data object Failed : PuzzleStreakUiState()

    abstract class BoardState : PuzzleStreakUiState() {
        abstract val data: PuzzleData
    }

    data class Playing(
        override val data: PuzzleData,
        val streakCount: Int,
        val hintEnabled: Boolean = true,
        val showAbandonDialog: Boolean = false,
        val isShowingSolution: Boolean = false,
        val isAwaitingNextPuzzle: Boolean = false,
        val isAnimating: Boolean = false,
        val promotion: PuzzleViewModelHelper.Promotion?,
    ) : BoardState()

    data class StreakEnded(
        override val data: PuzzleData,
        val finalStreakCount: Int,
        val isNewHighScore: Boolean,
        val showSummary: Boolean,
    ) : BoardState()
}
