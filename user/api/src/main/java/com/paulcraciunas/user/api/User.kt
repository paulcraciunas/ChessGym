package com.paulcraciunas.user.api

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class User(
    val profile: Profile = Profile(),
    val ratings: Ratings = Ratings(),
    val highScores: HighScores = HighScores(),
    val statistics: Statistics = Statistics(),
    val achievements: Achievements = Achievements(),
    val history: List<HistoryItem> = emptyList(),
    val failedPuzzles: List<Int> = emptyList(), // Puzzle IDs for retry; This is not persistable across Network, as DB IDs might differ
    val authentication: AuthenticationState? = null,
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
        val puzzleStreak: PuzzleStreak = PuzzleStreak(),
    )

    @Serializable
    data class HighScores(
        val ratedPuzzle: Int = 1200,
        val puzzleRush: Int = 0,
        val puzzleStreak: Int = 0,
        val findTheSquare: Int = 0,
        val moveThePiece: Int = 0,
        val blindMode: Int = 400
    )

    @Serializable
    data class Statistics(
        val puzzlesPlayed: Int = 0,
        val puzzlesSolved: Int = 0,
        val totalTimeSpent: Long = 0, // in milliseconds
        val ratedPuzzlesSolved: Int = 0,
        val puzzleRushSessions: Int = 0,
        val streakSessions: Int = 0,
        val failedPuzzlesRedeemed: Int = 0,
        val findSquareSessions: Int = 0,
        val moveThePieceSessions: Int = 0,
        val blindModeWins: Int = 0,
        val rushPuzzlesSolved: Int = 0,
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

            @Serializable
            data class PuzzleStreakData(
                val finalStreakCount: Int,
                val timeSpent: Long,
            ) : HistoryItemData()

            @Serializable
            data class FailedPuzzleData(
                val puzzlesSolved: Int,
                val timeSpent: Long,
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

    @Serializable
    data class Achievements(
        val progress: Map<String, Long> = emptyMap(),
        val unseenAchievements: Set<String> = emptySet(),
        @Serializable(with = LocalDateSerializer::class)
        val lastActiveDate: LocalDate? = null,
        val consecutiveDaysStreak: Int = 0,
        val bestConsecutiveDaysStreak: Int = 0,
        val currentRatedWinStreak: Int = 0,
        val bestRatedWinStreak: Int = 0,
    )
}
