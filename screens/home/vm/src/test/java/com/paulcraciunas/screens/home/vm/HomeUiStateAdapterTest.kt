package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.user.api.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class HomeUiStateAdapterTest {

    private val underTest = HomeUiStateAdapter()

    @Test
    fun `GIVEN defaultUser WHEN adapt THEN returnsCorrectUiState`() {
        // Given
        val user = User()

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals("Master Ego", result.userProfile.name)
        assertEquals(1200, result.userProfile.currentRating)
        assertEquals(0, result.userProfile.totalActivities)
        assertEquals(LocalDate.now(), result.userProfile.joinDate)

        assertEquals(0, result.userStats.puzzlesPlayed)
        assertEquals(0, result.userStats.puzzlesSolved)
        assertEquals(1200, result.userStats.currentRating)
        assertEquals(1200, result.userStats.bestRating)
        assertEquals(0, result.userStats.bestPuzzleRushScore)
        assertEquals(400, result.userStats.bestBlindModeScore)
        assertEquals(0, result.userStats.bestVisualizationScore)

        assertEquals(emptyList<HomeUiState.HistoryGroup>(), result.history)
        assertFalse(result.isLoading)
    }

    @Test
    fun `GIVEN userWithCustomProfile_WHEN_adapt_THEN_userProfileCorrectlyMapped`() {
        // Given
        val user = User(
            profile = User.Profile(
                firstName = "John",
                lastName = "Doe",
                joinDate = LocalDate.of(2023, 5, 15),
                avatarUrl = "https://example.com/avatar.jpg"
            ),
            ratings = User.Ratings(current = 1450, blindMode = 600)
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals("John Doe", result.userProfile.name)
        assertEquals(1450, result.userProfile.currentRating)
        assertEquals(0, result.userProfile.totalActivities) // No history items
        assertEquals(LocalDate.of(2023, 5, 15), result.userProfile.joinDate)
    }

    @Test
    fun `GIVEN userWithStatistics_WHEN_adapt_THEN_userStatsCorrectlyMapped`() {
        // Given
        val user = User(
            statistics = User.Statistics(
                puzzlesPlayed = 150,
                puzzlesSolved = 120,
                totalTimeSpent = 7200000L
            ),
            ratings = User.Ratings(current = 1550, blindMode = 750),
            highScores = User.HighScores(
                ratedPuzzle = 1600,
                puzzleRush = 85,
                boardVisualization = 92,
                blindMode = 800
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(150, result.userStats.puzzlesPlayed)
        assertEquals(120, result.userStats.puzzlesSolved)
        assertEquals(1550, result.userStats.currentRating)
        assertEquals(1600, result.userStats.bestRating)
        assertEquals(85, result.userStats.bestPuzzleRushScore)
        assertEquals(800, result.userStats.bestBlindModeScore)
        assertEquals(92, result.userStats.bestVisualizationScore)
    }

    @Test
    fun `GIVEN userWithPuzzleRushHistory_WHEN_adapt_THEN_puzzleRushEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 15),
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 3,
                        bestScore = 85,
                        timeSpent = 1800000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(1, result.history.size)
        assertEquals(LocalDate.of(2024, 1, 15), result.history[0].date)
        assertEquals(1, result.history[0].events.size)

        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.PuzzleRushEvent
        assertEquals(85, event.highScore)
        assertEquals(3, event.runs)
    }

    @Test
    fun `GIVEN userWithBoardVisualizationHistory_WHEN_adapt_THEN_boardVizEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 14),
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 5,
                        timeSpent = 900000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.BoardVizEvent
        assertEquals(5, event.runs)
    }

    @Test
    fun `GIVEN userWithBlindModeTrainingHistory_WHEN_adapt_THEN_blindModeTrainingEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 13),
                    data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                        tries = 4,
                        mostMovesCompleted = 18,
                        timeSpent = 1200000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.BlindModeTrainingEvent
        assertEquals(18, event.mostMovesCompleted)
        assertEquals(4, event.runs)
    }

    @Test
    fun `GIVEN userWithBlindModeRatedHistory_WHEN_adapt_THEN_blindModeEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 12),
                    data = User.HistoryItem.HistoryItemData.BlindModeData(
                        played = 2,
                        ratingChange = -25,
                        timeSpent = 600000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.BlindModeEvent
        assertEquals(-25, event.ratingChange)
        assertEquals(2, event.gamesPlayed)
    }

    @Test
    fun `GIVEN userWithRatedPuzzleHistory_WHEN_adapt_THEN_ratedPuzzleEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 11),
                    data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = 15,
                        puzzlesSolved = 12,
                        ratingChange = 35,
                        timeSpent = 2100000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.RatedPuzzleEvent
        assertEquals(35, event.ratingChange)
        assertEquals(15, event.count)
    }

    @Test
    fun `GIVEN userWithMultipleHistoryItemsOnSameDate_WHEN_adapt_THEN_eventsGroupedByDate`() {
        // Given
        val date = LocalDate.of(2024, 1, 15)
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = date,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 2,
                        bestScore = 75,
                        timeSpent = 900000L
                    )
                ),
                User.HistoryItem(
                    timestamp = date,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 3,
                        timeSpent = 600000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(1, result.history.size) // Same date, so grouped together
        assertEquals(date, result.history[0].date)
        assertEquals(2, result.history[0].events.size)

        val puzzleRushEvent = result.history[0].events[0] as HomeUiState.HistoryEvent.PuzzleRushEvent
        assertEquals(75, puzzleRushEvent.highScore)
        assertEquals(2, puzzleRushEvent.runs)

        val boardVizEvent = result.history[0].events[1] as HomeUiState.HistoryEvent.BoardVizEvent
        assertEquals(3, boardVizEvent.runs)
    }

    @Test
    fun `GIVEN userWithMultipleHistoryItemsOnDifferentDates_WHEN_adapt_THEN_eventsGroupedAndSortedByDate`() {
        // Given
        val today = LocalDate.of(2024, 1, 15)
        val yesterday = LocalDate.of(2024, 1, 14)
        val twoDaysAgo = LocalDate.of(2024, 1, 13)

        val user = User(
            history = listOf(
                // Add items in non-chronological order to test sorting
                User.HistoryItem(
                    timestamp = yesterday,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 1,
                        bestScore = 60,
                        timeSpent = 300000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 2,
                        timeSpent = 400000L
                    )
                ),
                User.HistoryItem(
                    timestamp = twoDaysAgo,
                    data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = 5,
                        puzzlesSolved = 4,
                        ratingChange = 15,
                        timeSpent = 1000000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(3, result.history.size)

        // Should be sorted by date descending (most recent first)
        assertEquals(today, result.history[0].date)
        assertEquals(yesterday, result.history[1].date)
        assertEquals(twoDaysAgo, result.history[2].date)

        // Check each group has correct events
        assertEquals(1, result.history[0].events.size) // Today: 1 event
        assertEquals(1, result.history[1].events.size) // Yesterday: 1 event  
        assertEquals(1, result.history[2].events.size) // Two days ago: 1 event
    }

    @Test
    fun `GIVEN userWithAllHistoryEventTypes_WHEN_adapt_THEN_allEventsCorrectlyMapped`() {
        // Given
        val date = LocalDate.of(2024, 1, 15)
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = date,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 3,
                        bestScore = 85,
                        timeSpent = 900000L
                    )
                ),
                User.HistoryItem(
                    timestamp = date,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 4,
                        timeSpent = 800000L
                    )
                ),
                User.HistoryItem(
                    timestamp = date,
                    data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                        tries = 2,
                        mostMovesCompleted = 12,
                        timeSpent = 700000L
                    )
                ),
                User.HistoryItem(
                    timestamp = date,
                    data = User.HistoryItem.HistoryItemData.BlindModeData(
                        played = 1,
                        ratingChange = 20,
                        timeSpent = 500000L
                    )
                ),
                User.HistoryItem(
                    timestamp = date,
                    data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = 10,
                        puzzlesSolved = 8,
                        ratingChange = 25,
                        timeSpent = 1200000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(1, result.history.size)
        assertEquals(5, result.history[0].events.size)

        val events = result.history[0].events

        // Verify each event type is correctly mapped
        val puzzleRushEvent = events[0] as HomeUiState.HistoryEvent.PuzzleRushEvent
        assertEquals(85, puzzleRushEvent.highScore)
        assertEquals(3, puzzleRushEvent.runs)

        val boardVizEvent = events[1] as HomeUiState.HistoryEvent.BoardVizEvent
        assertEquals(4, boardVizEvent.runs)

        val blindTrainingEvent = events[2] as HomeUiState.HistoryEvent.BlindModeTrainingEvent
        assertEquals(12, blindTrainingEvent.mostMovesCompleted)
        assertEquals(2, blindTrainingEvent.runs)

        val blindModeEvent = events[3] as HomeUiState.HistoryEvent.BlindModeEvent
        assertEquals(20, blindModeEvent.ratingChange)
        assertEquals(1, blindModeEvent.gamesPlayed)

        val ratedPuzzleEvent = events[4] as HomeUiState.HistoryEvent.RatedPuzzleEvent
        assertEquals(25, ratedPuzzleEvent.ratingChange)
        assertEquals(10, ratedPuzzleEvent.count)
    }

    @Test
    fun `GIVEN userWithHistoryItems_WHEN_adapt_THEN_totalActivitiesMatchesHistorySize`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 15),
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 1,
                        bestScore = 50,
                        timeSpent = 300000L
                    )
                ),
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 14),
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 1,
                        timeSpent = 200000L
                    )
                ),
                User.HistoryItem(
                    timestamp = LocalDate.of(2024, 1, 13),
                    data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = 5,
                        puzzlesSolved = 3,
                        ratingChange = 10,
                        timeSpent = 600000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(3, result.userProfile.totalActivities)
        assertEquals(3, result.history.size) // 3 different dates
    }

    @Test
    fun `GIVEN userWithExtremeDateValues WHEN adapt THEN datesCorrectlyHandled`() {
        // Given
        val farPast = LocalDate.of(1970, 1, 1)
        val farFuture = LocalDate.of(2099, 12, 31)

        val user = User(
            profile = User.Profile(joinDate = farPast),
            history = listOf(
                User.HistoryItem(
                    timestamp = farFuture,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 1,
                        bestScore = 100,
                        timeSpent = 300000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user)

        // Then
        assertEquals(farPast, result.userProfile.joinDate)
        assertEquals(farFuture, result.history[0].date)
    }
} 