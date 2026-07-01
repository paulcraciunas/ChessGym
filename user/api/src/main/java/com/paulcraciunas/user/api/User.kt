package com.paulcraciunas.user.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class User(
    @SerialName("deviceId") val deviceId: String = "",
    @SerialName("profile") val profile: Profile = Profile(),
    @SerialName("ratings") val ratings: Ratings = Ratings(),
    @SerialName("highScores") val highScores: HighScores = HighScores(),
    @SerialName("statistics") val statistics: Statistics = Statistics(),
    @SerialName("achievements") val achievements: Achievements = Achievements(),
    @SerialName("history") val history: List<HistoryItem> = emptyList(),
    @SerialName("failedPuzzles") val failedPuzzles: List<Int> = emptyList(), // Puzzle IDs for retry; This is not persistable across Network, as DB IDs might differ
    @SerialName("authentication") val authentication: AuthenticationState? = null,
) {
    fun isSignedIn(): Boolean = authentication != null

    @Serializable
    data class Profile(
        @SerialName("displayName") val displayName: String = "ChessEnthusiast",
        @Serializable(with = LocalDateSerializer::class)
        @SerialName("joinDate") val joinDate: LocalDate = LocalDate.now(),
        @SerialName("isSupporter") val isSupporter: Boolean = false,
    )

    @Serializable
    data class Ratings(
        @SerialName("current") val current: Int = DEFAULT_RATED_PUZZLE_RATING,
        @SerialName("blindMode") val blindMode: Int = DEFAULT_BLIND_MODE_RATING,
        @SerialName("puzzleStreak") val puzzleStreak: PuzzleStreak = PuzzleStreak(),
    )

    @Serializable
    data class HighScores(
        @SerialName("ratedPuzzle") val ratedPuzzle: Int = DEFAULT_RATED_PUZZLE_RATING,
        @SerialName("puzzleRush") val puzzleRush: Int = 0,
        @SerialName("puzzleStreak") val puzzleStreak: Int = 0,
        @SerialName("findTheSquare") val findTheSquare: Int = 0,
        @SerialName("knightPath") val knightPath: Int = 0,
        @SerialName("blindMode") val blindMode: Int = DEFAULT_BLIND_MODE_RATING
    )

    @Serializable
    data class Statistics(
        @SerialName("puzzlesPlayed") val puzzlesPlayed: Int = 0,
        @SerialName("puzzlesSolved") val puzzlesSolved: Int = 0,
        @SerialName("totalTimeSpent") val totalTimeSpent: Long = 0, // in milliseconds
        @SerialName("ratedPuzzlesSolved") val ratedPuzzlesSolved: Int = 0,
        @SerialName("puzzleRushSessions") val puzzleRushSessions: Int = 0,
        @SerialName("streakSessions") val streakSessions: Int = 0,
        @SerialName("failedPuzzlesRedeemed") val failedPuzzlesRedeemed: Int = 0,
        @SerialName("findSquareSessions") val findSquareSessions: Int = 0,
        @SerialName("knightPathSessions") val knightPathSessions: Int = 0,
        @SerialName("blindModeWins") val blindModeWins: Int = 0,
        @SerialName("rushPuzzlesSolved") val rushPuzzlesSolved: Int = 0,
    )

    @Serializable
    data class HistoryItem(
        @Serializable(with = LocalDateSerializer::class)
        @SerialName("timestamp") val timestamp: LocalDate,
        @SerialName("data") val data: HistoryItemData
    ) {
        @Serializable
        sealed class HistoryItemData {
            @Serializable
            data class PuzzleRushData(
                @SerialName("tries") val tries: Int,
                @SerialName("bestScore") val bestScore: Int,
                @SerialName("timeSpent") val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class BoardVisualizationData(
                @SerialName("sessionsCompleted") val sessionsCompleted: Int,
                @SerialName("timeSpent") val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class BlindModeTrainingData(
                @SerialName("tries") val tries: Int,
                @SerialName("mostMovesCompleted") val mostMovesCompleted: Int,
                @SerialName("timeSpent") val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class BlindModeData(
                @SerialName("played") val played: Int,
                @SerialName("ratingChange") val ratingChange: Int,
                @SerialName("timeSpent") val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class RatedPuzzleData(
                @SerialName("puzzlesPlayed") val puzzlesPlayed: Int,
                @SerialName("puzzlesSolved") val puzzlesSolved: Int,
                @SerialName("ratingChange") val ratingChange: Int,
                @SerialName("timeSpent") val timeSpent: Long
            ) : HistoryItemData()

            @Serializable
            data class PuzzleStreakData(
                @SerialName("finalStreakCount") val finalStreakCount: Int,
                @SerialName("timeSpent") val timeSpent: Long,
            ) : HistoryItemData()

            @Serializable
            data class FailedPuzzleData(
                @SerialName("puzzlesSolved") val puzzlesSolved: Int,
                @SerialName("timeSpent") val timeSpent: Long,
            ) : HistoryItemData()
        }
    }

    @Serializable
    data class AuthenticationState(
        @SerialName("provider") val provider: AuthProvider = AuthProvider.EMAIL,
        @SerialName("userId") val userId: String,
    ) {
        @Serializable
        enum class AuthProvider {
            EMAIL,
            GOOGLE,
        }
    }

    @Serializable
    data class PuzzleStreak(
        @SerialName("currentCount") val currentCount: Int = 0,
        @SerialName("lastPuzzleId") val lastPuzzleId: Int? = null,
    )

    @Serializable
    data class Achievements(
        @SerialName("progress") val progress: Map<String, Long> = mapOf(
            ACHIEVEMENT_RATING_CLIMBER to DEFAULT_RATED_PUZZLE_RATING.toLong(),
            ACHIEVEMENT_BLIND_STRATEGIST to DEFAULT_BLIND_MODE_RATING.toLong(),
        ),
        @SerialName("unseenAchievements") val unseenAchievements: Set<String> = emptySet(),
        @Serializable(with = LocalDateSerializer::class)
        @SerialName("lastActiveDate") val lastActiveDate: LocalDate? = null,
        @SerialName("consecutiveDaysStreak") val consecutiveDaysStreak: Int = 0,
        @SerialName("bestConsecutiveDaysStreak") val bestConsecutiveDaysStreak: Int = 0,
        @SerialName("currentRatedWinStreak") val currentRatedWinStreak: Int = 0,
        @SerialName("bestRatedWinStreak") val bestRatedWinStreak: Int = 0,
    )

    companion object {
        const val DEFAULT_RATED_PUZZLE_RATING: Int = 1000
        const val DEFAULT_BLIND_MODE_RATING: Int = 400

        internal const val ACHIEVEMENT_RATING_CLIMBER: String = "RATING_CLIMBER"
        internal const val ACHIEVEMENT_BLIND_STRATEGIST: String = "BLIND_STRATEGIST"
    }
}
