package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetFailedPuzzlesImplTest {
    private val userRepository = FakeUserRepository()
    private val puzzleRepository = FakePuzzleRepository.default()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val underTest = GetFailedPuzzlesImpl(userRepository, puzzleRepository, testDispatcher)

    @Test
    fun `GIVEN no failed puzzles WHEN load THEN totalCount is zero`() = runTest(testDispatcher) {
        // Given
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = emptyList()))

        // When
        val job = backgroundScope.launch {
            underTest.load(scope = this, bufferSize = GetFailedPuzzles.BUFFER_SIZE)
        }

        // Then
        assertEquals(0, underTest.totalCount())
        job.cancel()
    }

    @Test
    fun `GIVEN failed puzzles WHEN load THEN totalCount matches`() = runTest(testDispatcher) {
        // Given
        val failedIds = listOf(1, 2, 3)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

        // When
        val job = backgroundScope.launch {
            underTest.load(scope = this)
        }

        // Then
        assertEquals(failedIds.size, underTest.totalCount())
        job.cancel()
    }

    @Test
    fun `GIVEN failed puzzles WHEN next THEN returns puzzle by ID`() = runTest(testDispatcher) {
        // Given
        val failedIds = listOf(1, 2, 3)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

        // When
        val job = backgroundScope.launch {
            underTest.load(scope = this)
        }

        // Then
        (1..3).forEach {
            assertEquals(it, underTest.next()?.id)
        }
        job.cancel()
    }

    @Test
    fun `GIVEN all puzzles consumed WHEN next THEN returns null`() = runTest(testDispatcher) {
        // Given
        val failedIds = listOf(1, 2)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        val job = backgroundScope.launch {
            underTest.load(scope = this)
        }

        // Consume all puzzles
        repeat(2) { underTest.next() }

        // When
        val puzzle = underTest.next()

        // Then
        assertNull(puzzle)
        job.cancel()
    }

    @Test
    fun `GIVEN load called WHEN load called again THEN resets state`() = runTest(testDispatcher) {
        // Given
        val failedIds = listOf(1, 2, 3)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        val job = backgroundScope.launch {
            underTest.load(scope = this)
        }
        repeat(2) { underTest.next() } // Consume some puzzles
        job.cancel()

        // When - load again
        val secondJob = backgroundScope.launch {
            underTest.load(scope = this)
        }

        // Then - should be back at the start
        assertEquals(3, underTest.totalCount())
        secondJob.cancel()
    }

    @Test
    fun `GIVEN small batchSize WHEN next beyond batch THEN loads multiple batches`() = runTest(testDispatcher) {
        // Given
        val failedIds = listOf(1, 2, 3, 4, 5)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        val job = backgroundScope.launch {
            underTest.load(scope = this, bufferSize = 2) // Small buffer size
        }

        // When - get 5 puzzles (3 buffered)
        val puzzles = (1..5).mapNotNull { underTest.next() }

        // Then
        assertEquals(5, puzzles.size)
        job.cancel()
    }

    @Test
    fun `GIVEN puzzle ID not in repository WHEN next THEN skips missing puzzle`() = runTest(testDispatcher) {
        // Given - ID 999 doesn't exist in repository
        val failedIds = listOf(1, 999, 2)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

        // When
        val job = backgroundScope.launch {
            underTest.load(scope = this)
        }

        // Then - should get puzzle 1 and 2, skipping 999
        assertNotNull(underTest.next())
        assertNotNull(underTest.next())
        assertNull(underTest.next())
        job.cancel()
    }

    @Test
    fun `GIVEN no load called WHEN next THEN returns null`() = runTest(testDispatcher) {
        // Given - no load called

        // When
        val puzzle = underTest.next()

        // Then
        assertNull(puzzle)
    }

    @Test
    fun `GIVEN no load called WHEN totalCount THEN returns zero`() = runTest(testDispatcher) {
        // Given - no load called

        // Then
        assertEquals(0, underTest.totalCount())
    }

    @Test
    fun `GIVEN custom batchSize WHEN load THEN uses custom batchSize`() = runTest(testDispatcher) {
        // Given
        val failedIds = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

        // When - use batch size of 3
        val job = backgroundScope.launch {
            underTest.load(scope = this, bufferSize = 3)
        }

        // Then - should be able to get all puzzles
        val puzzles = (1..10).mapNotNull { underTest.next() }
        assertEquals(10, puzzles.size)
        job.cancel()
    }
}
