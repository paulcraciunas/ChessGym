package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class OnStreakPuzzleCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = OnStreakPuzzleCompleteImpl(fakeUserRepository)

    @Test
    fun `GIVEN no active streak WHEN invoke THEN increments streak to 1`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
            )
        )
        val timeSpentMillis = 42L
        fakeUserRepository.update(user)

        // When
        underTest(timeSpentMillis)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(1, ratings.puzzleStreak.currentCount)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + timeSpentMillis, statistics.totalTimeSpent)
        }
    }

    @Test
    fun `GIVEN active streak of 5 WHEN invoke THEN increments streak to 6`() = runTest {
        // Given
        val initialCount = 5
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = initialCount, lastPuzzleId = 41)
            )
        )
        val timeSpentMillis = 42L
        fakeUserRepository.update(user)

        // When
        underTest(timeSpentMillis)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(initialCount + 1, ratings.puzzleStreak.currentCount)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + timeSpentMillis, statistics.totalTimeSpent)
        }
    }

    @Test
    fun `GIVEN streak in progress WHEN invoke THEN clears lastPuzzleId`() = runTest {
        // Given
        val oldPuzzleId = 100
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = 3, lastPuzzleId = oldPuzzleId)
            )
        )
        val timeSpentMillis = 42L
        fakeUserRepository.update(user)

        // When
        underTest(timeSpentMillis)

        // Then
        fakeUserRepository.get().apply {
            assertNull(ratings.puzzleStreak.lastPuzzleId)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + timeSpentMillis, statistics.totalTimeSpent)
        }
    }

    @Test
    fun `GIVEN large streak WHEN invoke THEN continues incrementing`() = runTest {
        // Given
        val largeStreak = 100
        val user = UserDefaults.signedInUser().copy(
            ratings = UserDefaults.signedInUser().ratings.copy(
                puzzleStreak = User.PuzzleStreak(currentCount = largeStreak, lastPuzzleId = 999)
            )
        )
        val timeSpentMillis = 42L
        fakeUserRepository.update(user)

        // When
        underTest(timeSpentMillis)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(largeStreak + 1, ratings.puzzleStreak.currentCount)
            assertEquals(UserDefaults.STATISTICS_TIME_PLAYED + timeSpentMillis, statistics.totalTimeSpent)
            assertNull(ratings.puzzleStreak.lastPuzzleId)
        }
    }
}
