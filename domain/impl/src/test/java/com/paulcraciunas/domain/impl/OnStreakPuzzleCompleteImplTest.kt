package com.paulcraciunas.domain.impl

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OnStreakPuzzleCompleteImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = OnStreakPuzzleCompleteImpl(fakeUserRepository)

    @Test
    fun `GIVEN no active streak WHEN invoke THEN increments streak to 1`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = 0, lastPuzzleId = null)
        )
        fakeUserRepository.update(user)
        val puzzleId = 42

        // When
        underTest(puzzleId)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(1, puzzleStreak.currentCount)
            assertEquals(puzzleId, puzzleStreak.lastPuzzleId)
        }
    }

    @Test
    fun `GIVEN active streak of 5 WHEN invoke THEN increments streak to 6`() = runTest {
        // Given
        val initialCount = 5
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = initialCount, lastPuzzleId = 41)
        )
        fakeUserRepository.update(user)
        val newPuzzleId = 42

        // When
        underTest(newPuzzleId)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(initialCount + 1, puzzleStreak.currentCount)
            assertEquals(newPuzzleId, puzzleStreak.lastPuzzleId)
        }
    }

    @Test
    fun `GIVEN streak in progress WHEN invoke THEN updates lastPuzzleId`() = runTest {
        // Given
        val oldPuzzleId = 100
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = 3, lastPuzzleId = oldPuzzleId)
        )
        fakeUserRepository.update(user)
        val newPuzzleId = 200

        // When
        underTest(newPuzzleId)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(newPuzzleId, puzzleStreak.lastPuzzleId)
        }
    }

    @Test
    fun `GIVEN large streak WHEN invoke THEN continues incrementing`() = runTest {
        // Given
        val largeStreak = 100
        val user = UserDefaults.signedInUser().copy(
            puzzleStreak = User.PuzzleStreak(currentCount = largeStreak, lastPuzzleId = 999)
        )
        fakeUserRepository.update(user)
        val newPuzzleId = 1000

        // When
        underTest(newPuzzleId)

        // Then
        fakeUserRepository.get().apply {
            assertEquals(largeStreak + 1, puzzleStreak.currentCount)
            assertEquals(newPuzzleId, puzzleStreak.lastPuzzleId)
        }
    }
}
