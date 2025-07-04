package com.paulcraciunas.screens.home.vm

data class HomeUiState(
    val userStats: Stats = Stats(
        puzzlesPlayed = 0,
        puzzlesSolved = 0,
        currentRating = 1200,
        bestRating = 1200,
        bestPuzzleRushScore = 0,
        bestBlindModeScore = 0,
        bestVisualizationScore = 0
    ),
    val isLoading: Boolean = true,
) {
    data class Stats(
        val puzzlesPlayed: Int,
        val puzzlesSolved: Int,
        val currentRating: Int,
        val bestRating: Int,
        val bestPuzzleRushScore: Int,
        val bestBlindModeScore: Int,
        val bestVisualizationScore: Int,
    )
}
