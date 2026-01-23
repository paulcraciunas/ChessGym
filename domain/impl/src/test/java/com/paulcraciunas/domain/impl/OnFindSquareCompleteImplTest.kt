package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.FindSquareResult
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OnFindSquareCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = OnFindSquareCompleteImpl(fakeUserRepository)

    @Test
    fun `GIVEN score above high score WHEN invoke THEN updates high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val newHighScore = UserDefaults.HIGH_SCORE_FIND_SQUARE + 10
        val result = FindSquareResult(
            score = newHighScore,
            timeSpentMillis = 30_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(newHighScore, highScores.findTheSquare)
        }
    }

    @Test
    fun `GIVEN score below high score WHEN invoke THEN does not update high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val belowHighScore = UserDefaults.HIGH_SCORE_FIND_SQUARE - 5
        val result = FindSquareResult(
            score = belowHighScore,
            timeSpentMillis = 30_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_FIND_SQUARE, highScores.findTheSquare)
        }
    }

    @Test
    fun `GIVEN score equal to high score WHEN invoke THEN does not update high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = FindSquareResult(
            score = UserDefaults.HIGH_SCORE_FIND_SQUARE,
            timeSpentMillis = 30_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_FIND_SQUARE, highScores.findTheSquare)
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN logs history entry`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = FindSquareResult(
            score = 15,
            timeSpentMillis = 30_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertTrue(history.isNotEmpty())
            val todayHistory = history.find { it.timestamp == LocalDate.now() }
            assertTrue(todayHistory != null)

            val historyData = todayHistory!!.data as User.HistoryItem.HistoryItemData.BoardVisualizationData
            assertEquals(1, historyData.sessionsCompleted)
            assertEquals(result.timeSpentMillis, historyData.timeSpent)
        }
    }

    @Test
    fun `GIVEN multiple completions same day WHEN invoke THEN merges history entries`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)

        val firstResult = FindSquareResult(
            score = 10,
            timeSpentMillis = 30_000L
        )
        val secondResult = FindSquareResult(
            score = 20,
            timeSpentMillis = 30_000L
        )

        // When
        underTest(firstResult)
        underTest(secondResult)

        // Then
        fakeUserRepository.get().apply {
            val todayHistory = history.filter { it.timestamp == LocalDate.now() }
            assertEquals(1, todayHistory.size) // Should be merged

            val historyData = todayHistory.first().data as User.HistoryItem.HistoryItemData.BoardVisualizationData
            assertEquals(2, historyData.sessionsCompleted) // 1 + 1
            assertEquals(60_000L, historyData.timeSpent) // 30_000 + 30_000
        }
    }

    @Test
    fun `GIVEN user with zero high score WHEN invoke with positive score THEN updates high score`() = runTest {
        // Given
        val currentUser = User()
        fakeUserRepository.update(currentUser)
        val result = FindSquareResult(
            score = 5,
            timeSpentMillis = 30_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(5, highScores.findTheSquare)
        }
    }

    @Test
    fun `GIVEN result WHEN invoke THEN does not affect other high scores`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = FindSquareResult(
            score = 100,
            timeSpentMillis = 30_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_RATED, highScores.ratedPuzzle)
            assertEquals(UserDefaults.HIGH_SCORE_RUSH, highScores.puzzleRush)
            assertEquals(UserDefaults.HIGH_SCORE_STREAK, highScores.puzzleStreak)
            assertEquals(UserDefaults.HIGH_SCORE_BOARD, highScores.boardVisualization)
            assertEquals(UserDefaults.RATING_BLIND_MODE, highScores.blindMode)
        }
    }
}
