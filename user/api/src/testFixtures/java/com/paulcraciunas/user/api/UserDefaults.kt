package com.paulcraciunas.user.api

import java.time.LocalDate

object UserDefaults {
    fun signedInUser(): User = User(
        deviceId = DEVICE_ID,
        profile = User.Profile(
            displayName = DISPLAY_NAME,
            joinDate = LocalDate.of(2023, 1, 1),
        ),
        ratings = User.Ratings(
            current = RATING,
            blindMode = RATING_BLIND_MODE
        ),
        highScores = User.HighScores(
            ratedPuzzle = HIGH_SCORE_RATED,
            puzzleRush = HIGH_SCORE_RUSH,
            puzzleStreak = HIGH_SCORE_STREAK,
            findTheSquare = HIGH_SCORE_FIND_SQUARE,
            moveThePiece = HIGH_SCORE_MOVE_PIECE,
            blindMode = RATING_BLIND_MODE
        ),
        statistics = User.Statistics(
            puzzlesPlayed = STATISTICS_PLAYED,
            puzzlesSolved = STATISTICS_SOLVED,
            totalTimeSpent = STATISTICS_TIME_PLAYED,
        ),
        authentication = User.AuthenticationState(
            provider = User.AuthenticationState.AuthProvider.GOOGLE,
            userId = USER_ID
        )
    )

    const val DISPLAY_NAME = "DarthVader"
    const val RATING = 1200
    const val RATING_BLIND_MODE = User.DEFAULT_BLIND_MODE_RATING
    const val HIGH_SCORE_RATED = 1350
    const val HIGH_SCORE_RUSH = 85
    const val HIGH_SCORE_STREAK = 42
    const val HIGH_SCORE_FIND_SQUARE = 25
    const val HIGH_SCORE_MOVE_PIECE = 15
    const val STATISTICS_PLAYED = 5
    const val STATISTICS_SOLVED = 2
    const val STATISTICS_TIME_PLAYED = 1_000L
    const val USER_ID = "anakin.skywalker"
    const val DEVICE_ID = "test-device-id"
}
