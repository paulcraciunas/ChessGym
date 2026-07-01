package com.paulcraciunas.chessgym.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("deviceId") val deviceId: String = "",
    @SerialName("profile") val profile: ProfileDto = ProfileDto(),
    @SerialName("ratings") val ratings: RatingsDto = RatingsDto(),
    @SerialName("highScores") val highScores: HighScoresDto = HighScoresDto(),
    @SerialName("statistics") val statistics: StatisticsDto = StatisticsDto(),
    @SerialName("achievements") val achievements: AchievementsDto = AchievementsDto(),
)

@Serializable
data class ProfileDto(
    @SerialName("displayName") val displayName: String = "",
    @SerialName("joinDate") val joinDate: String? = null,
    @SerialName("isSupporter") val isSupporter: Boolean = false,
    @SerialName("lastModified") val lastModified: Long = 0L,
)

@Serializable
data class RatingsDto(
    @SerialName("current") val current: Int = DEFAULT_RATED_PUZZLE_RATING,
    @SerialName("blindMode") val blindMode: Int = DEFAULT_BLIND_MODE_RATING,
) {
    companion object {
        const val DEFAULT_RATED_PUZZLE_RATING: Int = 1000
        const val DEFAULT_BLIND_MODE_RATING: Int = 400
    }
}

@Serializable
data class HighScoresDto(
    @SerialName("ratedPuzzle") val ratedPuzzle: Int = RatingsDto.DEFAULT_RATED_PUZZLE_RATING,
    @SerialName("puzzleRush") val puzzleRush: Int = 0,
    @SerialName("puzzleStreak") val puzzleStreak: Int = 0,
    @SerialName("findTheSquare") val findTheSquare: Int = 0,
    @SerialName("knightPath") val knightPath: Int = 0,
    @SerialName("blindMode") val blindMode: Int = RatingsDto.DEFAULT_BLIND_MODE_RATING,
)

@Serializable
data class StatisticsDto(
    @SerialName("puzzlesPlayed") val puzzlesPlayed: Int = 0,
    @SerialName("puzzlesSolved") val puzzlesSolved: Int = 0,
    @SerialName("totalTimeSpent") val totalTimeSpent: Long = 0L,
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
data class AchievementsDto(
    @SerialName("progress") val progress: Map<String, Long> = emptyMap(),
    @SerialName("lastActiveDate") val lastActiveDate: String? = null,
    @SerialName("consecutiveDaysStreak") val consecutiveDaysStreak: Int = 0,
    @SerialName("bestConsecutiveDaysStreak") val bestConsecutiveDaysStreak: Int = 0,
    @SerialName("currentRatedWinStreak") val currentRatedWinStreak: Int = 0,
    @SerialName("bestRatedWinStreak") val bestRatedWinStreak: Int = 0,
)
