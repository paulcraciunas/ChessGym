package com.paulcraciunas.user.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val profile: ProfileDto = ProfileDto(),
    val ratings: RatingsDto = RatingsDto(),
    val highScores: HighScoresDto = HighScoresDto(),
    val statistics: StatisticsDto = StatisticsDto(),
    val achievements: AchievementsDto = AchievementsDto(),
)

@Serializable
data class ProfileDto(
    val displayName: String = "ChessEnthusiast",
    val joinDate: String? = null,
    val avatarUrl: String? = null,
    val lastModified: Long = 0L,
)

@Serializable
data class RatingsDto(
    val current: Int = 1000,
    val blindMode: Int = 400,
)

@Serializable
data class HighScoresDto(
    val ratedPuzzle: Int = 1000,
    val puzzleRush: Int = 0,
    val puzzleStreak: Int = 0,
    val findTheSquare: Int = 0,
    val moveThePiece: Int = 0,
    val blindMode: Int = 400,
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
    val moveThePieceSessions: Int = 0,
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
