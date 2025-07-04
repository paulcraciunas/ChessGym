package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.settings.user.UserStats
import javax.inject.Inject

class HomeUiStateAdapter @Inject constructor() {
    fun adapt(userStats: UserStats) = HomeUiState.Stats(
        puzzlesPlayed = userStats.puzzlesPlayed,
        puzzlesSolved = userStats.puzzlesSolved,
        currentRating = userStats.currentRating,
        bestRating = userStats.bestRating,
        bestPuzzleRushScore = userStats.bestPuzzleRushScore,
        bestBlindModeScore = userStats.bestBlindModeScore,
        bestVisualizationScore = userStats.bestVisualizationScore,
    )
}
