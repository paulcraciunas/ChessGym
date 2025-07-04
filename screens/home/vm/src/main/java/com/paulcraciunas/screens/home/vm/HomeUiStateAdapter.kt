package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.settings.user.UserStats
import java.time.LocalDate
import java.time.LocalDateTime
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
        activityHistory = generateSampleActivityHistory(), // TODO: Get from database
        isLoading = false
    )

    // TODO: Replace with actual data from database
    private fun generateSampleActivityHistory(): List<HomeUiState.ActivityGroup> {
        val today = LocalDate.now()
        return listOf(
            HomeUiState.ActivityGroup(
                date = today,
                activities = listOf(
                    HomeUiState.ActivityEvent(
                        id = "1",
                        type = HomeUiState.ActivityType.PUZZLE_RUSH,
                        title = "Puzzle Rush",
                        description = "Completed 5 runs with best score 18",
                        timestamp = LocalDateTime.now().minusHours(2),
                        score = 18,
                        count = 5
                    ),
                    HomeUiState.ActivityEvent(
                        id = "2",
                        type = HomeUiState.ActivityType.BOARD_VISUALIZATION,
                        title = "Board Visualization",
                        description = "Completed 2 sessions",
                        timestamp = LocalDateTime.now().minusHours(4),
                        count = 2
                    )
                )
            ),
            HomeUiState.ActivityGroup(
                date = today.minusDays(1),
                activities = listOf(
                    HomeUiState.ActivityEvent(
                        id = "3",
                        type = HomeUiState.ActivityType.RATED_PUZZLE,
                        title = "Rated Puzzles",
                        description = "Solved 12 puzzles, rating improved by 15 points",
                        timestamp = LocalDateTime.now().minusDays(1).minusHours(3),
                        count = 12
                    ),
                    HomeUiState.ActivityEvent(
                        id = "4",
                        type = HomeUiState.ActivityType.BLIND_MODE,
                        title = "Blind Mode",
                        description = "Completed 3 sessions with best score 8",
                        timestamp = LocalDateTime.now().minusDays(1).minusHours(5),
                        score = 8,
                        count = 3
                    )
                )
            )
        )
    }
}
