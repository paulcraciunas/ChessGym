package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.domain.api.achievements.Achievement
import java.time.LocalDate

data class HomeUiState(
    val userProfile: UserProfile = UserProfile(),
    val userStats: Stats = Stats(),
    val history: List<HistoryGroup> = emptyList(),
    val unseenAchievementCount: Int = 0,
    val ribbons: List<Ribbon> = emptyList(),
    val isLoading: Boolean = true,
) {
    data class Ribbon(
        val achievement: Achievement,
        val tier: Achievement.Tier,
    )

    data class UserProfile(
        val name: String = "ChessPlayer",
        val currentRating: Int = 1200,
        val totalActivities: Int = 0,
        val joinDate: LocalDate = LocalDate.now(),
    ) {
        fun initial(): String = name.first().uppercase()
    }

    data class Stats(
        val puzzlesPlayed: Int = 0,
        val puzzlesSolved: Int = 0,
        val currentRating: Int = 1200,
        val bestRating: Int = 1200,
        val bestPuzzleRushScore: Int = 0,
        val bestPuzzleStreakScore: Int = 0,
        val bestFindTheSquareScore: Int = 0,
        val bestMoveThePieceScore: Int = 0,
        val bestBlindModeScore: Int = 0,
    )

    data class HistoryGroup(
        val label: String,
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
            val ratingChange: Int,
            val gamesPlayed: Int,
        ) : HistoryEvent()

        data class BlindModeTrainingEvent(
            val mostMovesCompleted: Int,
            val runs: Int,
        ) : HistoryEvent()

        data class PuzzleStreakEvent(
            val finalStreakCount: Int,
        ) : HistoryEvent()

        data class FailedPuzzleEvent(
            val puzzlesSolved: Int,
        ) : HistoryEvent()
    }
}
