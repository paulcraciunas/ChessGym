package com.paulcraciunas.user.impl

import com.paulcraciunas.user.api.User
import java.time.LocalDate

internal object UserTestFixtures {

    fun createDefaultUser(): User = User(
        profile = User.Profile(
            firstName = "Test",
            lastName = "Player",
            joinDate = LocalDate.of(2023, 1, 1),
            avatarUrl = null
        ),
        ratings = User.Ratings(
            current = 1350,
            blindMode = 400
        ),
        highScores = User.HighScores(
            ratedPuzzle = 1350,
            puzzleRush = 85,
            blindMode = 400
        ),
        statistics = User.Statistics(
            puzzlesPlayed = 150,
            puzzlesSolved = 120,
            totalTimeSpent = 3600000, // 1 hour in milliseconds
        ),
        history = listOf(
            createSamplePuzzleRushHistoryItem(),
            createSampleBoardVisualizationHistoryItem(),
            createSampleBlindModeHistoryItem()
        ),
        failedPuzzles = listOf(12, 34),
        authentication = null // Not signed in by default
    )

    fun createSignedUpUser(): User = createDefaultUser().copy(
        authentication = User.AuthenticationState(
            provider = User.AuthenticationState.AuthProvider.GOOGLE,
            userId = "user_123"
        )
    )

    private fun createSamplePuzzleRushHistoryItem(): User.HistoryItem = User.HistoryItem(
        timestamp = LocalDate.now().minusDays(1),
        data = User.HistoryItem.HistoryItemData.PuzzleRushData(
            tries = 3,
            bestScore = 85,
            timeSpent = 600000 // 10 minutes
        )
    )

    private fun createSampleBoardVisualizationHistoryItem(): User.HistoryItem = User.HistoryItem(
        timestamp = LocalDate.now().minusDays(2),
        data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
            sessionsCompleted = 5,
            timeSpent = 900000 // 15 minutes
        )
    )

    private fun createSampleBlindModeHistoryItem(): User.HistoryItem = User.HistoryItem(
        timestamp = LocalDate.now().minusDays(3),
        data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
            tries = 2,
            mostMovesCompleted = 12,
            timeSpent = 1200000 // 20 minutes
        )
    )

    fun createSampleRatedPuzzleHistoryItem(): User.HistoryItem = User.HistoryItem(
        timestamp = LocalDate.now().minusDays(4),
        data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
            puzzlesPlayed = 10,
            puzzlesSolved = 8,
            ratingChange = 25,
            timeSpent = 1800000 // 30 minutes
        )
    )
}
