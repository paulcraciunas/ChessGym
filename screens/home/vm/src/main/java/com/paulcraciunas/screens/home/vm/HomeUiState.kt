package com.paulcraciunas.screens.home.vm

import java.time.LocalDate

data class HomeUiState(
    val userProfile: UserProfile = UserProfile(),
    val userStats: Stats = Stats(),
    val history: List<HistoryGroup> = emptyList(),
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

    data class HistoryGroup(
        val date: LocalDate,
        val events: List<HistoryEvent>
    )

    sealed class HistoryEvent {
        data class PuzzleRushEvent(
            val highScore: Int,
            val runs: Int,
        ) : HistoryEvent()

        data class BoardVizEvent(
            val runs: Int,
        ) : HistoryEvent()

        data class RatedPuzzleEvent(
            val ratingChange: Int,
            val count: Int,
        ) : HistoryEvent()

        data class BlindModeEvent(
            val completedMoves: Int,
            val runs: Int,
        ) : HistoryEvent()
    }
}
