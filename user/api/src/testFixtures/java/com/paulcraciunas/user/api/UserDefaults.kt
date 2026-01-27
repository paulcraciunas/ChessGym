package com.paulcraciunas.user.api

import java.time.LocalDate

object UserDefaults {
    fun signedInUser(): User = User(
        profile = User.Profile(
            firstName = FIRST_NAME,
            lastName = LAST_NAME,
            joinDate = LocalDate.of(2023, 1, 1),
            avatarUrl = null
        ),
        ratings = User.Ratings(
            current = RATING,
            blindMode = RATING_BLIND_MODE
        ),
        highScores = User.HighScores(
            ratedPuzzle = HIGH_SCORE_RATED,
            puzzleRush = HIGH_SCORE_RUSH,
            puzzleStreak = HIGH_SCORE_STREAK,
            boardVisualization = HIGH_SCORE_BOARD,
            findTheSquare = HIGH_SCORE_FIND_SQUARE,
            moveThePiece = HIGH_SCORE_MOVE_PIECE,
            blindMode = RATING_BLIND_MODE
        ),
        statistics = User.Statistics(
            puzzlesPlayed = STATISTICS_PLAYED,
            puzzlesSolved = STATISTICS_SOLVED,
            totalTimeSpent = STATISTICS_TIME_PLAYED,
            streaks = User.Streaks(
                current = STATISTICS_STREAK_CURRENT,
                longest = STATISTICS_STREAK_LONGEST,
                lastActivityDate = LocalDate.now().minusDays(1)
            )
        ),
        authentication = User.AuthenticationState(
            provider = User.AuthenticationState.AuthProvider.GOOGLE,
            userId = USER_ID
        )
    )

    const val FIRST_NAME = "Darth"
    const val LAST_NAME = "Vader"
    const val RATING = 1200
    const val RATING_BLIND_MODE = 400
    const val HIGH_SCORE_RATED = 1350
    const val HIGH_SCORE_RUSH = 85
    const val HIGH_SCORE_STREAK = 42
    const val HIGH_SCORE_BOARD = 92
    const val HIGH_SCORE_FIND_SQUARE = 25
    const val HIGH_SCORE_MOVE_PIECE = 15
    const val STATISTICS_PLAYED = 5
    const val STATISTICS_SOLVED = 2
    const val STATISTICS_TIME_PLAYED = 1_000L
    const val STATISTICS_STREAK_CURRENT = 5
    const val STATISTICS_STREAK_LONGEST = 15
    const val USER_ID = "anakin.skywalker"
}
