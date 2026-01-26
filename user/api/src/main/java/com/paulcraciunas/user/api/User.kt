package com.paulcraciunas.user.api

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class User(
    val profile: Profile = Profile(),
    val ratings: Ratings = Ratings(),
    val highScores: HighScores = HighScores(),
    val statistics: Statistics = Statistics(),
    val history: List<HistoryItem> = emptyList(),
    val failedPuzzles: List<Int> = emptyList(), // Puzzle IDs for retry; This is not persistable across Network, as DB IDs might differ
    val authentication: AuthenticationState? = null,
    val puzzleStreak: PuzzleStreak = PuzzleStreak(),
) {
    fun isSignedIn(): Boolean = authentication != null

    @Serializable
    data class Profile(
        val firstName: String = "Chess",
        val lastName: String = "Enthusiast",
        @Serializable(with = LocalDateSerializer::class)
        val joinDate: LocalDate = LocalDate.now(),
        val avatarUrl: String? = null
    )

    @Serializable
    data class Ratings(
        val current: Int = 1200,
        val blindMode: Int = 400,
    )

    @Serializable
    data class HighScores(
        val ratedPuzzle: Int = 1200,
        val puzzleRush: Int = 0,
        val puzzleStreak: Int = 0,
        val boardVisualization: Int = 0,
        val findTheSquare: Int = 0,
        val blindMode: Int = 400
    )

    @Serializable
    data class Statistics(
        val puzzlesPlayed: Int = 0,
        val puzzlesSolved: Int = 0,
        val totalTimeSpent: Long = 0, // in milliseconds
        val streaks: Streaks = Streaks()
    )

    @Serializable
    data class Streaks(
        val current: Int = 0,
        val longest: Int = 0,
        @Serializable(with = LocalDateSerializer::class)
        val lastActivityDate: LocalDate? = null
    )

    @Serializable
    data class HistoryItem(
        @Serializable(with = LocalDateSerializer::class)
        val timestamp: LocalDate,
        val data: HistoryItemData
    ) {
        @Serializable
        sealed class HistoryItemData {
            @Serializable
            data class PuzzleRushData(
                val tries: Int,
                val bestScore: Int,
                val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class BoardVisualizationData(
                val sessionsCompleted: Int,
                val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class BlindModeTrainingData(
                val tries: Int,
                val mostMovesCompleted: Int,
                val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class BlindModeData(
                val played: Int,
                val ratingChange: Int,
                val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class RatedPuzzleData(
                val puzzlesPlayed: Int,
                val puzzlesSolved: Int,
                val ratingChange: Int,
                val timeSpent: Long
            ) : HistoryItemData()
        }
    }

    @Serializable
    data class AuthenticationState(
        val provider: AuthProvider = AuthProvider.NONE,
        val userId: String,
    ) {
        @Serializable
        enum class AuthProvider {
            NONE,
            GOOGLE,
            INSTAGRAM,
            APPLE,
            FACEBOOK,
        }
    }

    @Serializable
    data class PuzzleStreak(
        val currentCount: Int = 0,
        val lastPuzzleId: Int? = null,
    )
}
