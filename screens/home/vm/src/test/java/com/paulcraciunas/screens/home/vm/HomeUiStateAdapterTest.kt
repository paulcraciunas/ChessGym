package com.paulcraciunas.screens.home.vm

import com.paulcraciunas.user.api.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class HomeUiStateAdapterTest {

    private val underTest = HomeUiStateAdapter()
    private val today = LocalDate.of(2026, 2, 17) // Tuesday

    @Test
    fun `GIVEN defaultUser WHEN adapt THEN returnsCorrectUiState`() {
        // Given
        val user = User()

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals("ChessEnthusiast", result.userProfile.name)
        assertEquals(1000, result.userProfile.currentRating)
        assertEquals(0, result.userProfile.totalActivities)

        assertEquals(0, result.userStats.puzzlesPlayed)
        assertEquals(0, result.userStats.puzzlesSolved)
        assertEquals(1000, result.userStats.currentRating)
        assertEquals(1000, result.userStats.bestRating)
        assertEquals(0, result.userStats.bestPuzzleRushScore)
        assertEquals(0, result.userStats.bestPuzzleStreakScore)
        assertEquals(0, result.userStats.bestFindTheSquareScore)
        assertEquals(0, result.userStats.bestMoveThePieceScore)
        assertEquals(400, result.userStats.bestBlindModeScore)

        assertEquals(emptyList<HomeUiState.HistoryGroup>(), result.history)
        assertFalse(result.isLoading)
    }

    @Test
    fun `GIVEN userWithCustomProfile WHEN adapt THEN userProfileCorrectlyMapped`() {
        // Given
        val user = User(
            profile = User.Profile(
                displayName = "JohnDoe",
                joinDate = LocalDate.of(2023, 5, 15),
            ),
            ratings = User.Ratings(current = 1450, blindMode = 600)
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals("JohnDoe", result.userProfile.name)
        assertEquals(1450, result.userProfile.currentRating)
        assertEquals(0, result.userProfile.totalActivities)
        assertEquals(LocalDate.of(2023, 5, 15), result.userProfile.joinDate)
    }

    @Test
    fun `GIVEN userWithStatistics WHEN adapt THEN userStatsCorrectlyMapped`() {
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
                puzzleStreak = 42,
                findTheSquare = 25,
                moveThePiece = 15,
                blindMode = 800
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(150, result.userStats.puzzlesPlayed)
        assertEquals(120, result.userStats.puzzlesSolved)
        assertEquals(1550, result.userStats.currentRating)
        assertEquals(1600, result.userStats.bestRating)
        assertEquals(85, result.userStats.bestPuzzleRushScore)
        assertEquals(42, result.userStats.bestPuzzleStreakScore)
        assertEquals(25, result.userStats.bestFindTheSquareScore)
        assertEquals(15, result.userStats.bestMoveThePieceScore)
        assertEquals(800, result.userStats.bestBlindModeScore)
    }

    @Test
    fun `GIVEN userWithPuzzleRushHistory WHEN adapt THEN puzzleRushEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 3,
                        bestScore = 85,
                        timeSpent = 1800000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        assertEquals(HomeUiStateAdapter.LABEL_TODAY, result.history[0].label)
        assertEquals(1, result.history[0].events.size)

        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.PuzzleRushEvent
        assertEquals(85, event.highScore)
        assertEquals(3, event.runs)
    }

    @Test
    fun `GIVEN userWithBoardVisualizationHistory WHEN adapt THEN boardVizEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 5,
                        timeSpent = 900000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.BoardVizEvent
        assertEquals(5, event.runs)
    }

    @Test
    fun `GIVEN userWithBlindModeTrainingHistory WHEN adapt THEN blindModeTrainingEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                        tries = 4,
                        mostMovesCompleted = 18,
                        timeSpent = 1200000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.BlindModeTrainingEvent
        assertEquals(18, event.mostMovesCompleted)
        assertEquals(4, event.runs)
    }

    @Test
    fun `GIVEN userWithBlindModeRatedHistory WHEN adapt THEN blindModeEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BlindModeData(
                        played = 2,
                        ratingChange = -25,
                        timeSpent = 600000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.BlindModeEvent
        assertEquals(-25, event.ratingChange)
        assertEquals(2, event.gamesPlayed)
    }

    @Test
    fun `GIVEN userWithRatedPuzzleHistory WHEN adapt THEN ratedPuzzleEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
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
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.RatedPuzzleEvent
        assertEquals(35, event.ratingChange)
        assertEquals(15, event.count)
    }

    @Test
    fun `GIVEN userWithPuzzleStreakHistory WHEN adapt THEN puzzleStreakEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.PuzzleStreakData(
                        finalStreakCount = 12,
                        timeSpent = 3600000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.PuzzleStreakEvent
        assertEquals(12, event.finalStreakCount)
    }

    @Test
    fun `GIVEN userWithFailedPuzzleHistory WHEN adapt THEN failedPuzzleEventCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.FailedPuzzleData(
                        puzzlesSolved = 5,
                        timeSpent = 1500000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        val event = result.history[0].events[0] as HomeUiState.HistoryEvent.FailedPuzzleEvent
        assertEquals(5, event.puzzlesSolved)
    }

    @Test
    fun `GIVEN itemsOnSameDate WHEN adapt THEN eventsGroupedTogether`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 2, bestScore = 75, timeSpent = 900000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 3, timeSpent = 600000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        assertEquals(HomeUiStateAdapter.LABEL_TODAY, result.history[0].label)
        assertEquals(2, result.history[0].events.size)
        assertTrue(result.history[0].events[0] is HomeUiState.HistoryEvent.PuzzleRushEvent)
        assertTrue(result.history[0].events[1] is HomeUiState.HistoryEvent.BoardVizEvent)
    }

    @Test
    fun `GIVEN itemsAcrossTimePeriods WHEN adapt THEN groupedByPeriodAndSorted`() {
        // Given - use Thursday Feb 19 so there's room for "This Week" entries
        val thursday = LocalDate.of(2026, 2, 19) // Thursday
        val yesterday = thursday.minusDays(1) // Wednesday Feb 18
        val thisWeek = thursday.minusDays(3) // Monday Feb 16 (start of week)
        val thisMonth = thursday.minusDays(12) // Feb 7
        val thisYear = LocalDate.of(2026, 1, 5) // January
        val older = LocalDate.of(2025, 6, 15) // Last year

        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = older,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 1, bestScore = 50, timeSpent = 300000L
                    )
                ),
                User.HistoryItem(
                    timestamp = thursday,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 2, timeSpent = 400000L
                    )
                ),
                User.HistoryItem(
                    timestamp = thisMonth,
                    data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = 5, puzzlesSolved = 4, ratingChange = 15, timeSpent = 1000000L
                    )
                ),
                User.HistoryItem(
                    timestamp = yesterday,
                    data = User.HistoryItem.HistoryItemData.BlindModeData(
                        played = 1, ratingChange = 10, timeSpent = 500000L
                    )
                ),
                User.HistoryItem(
                    timestamp = thisWeek,
                    data = User.HistoryItem.HistoryItemData.PuzzleStreakData(
                        finalStreakCount = 8, timeSpent = 2000000L
                    )
                ),
                User.HistoryItem(
                    timestamp = thisYear,
                    data = User.HistoryItem.HistoryItemData.FailedPuzzleData(
                        puzzlesSolved = 3, timeSpent = 900000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, thursday)

        // Then - sorted by period order
        assertEquals(6, result.history.size)
        assertEquals(HomeUiStateAdapter.LABEL_TODAY, result.history[0].label)
        assertEquals(HomeUiStateAdapter.LABEL_YESTERDAY, result.history[1].label)
        assertEquals(HomeUiStateAdapter.LABEL_THIS_WEEK, result.history[2].label)
        assertEquals(HomeUiStateAdapter.LABEL_THIS_MONTH, result.history[3].label)
        assertEquals(HomeUiStateAdapter.LABEL_THIS_YEAR, result.history[4].label)
        assertEquals(HomeUiStateAdapter.LABEL_OLDER, result.history[5].label)
    }

    @Test
    fun `GIVEN multipleItemsInSamePeriod WHEN adapt THEN groupedUnderSameLabel`() {
        // Given - use Thursday so there are "This Week" days between yesterday and start of week
        val thursday = LocalDate.of(2026, 2, 19) // Thursday
        val tuesday = thursday.minusDays(2) // Tuesday Feb 17 - this week, not today/yesterday
        val monday = thursday.minusDays(3) // Monday Feb 16 - this week, not today/yesterday

        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = tuesday,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 1, bestScore = 30, timeSpent = 200000L
                    )
                ),
                User.HistoryItem(
                    timestamp = monday,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 1, timeSpent = 100000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, thursday)

        // Then - both should be in "This Week"
        assertEquals(1, result.history.size)
        assertEquals(HomeUiStateAdapter.LABEL_THIS_WEEK, result.history[0].label)
        assertEquals(2, result.history[0].events.size)
    }

    @Test
    fun `GIVEN userWithAllHistoryEventTypes WHEN adapt THEN allEventsCorrectlyMapped`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 3, bestScore = 85, timeSpent = 900000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 4, timeSpent = 800000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                        tries = 2, mostMovesCompleted = 12, timeSpent = 700000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.BlindModeData(
                        played = 1, ratingChange = 20, timeSpent = 500000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = 10, puzzlesSolved = 8, ratingChange = 25, timeSpent = 1200000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.PuzzleStreakData(
                        finalStreakCount = 7, timeSpent = 2000000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.FailedPuzzleData(
                        puzzlesSolved = 3, timeSpent = 800000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(1, result.history.size)
        assertEquals(7, result.history[0].events.size)

        val events = result.history[0].events
        assertTrue(events[0] is HomeUiState.HistoryEvent.PuzzleRushEvent)
        assertTrue(events[1] is HomeUiState.HistoryEvent.BoardVizEvent)
        assertTrue(events[2] is HomeUiState.HistoryEvent.BlindModeTrainingEvent)
        assertTrue(events[3] is HomeUiState.HistoryEvent.BlindModeEvent)
        assertTrue(events[4] is HomeUiState.HistoryEvent.RatedPuzzleEvent)
        assertTrue(events[5] is HomeUiState.HistoryEvent.PuzzleStreakEvent)
        assertTrue(events[6] is HomeUiState.HistoryEvent.FailedPuzzleEvent)
    }

    @Test
    fun `GIVEN userWithHistoryItems WHEN adapt THEN totalActivitiesMatchesHistorySize`() {
        // Given
        val user = User(
            history = listOf(
                User.HistoryItem(
                    timestamp = today,
                    data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = 1, bestScore = 50, timeSpent = 300000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today.minusDays(1),
                    data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = 1, timeSpent = 200000L
                    )
                ),
                User.HistoryItem(
                    timestamp = today.minusDays(2),
                    data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = 5, puzzlesSolved = 3, ratingChange = 10, timeSpent = 600000L
                    )
                )
            )
        )

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(3, result.userProfile.totalActivities)
    }

    @Test
    fun `GIVEN emptyHistory WHEN adapt THEN historyIsEmpty`() {
        // Given
        val user = User(history = emptyList())

        // When
        val result = underTest.adapt(user, today)

        // Then
        assertEquals(emptyList<HomeUiState.HistoryGroup>(), result.history)
    }

    @Test
    fun `GIVEN timePeriod WHEN dateIsToday THEN returnsToday`() {
        assertEquals(
            HomeUiStateAdapter.LABEL_TODAY,
            HomeUiStateAdapter.timePeriod(today, today)
        )
    }

    @Test
    fun `GIVEN timePeriod WHEN dateIsYesterday THEN returnsYesterday`() {
        assertEquals(
            HomeUiStateAdapter.LABEL_YESTERDAY,
            HomeUiStateAdapter.timePeriod(today.minusDays(1), today)
        )
    }

    @Test
    fun `GIVEN timePeriod WHEN dateIsThisWeek THEN returnsThisWeek`() {
        // today is Tuesday Feb 17, Monday is Feb 16
        // Sunday Feb 15 is in this week (starts Monday Feb 16)... actually no.
        // Feb 17 is Tuesday, start of week (Monday) is Feb 16.
        // Feb 15 (Sunday) would be before Monday Feb 16 -> not This Week
        // Feb 16 (Monday) is yesterday. So "This Week" means Monday..Saturday excluding today/yesterday
        // Actually Monday is yesterday in this case.
        // Let's test with a date that's clearly in the week but not today/yesterday
        // Today is Tuesday, so nothing is "This Week" that isn't today or yesterday
        // Let's use a Wednesday as today so we have more room
        val wednesday = LocalDate.of(2026, 2, 18)
        val monday = wednesday.minusDays(2) // Not yesterday, still this week
        assertEquals(
            HomeUiStateAdapter.LABEL_THIS_WEEK,
            HomeUiStateAdapter.timePeriod(monday, wednesday)
        )
    }

    @Test
    fun `GIVEN timePeriod WHEN dateIsThisMonth THEN returnsThisMonth`() {
        val earlyFeb = LocalDate.of(2026, 2, 5)
        assertEquals(
            HomeUiStateAdapter.LABEL_THIS_MONTH,
            HomeUiStateAdapter.timePeriod(earlyFeb, today)
        )
    }

    @Test
    fun `GIVEN timePeriod WHEN dateIsThisYear THEN returnsThisYear`() {
        val january = LocalDate.of(2026, 1, 10)
        assertEquals(
            HomeUiStateAdapter.LABEL_THIS_YEAR,
            HomeUiStateAdapter.timePeriod(january, today)
        )
    }

    @Test
    fun `GIVEN timePeriod WHEN dateIsOlder THEN returnsOlder`() {
        val lastYear = LocalDate.of(2025, 6, 15)
        assertEquals(
            HomeUiStateAdapter.LABEL_OLDER,
            HomeUiStateAdapter.timePeriod(lastYear, today)
        )
    }
}
