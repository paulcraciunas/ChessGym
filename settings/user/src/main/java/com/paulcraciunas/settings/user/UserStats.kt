package com.paulcraciunas.settings.user

data class UserStats(
    val puzzlesPlayed: Int,
    val puzzlesSolved: Int,
    val currentRating: Int,
    val bestRating: Int,
    val bestPuzzleRushScore: Int,
    val bestBlindModeScore: Int,
    val bestVisualizationScore: Int,
)
