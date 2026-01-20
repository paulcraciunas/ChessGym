package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.PuzzleRushResult
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OnPuzzleRushCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = OnPuzzleRushCompleteImpl(fakeUserRepository)

    @Test
    fun `GIVEN rush result WHEN invoke THEN updates puzzles played and solved`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = PuzzleRushResult(
            puzzlesSolved = 10,
            puzzlesFailed = 1,
            failedPuzzleIds = listOf(42),
            timeSpentMillis = 180_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(
                UserDefaults.STATISTICS_PLAYED + result.puzzlesSolved + result.puzzlesFailed,
                statistics.puzzlesPlayed
            )
            assertEquals(
                UserDefaults.STATISTICS_SOLVED + result.puzzlesSolved,
                statistics.puzzlesSolved
            )
        }
    }

    @Test
    fun `GIVEN rush with new high score WHEN invoke THEN updates high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val newHighScore = UserDefaults.HIGH_SCORE_RUSH + 10
        val result = PuzzleRushResult(
            puzzlesSolved = newHighScore,
            puzzlesFailed = 1,
            failedPuzzleIds = emptyList(),
            timeSpentMillis = 180_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(newHighScore, highScores.puzzleRush)
        }
    }

    @Test
    fun `GIVEN rush below high score WHEN invoke THEN does not update high score`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val belowHighScore = UserDefaults.HIGH_SCORE_RUSH - 10
        val result = PuzzleRushResult(
            puzzlesSolved = belowHighScore,
            puzzlesFailed = 1,
            failedPuzzleIds = emptyList(),
            timeSpentMillis = 180_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(UserDefaults.HIGH_SCORE_RUSH, highScores.puzzleRush)
        }
    }

    @Test
    fun `GIVEN rush result WHEN invoke THEN updates total time spent`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val timeSpent = 120_000L
        val result = PuzzleRushResult(
            puzzlesSolved = 5,
            puzzlesFailed = 1,
            failedPuzzleIds = emptyList(),
            timeSpentMillis = timeSpent
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(
                UserDefaults.STATISTICS_TIME_PLAYED + timeSpent,
                statistics.totalTimeSpent
            )
        }
    }

    @Test
    fun `GIVEN rush with failed puzzles WHEN invoke THEN adds to failed puzzles list`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val failedIds = listOf(42, 99, 123)
        val result = PuzzleRushResult(
            puzzlesSolved = 5,
            puzzlesFailed = 3,
            failedPuzzleIds = failedIds,
            timeSpentMillis = 100_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            failedIds.forEach { id ->
                assertTrue(failedPuzzles.contains(id))
            }
        }
    }

    @Test
    fun `GIVEN existing failed puzzles WHEN invoke with duplicates THEN avoids duplicates`() = runTest {
        // Given
        val existingFailedId = 42
        val currentUser = UserDefaults.signedInUser().copy(
            failedPuzzles = listOf(existingFailedId)
        )
        fakeUserRepository.update(currentUser)
        val result = PuzzleRushResult(
            puzzlesSolved = 5,
            puzzlesFailed = 1,
            failedPuzzleIds = listOf(existingFailedId, 99),
            timeSpentMillis = 100_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(2, failedPuzzles.size) // No duplicates
            assertTrue(failedPuzzles.contains(existingFailedId))
            assertTrue(failedPuzzles.contains(99))
        }
    }

    @Test
    fun `GIVEN rush result WHEN invoke THEN logs history entry`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)
        val result = PuzzleRushResult(
            puzzlesSolved = 12,
            puzzlesFailed = 1,
            failedPuzzleIds = listOf(42),
            timeSpentMillis = 175_000L
        )

        // When
        underTest(result)

        // Then
        fakeUserRepository.get().apply {
            assertTrue(history.isNotEmpty())
            val todayHistory = history.find { it.timestamp == LocalDate.now() }
            assertTrue(todayHistory != null)

            val historyData = todayHistory!!.data as User.HistoryItem.HistoryItemData.PuzzleRushData
            assertEquals(1, historyData.tries)
            assertEquals(result.puzzlesSolved, historyData.bestScore)
            assertEquals(result.timeSpentMillis, historyData.timeSpent)
        }
    }

    @Test
    fun `GIVEN multiple rush completions same day WHEN invoke THEN merges history entries`() = runTest {
        // Given
        val currentUser = UserDefaults.signedInUser()
        fakeUserRepository.update(currentUser)

        val firstResult = PuzzleRushResult(
            puzzlesSolved = 8,
            puzzlesFailed = 1,
            failedPuzzleIds = emptyList(),
            timeSpentMillis = 120_000L
        )
        val secondResult = PuzzleRushResult(
            puzzlesSolved = 12,
            puzzlesFailed = 1,
            failedPuzzleIds = emptyList(),
            timeSpentMillis = 150_000L
        )

        // When
        underTest(firstResult)
        underTest(secondResult)

        // Then
        fakeUserRepository.get().apply {
            val todayHistory = history.filter { it.timestamp == LocalDate.now() }
            assertEquals(1, todayHistory.size) // Should be merged

            val historyData = todayHistory.first().data as User.HistoryItem.HistoryItemData.PuzzleRushData
            assertEquals(2, historyData.tries) // 1 + 1
            assertEquals(12, historyData.bestScore) // max(8, 12)
            assertEquals(270_000L, historyData.timeSpent) // 120_000 + 150_000
        }
    }
}
