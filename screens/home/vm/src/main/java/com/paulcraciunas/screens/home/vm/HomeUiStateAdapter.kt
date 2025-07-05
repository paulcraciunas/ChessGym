package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.settings.user.UserStats
import java.time.LocalDate
import javax.inject.Inject

class HomeUiStateAdapter @Inject constructor() {
    fun adapt(userStats: UserStats) = HomeUiState(
        userProfile = HomeUiState.UserProfile(
            name = "Chess Player", // TODO: Get from user preferences
            currentRating = userStats.currentRating,
            totalActivities = userStats.puzzlesPlayed,
            joinDate = LocalDate.now(), // TODO: Get from user preferences
        ),
        userStats = HomeUiState.Stats(
            puzzlesPlayed = userStats.puzzlesPlayed,
            puzzlesSolved = userStats.puzzlesSolved,
            currentRating = userStats.currentRating,
            bestRating = userStats.bestRating,
            bestPuzzleRushScore = userStats.bestPuzzleRushScore,
            bestBlindModeScore = userStats.bestBlindModeScore,
            bestVisualizationScore = userStats.bestVisualizationScore,
        ),
        history = generateSampleHistory(), // TODO: Get from database
        isLoading = false
    )

    // TODO: Replace with actual data from database
    private fun generateSampleHistory(): List<HomeUiState.HistoryGroup> {
        val today = LocalDate.now()
        return listOf(
            HomeUiState.HistoryGroup(
                date = today,
                events = listOf(
                    HomeUiState.HistoryEvent.PuzzleRushEvent(highScore = 18, runs = 5),
                    HomeUiState.HistoryEvent.BoardVizEvent(runs = 2)
                )
            ),
            HomeUiState.HistoryGroup(
                date = today.minusDays(1),
                events = listOf(
                    HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = 42, count = 12),
                    HomeUiState.HistoryEvent.BlindModeEvent(completedMoves = 8, runs = 3)
                )
            )
        )
    }
}
