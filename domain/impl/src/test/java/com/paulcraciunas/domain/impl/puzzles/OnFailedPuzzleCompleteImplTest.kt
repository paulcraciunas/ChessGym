package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class OnFailedPuzzleCompleteImplTest {
    private val userRepository = FakeUserRepository()
    private val updateAchievementProgress = UpdateAchievementProgressImpl(
        FakeAchievementNotificationManager(),
    )
    private val underTest = OnFailedPuzzleCompleteImpl(
        userRepository,
        updateAchievementProgress,
    )

    @Test
    fun `GIVEN puzzle in failed list WHEN invoke THEN removes puzzle from list`() = runTest {
        // Given
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId, 99, 123))
        userRepository.update(user)

        // When
        underTest(puzzleId, TIME_SPENT)

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
        underTest(puzzleId, TIME_SPENT)

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
        puzzleIds.forEach { underTest(it, TIME_SPENT) }

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

        // When
        underTest(42, TIME_SPENT)

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
        underTest(42, TIME_SPENT)

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
        underTest(puzzleId, TIME_SPENT)

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
    fun `GIVEN duplicate puzzle IDs WHEN invoke once THEN removes all occurrences`() = runTest {
        // Given
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId, 99, puzzleId))
        userRepository.update(user)

        // When
        underTest(puzzleId, TIME_SPENT)

        // Then
        val updatedUser = userRepository.get()
        assertFalse(updatedUser.failedPuzzles.contains(puzzleId))
        assertEquals(1, updatedUser.failedPuzzles.size)
    }

    @Test
    fun `GIVEN puzzle completed WHEN invoke THEN logs FailedPuzzleData history`() = runTest {
        // Given
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId))
        userRepository.update(user)

        // When
        underTest(puzzleId, TIME_SPENT)

        // Then
        val updatedUser = userRepository.get()
        assertEquals(1, updatedUser.history.size)
        val historyData = updatedUser.history.first().data
        assertTrue(historyData is User.HistoryItem.HistoryItemData.FailedPuzzleData)
        val failedData = historyData as User.HistoryItem.HistoryItemData.FailedPuzzleData
        assertEquals(1, failedData.puzzlesSolved)
        assertEquals(TIME_SPENT, failedData.timeSpent)
    }

    @Test
    fun `GIVEN multiple completions WHEN invoke multiple times THEN history items merge`() = runTest {
        // Given
        val puzzleIds = listOf(42, 99)
        val user = UserDefaults.signedInUser().copy(failedPuzzles = puzzleIds)
        userRepository.update(user)

        // When
        puzzleIds.forEach { underTest(it, TIME_SPENT) }

        // Then
        val updatedUser = userRepository.get()
        assertEquals(1, updatedUser.history.size)
        val failedData = updatedUser.history.first().data as User.HistoryItem.HistoryItemData.FailedPuzzleData
        assertEquals(2, failedData.puzzlesSolved)
        assertEquals(TIME_SPENT * 2, failedData.timeSpent)
    }

    @Test
    fun `GIVEN puzzle completed WHEN invoke THEN updates achievement-related statistics and progress`() = runTest {
        // Given
        val puzzleId = 42
        val user = UserDefaults.signedInUser().copy(failedPuzzles = listOf(puzzleId))
        userRepository.update(user)

        // When
        underTest(puzzleId, TIME_SPENT)

        // Then
        userRepository.get().apply {
            assertEquals(1, statistics.failedPuzzlesRedeemed)
            assertEquals(1L, achievements.progress[Achievement.FAILED_PUZZLES_REDEEMED.name])
        }
    }

    companion object {
        private const val TIME_SPENT = 5_000L
    }
}
