package com.paulcraciunas.domain.impl

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
            puzzleStreak = User.PuzzleStreak(currentCount = newStreakCount, lastPuzzleId = 42)
        )
        fakeUserRepository.update(user)

        // When
        val isNewHighScore = underTest(newStreakCount)

        // Then
        assertTrue(isNewHighScore)
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
            puzzleStreak = User.PuzzleStreak(currentCount = lowerStreakCount, lastPuzzleId = 42)
        )
        fakeUserRepository.update(user)

        // When
        val isNewHighScore = underTest(lowerStreakCount)

        // Then
        assertFalse(isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(currentHighScore, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN streak equal to high score WHEN invoke THEN does not update high score and returns false`() = runTest {
        // Given
        val currentHighScore = UserDefaults.HIGH_SCORE_STREAK
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = currentHighScore, lastPuzzleId = 42)
        )
        fakeUserRepository.update(user)

        // When
        val isNewHighScore = underTest(currentHighScore)

        // Then
        assertFalse(isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(currentHighScore, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN active streak WHEN invoke THEN resets streak count to zero`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = 15, lastPuzzleId = 42)
        )
        fakeUserRepository.update(user)

        // When
        underTest(15)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(0, puzzleStreak.currentCount)
        }
    }

    @Test
    fun `GIVEN active streak WHEN invoke THEN clears lastPuzzleId`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = 10, lastPuzzleId = 99)
        )
        fakeUserRepository.update(user)

        // When
        underTest(10)

        // Then
        fakeUserRepository.get().apply {
            assertNull(puzzleStreak.lastPuzzleId)
        }
    }

    @Test
    fun `GIVEN first streak ever WHEN invoke with count above 0 THEN sets high score`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            highScores = UserDefaults.signedInUser().highScores.copy(puzzleStreak = 0),
            puzzleStreak = User.PuzzleStreak(currentCount = 5, lastPuzzleId = 42)
        )
        fakeUserRepository.update(user)

        // When
        val isNewHighScore = underTest(5)

        // Then
        assertTrue(isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(5, highScores.puzzleStreak)
        }
    }

    @Test
    fun `GIVEN zero streak WHEN invoke THEN does not set high score`() = runTest {
        // Given
        val currentHighScore = UserDefaults.HIGH_SCORE_STREAK
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
        )
        fakeUserRepository.update(user)

        // When
        val isNewHighScore = underTest(0)

        // Then
        assertFalse(isNewHighScore)
        fakeUserRepository.get().apply {
            assertEquals(currentHighScore, highScores.puzzleStreak)
        }
    }
}
