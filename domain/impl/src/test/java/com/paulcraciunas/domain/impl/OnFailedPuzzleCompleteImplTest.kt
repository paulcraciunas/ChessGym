package com.paulcraciunas.domain.impl

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class OnFailedPuzzleCompleteImplTest {
    private val userRepository = FakeUserRepository()
    private val underTest = OnFailedPuzzleCompleteImpl(userRepository)

    @Test
    fun `GIVEN puzzle in failed list WHEN invoke THEN removes puzzle from list`() = runTest {
        // Given
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId, 99, 123))
        userRepository.update(user)

        // When
        underTest(puzzleId)

        // Then
        val updatedUser = userRepository.get()
        assertFalse(updatedUser.failedPuzzles.contains(puzzleId))
        assertEquals(2, updatedUser.failedPuzzles.size)
    }

    @Test
    fun `GIVEN puzzle completed WHEN invoke THEN increments puzzles solved`() = runTest {
        // Given
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId))
        userRepository.update(user)
        val initialSolved = user.statistics.puzzlesSolved

        // When
        underTest(puzzleId)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(initialSolved + 1, updatedUser.statistics.puzzlesSolved)
    }

    @Test
    fun `GIVEN multiple completions WHEN invoke multiple times THEN increments correctly`() = runTest {
        // Given
        val puzzleIds = listOf(42, 99, 123)
        val user = UserDefaults.signedInUser().copy(failedPuzzles = puzzleIds)
        userRepository.update(user)
        val initialSolved = user.statistics.puzzlesSolved

        // When
        puzzleIds.forEach { underTest(it) }

        // Then
        val updatedUser = userRepository.get()
        assertEquals(initialSolved + 3, updatedUser.statistics.puzzlesSolved)
        assertTrue(updatedUser.failedPuzzles.isEmpty())
    }

    @Test
    fun `GIVEN puzzle not in failed list WHEN invoke THEN does not affect list`() = runTest {
        // Given
        val existingIds = listOf(99, 123)
        val user = UserDefaults.signedInUser().copy(failedPuzzles = existingIds)
        userRepository.update(user)

        // When - try to remove puzzle that's not in the list
        underTest(42)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(2, updatedUser.failedPuzzles.size)
        assertTrue(updatedUser.failedPuzzles.containsAll(existingIds))
    }

    @Test
    fun `GIVEN empty failed list WHEN invoke THEN still increments solved count`() = runTest {
        // Given
        val user = UserDefaults.signedInUser().copy(failedPuzzles = emptyList())
        userRepository.update(user)
        val initialSolved = user.statistics.puzzlesSolved

        // When
        underTest(42)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(initialSolved + 1, updatedUser.statistics.puzzlesSolved)
    }

    @Test
    fun `GIVEN user data WHEN invoke THEN preserves other user fields`() = runTest {
        // Given
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId))
        userRepository.update(user)

        // When
        underTest(puzzleId)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(user.profile, updatedUser.profile)
        assertEquals(user.ratings, updatedUser.ratings)
        assertEquals(user.highScores, updatedUser.highScores)
        assertEquals(user.authentication, updatedUser.authentication)
        assertEquals(user.statistics.puzzlesPlayed, updatedUser.statistics.puzzlesPlayed)
        assertEquals(user.statistics.totalTimeSpent, updatedUser.statistics.totalTimeSpent)
    }

    @Test
    fun `GIVEN duplicate puzzle IDs WHEN invoke once THEN removes only one occurrence`() = runTest {
        // Given - list with duplicate (shouldn't happen normally but test edge case)
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId, 99, puzzleId))
        userRepository.update(user)

        // When
        underTest(puzzleId)

        // Then - filter removes all occurrences
        val updatedUser = userRepository.get()
        assertFalse(updatedUser.failedPuzzles.contains(puzzleId))
        assertEquals(1, updatedUser.failedPuzzles.size)
    }
}
