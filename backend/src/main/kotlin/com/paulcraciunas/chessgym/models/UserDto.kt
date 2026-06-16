package com.paulcraciunas.chessgym.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val deviceId: String = "",
    val profile: ProfileDto = ProfileDto(),
    val ratings: RatingsDto = RatingsDto(),
    val highScores: HighScoresDto = HighScoresDto(),
    val statistics: StatisticsDto = StatisticsDto(),
    val achievements: AchievementsDto = AchievementsDto(),
)

@Serializable
data class ProfileDto(
    val displayName: String = "",
    val joinDate: String? = null,
    val isSupporter: Boolean = false,
    val lastModified: Long = 0L,
)

@Serializable
data class RatingsDto(
    val current: Int = DEFAULT_RATED_PUZZLE_RATING,
    val blindMode: Int = DEFAULT_BLIND_MODE_RATING,
) {
    companion object {
        const val DEFAULT_RATED_PUZZLE_RATING: Int = 1000
        const val DEFAULT_BLIND_MODE_RATING: Int = 400
    }
}

@Serializable
data class HighScoresDto(
    val ratedPuzzle: Int = RatingsDto.DEFAULT_RATED_PUZZLE_RATING,
    val puzzleRush: Int = 0,
    val puzzleStreak: Int = 0,
    val findTheSquare: Int = 0,
    val knightPath: Int = 0,
    val blindMode: Int = RatingsDto.DEFAULT_BLIND_MODE_RATING,
)

@Serializable
data class StatisticsDto(
    val puzzlesPlayed: Int = 0,
    val puzzlesSolved: Int = 0,
    val totalTimeSpent: Long = 0L,
    val ratedPuzzlesSolved: Int = 0,
    val puzzleRushSessions: Int = 0,
    val streakSessions: Int = 0,
    val failedPuzzlesRedeemed: Int = 0,
    val findSquareSessions: Int = 0,
    val knightPathSessions: Int = 0,
    val blindModeWins: Int = 0,
    val rushPuzzlesSolved: Int = 0,
)

@Serializable
data class AchievementsDto(
    val progress: Map<String, Long> = emptyMap(),
    val lastActiveDate: String? = null,
    val consecutiveDaysStreak: Int = 0,
    val bestConsecutiveDaysStreak: Int = 0,
    val currentRatedWinStreak: Int = 0,
    val bestRatedWinStreak: Int = 0,
)
