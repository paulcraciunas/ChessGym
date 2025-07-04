package com.paulcraciunas.screens.home.vm

import java.time.LocalDate
import java.time.LocalDateTime

data class HomeUiState(
    val userProfile: UserProfile = UserProfile(),
    val userStats: Stats = Stats(),
    val activityHistory: List<ActivityGroup> = emptyList(),
    val isLoading: Boolean = true,
) {
    data class UserProfile(
        val name: String = "Chess Player",
        val currentRating: Int = 1200,
        val totalActivities: Int = 0,
        val joinDate: LocalDate = LocalDate.now(),
    )

    data class Stats(
        val puzzlesPlayed: Int = 0,
        val puzzlesSolved: Int = 0,
        val currentRating: Int = 1200,
        val bestRating: Int = 1200,
        val bestPuzzleRushScore: Int = 0,
        val bestBlindModeScore: Int = 0,
        val bestVisualizationScore: Int = 0,
    )

    data class ActivityGroup(
        val date: LocalDate,
        val activities: List<ActivityEvent>
    )

    data class ActivityEvent(
        val id: String,
        val type: ActivityType,
        val title: String,
        val description: String,
        val timestamp: LocalDateTime,
        val score: Int? = null,
        val count: Int? = null,
    )

    enum class ActivityType {
        PUZZLE_RUSH,
        BOARD_VISUALIZATION,
        BLIND_MODE,
        RATED_PUZZLE,
        TRAINING_SESSION
    }
}
