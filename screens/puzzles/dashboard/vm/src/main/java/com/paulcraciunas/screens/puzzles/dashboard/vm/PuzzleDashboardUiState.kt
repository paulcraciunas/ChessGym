package com.paulcraciunas.screens.puzzles.dashboard.vm

data class PuzzleDashboardUiState(
    val userRating: Int = 1200,
    val failedPuzzlesCount: Int = 0,
    val puzzleRushConfig: PuzzleRushConfig = PuzzleRushConfig(),
    val isLoading: Boolean = false
) {
    data class PuzzleRushConfig(
        val timeLimit: TimeLimit = TimeLimit.THREE_MINUTES,
        val mistakesAllowed: MistakesAllowed = MistakesAllowed.TWO_MISTAKES
    ) {
        enum class TimeLimit(val minutes: Int) {
            THREE_MINUTES(minutes = 3),
            FIVE_MINUTES(minutes = 5)
        }

        enum class MistakesAllowed(val count: Int) {
            ZERO_MISTAKES(count = 0),
            TWO_MISTAKES(count = 2)
        }
    }
}

sealed class PuzzleMode {
    data object RatedPuzzle : PuzzleMode()
    data class PuzzleRush(val config: PuzzleDashboardUiState.PuzzleRushConfig) : PuzzleMode()
    data object FailedPuzzles : PuzzleMode()
}
