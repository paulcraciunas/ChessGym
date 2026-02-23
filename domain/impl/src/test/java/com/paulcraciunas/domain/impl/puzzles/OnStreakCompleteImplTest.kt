package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class OnStreakCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = OnStreakCompleteImpl(fakeUserRepository)

    @Test
    fun `GIVEN streak higher than high score WHEN invoke THEN updates high score and returns true`() = runTest {
        // Given
        val currentHighScore = UserDefaults.HIGH_SCORE_STREAK
        val newStreakCount = currentHighScore + 10
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = newStreakCount, lastPuzzleId = 42)
            )
        )
        fakeUserRepository.update(user)

        // When
        val result = underTest(TIME_SPENT)

        // Then
        assertTrue(result.isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(newStreakCount, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN streak lower than high score WHEN invoke THEN does not update high score and returns false`() = runTest {
        // Given
        val currentHighScore = UserDefaults.HIGH_SCORE_STREAK
        val lowerStreakCount = currentHighScore - 10
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = lowerStreakCount, lastPuzzleId = 42)
            )
        )
        fakeUserRepository.update(user)

        // When
        val result = underTest(TIME_SPENT)

        // Then
        assertFalse(result.isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(currentHighScore, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN streak equal to high score WHEN invoke THEN does not update high score and returns false`() = runTest {
        // Given
        val currentHighScore = UserDefaults.HIGH_SCORE_STREAK
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = currentHighScore, lastPuzzleId = 42)
            )
        )
        fakeUserRepository.update(user)

        // When
        val result = underTest(TIME_SPENT)

        // Then
        assertFalse(result.isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(currentHighScore, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN active streak WHEN invoke THEN resets streak count to zero`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 15, lastPuzzleId = 42)
            )
        )
        fakeUserRepository.update(user)

        // When
        underTest(TIME_SPENT)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(0, ratings.puzzleStreak.currentCount)
        }
    }

    @Test
    fun `GIVEN active streak WHEN invoke THEN clears lastPuzzleId`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 10, lastPuzzleId = 99)
            )
        )
        fakeUserRepository.update(user)

        // When
        underTest(TIME_SPENT)

        // Then
        fakeUserRepository.get().apply {
            assertNull(ratings.puzzleStreak.lastPuzzleId)
        }
    }

    @Test
    fun `GIVEN first streak ever WHEN invoke with count above 0 THEN sets high score`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            highScores = UserDefaults.signedInUser().highScores.copy(puzzleStreak = 0),
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 5, lastPuzzleId = 42)
            )
        )
        fakeUserRepository.update(user)

        // When
        val result = underTest(TIME_SPENT)

        // Then
        assertTrue(result.isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(5, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN zero streak WHEN invoke THEN does not set high score`() = runTest {
        // Given
        val currentHighScore = UserDefaults.HIGH_SCORE_STREAK
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
            )
        )
        fakeUserRepository.update(user)

        // When
        val result = underTest(TIME_SPENT)

        // Then
        assertFalse(result.isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(currentHighScore, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN active streak WHEN invoke THEN logs PuzzleStreakData history`() = runTest {
        // Given
        val streakCount = 7
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = streakCount, lastPuzzleId = 42)
            )
        )
        fakeUserRepository.update(user)

        // When
        underTest(TIME_SPENT)

        // Then
        val updatedUser = fakeUserRepository.get()
        assertEquals(1, updatedUser.history.size)
        val historyData = updatedUser.history.first().data
        assertTrue(historyData is User.HistoryItem.HistoryItemData.PuzzleStreakData)
        val streakData = historyData as User.HistoryItem.HistoryItemData.PuzzleStreakData
        assertEquals(streakCount, streakData.finalStreakCount)
        assertEquals(TIME_SPENT, streakData.timeSpent)
    }

    companion object {
        private const val TIME_SPENT = 5_000L
    }
}
