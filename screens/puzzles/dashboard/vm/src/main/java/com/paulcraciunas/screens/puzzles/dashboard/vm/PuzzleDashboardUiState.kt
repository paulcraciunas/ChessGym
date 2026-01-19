package com.paulcraciunas.screens.puzzles.dashboard.vm

data class PuzzleDashboardUiState(
    val userRating: Int = 1200,
    val failedPuzzlesCount: Int = 0,
    val isLoading: Boolean = false
)

sealed class PuzzleMode {
    data object RatedPuzzle : PuzzleMode()
    data object PuzzleRush : PuzzleMode()
    data object PuzzleStreak : PuzzleMode()
    data object FailedPuzzles : PuzzleMode()
}
