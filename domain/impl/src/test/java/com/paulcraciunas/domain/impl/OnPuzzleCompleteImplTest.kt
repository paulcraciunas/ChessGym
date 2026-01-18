package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.PuzzleCompletionResult
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OnPuzzleCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest: OnPuzzleCompleteImpl = OnPuzzleCompleteImpl(fakeUserRepository)

    @Test
    fun `GIVEN successful completion WHEN invoke THEN updates rating stats and history`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        val completionResult = PuzzleCompletionResult(
            puzzleRating = 1400,
            wasSuccessful = true,
            ratingChange = 200,
            timeSpentMillis = 300L
        )
        fakeUserRepository.update(currentUser)

        // When
        underTest(completionResult)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.STATISTICS_PLAYED + 1, statistics.puzzlesPlayed)
            assertEquals(UserDefaults.STATISTICS_SOLVED + 1, statistics.puzzlesSolved)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + completionResult.timeSpentMillis, statistics.totalTimeSpent)
            assertEquals(UserDefaults.RATING + completionResult.ratingChange, ratings.current)
            assertEquals(ratings.current, highScores.ratedPuzzle)

            assertTrue(history.isNotEmpty())
            assertEquals(1, history.size)
            assertEquals(LocalDate.now(), history.first().timestamp)
            val historyData = history.first().data as User.HistoryItem.HistoryItemData.RatedPuzzleData
            assertEquals(1, historyData.puzzlesPlayed)
            assertEquals(1, historyData.puzzlesSolved)
            assertEquals(completionResult.ratingChange, historyData.ratingChange)
            assertEquals(completionResult.timeSpentMillis, historyData.timeSpent)
        }
    }

    @Test
    fun `GIVEN failed completion WHEN invoke THEN updates played count and logs failure`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        val completionResult = PuzzleCompletionResult(
            puzzleRating = 1450,
            wasSuccessful = false,
            ratingChange = 15,
            timeSpentMillis = 800L
        )
        fakeUserRepository.update(currentUser)

        // When
        underTest(completionResult)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.STATISTICS_PLAYED + 1, statistics.puzzlesPlayed)
            assertEquals(UserDefaults.STATISTICS_SOLVED, statistics.puzzlesSolved)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + completionResult.timeSpentMillis, statistics.totalTimeSpent)
            assertEquals(UserDefaults.RATING - completionResult.ratingChange, ratings.current)
            assertEquals(UserDefaults.HIGH_SCORE_RATED, highScores.ratedPuzzle)

            assertTrue(history.isNotEmpty())
            val historyData = history.first().data as User.HistoryItem.HistoryItemData.RatedPuzzleData
            assertEquals(1, historyData.puzzlesPlayed)
            assertEquals(0, historyData.puzzlesSolved)
            assertEquals(completionResult.ratingChange, historyData.ratingChange)
            assertEquals(completionResult.timeSpentMillis, historyData.timeSpent)
        }
    }
}
