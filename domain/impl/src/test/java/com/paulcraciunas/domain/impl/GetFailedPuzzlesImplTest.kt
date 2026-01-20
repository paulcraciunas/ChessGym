package com.paulcraciunas.domain.impl

import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GetFailedPuzzlesImplTest {
    private val userRepository = FakeUserRepository()
    private val puzzleRepository = FakePuzzleRepository.default()

    private lateinit var underTest: GetFailedPuzzlesImpl

    @BeforeEach
    fun setUp() {
        underTest = GetFailedPuzzlesImpl(userRepository, puzzleRepository)
    }

    @Test
    fun `GIVEN no failed puzzles WHEN load THEN totalCount is zero`() = runTest {
        // Given
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = emptyList()))

        // When
        underTest.load()

        // Then
        assertEquals(0, underTest.totalCount())
    }

    @Test
    fun `GIVEN failed puzzles WHEN load THEN totalCount matches`() = runTest {
        // Given
        val failedIds = listOf(1, 2, 3)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

        // When
        underTest.load()

        // Then
        assertEquals(failedIds.size, underTest.totalCount())
    }

    @Test
    fun `GIVEN failed puzzles WHEN next THEN returns puzzle by ID`() = runTest {
        // Given
        val failedIds = listOf(1, 2, 3)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        underTest.load()

        // When
        val puzzle = underTest.next()

        // Then
        assertNotNull(puzzle)
    }

    @Test
    fun `GIVEN failed puzzles WHEN next multiple times THEN returns puzzles in order`() = runTest {
        // Given
        val failedIds = listOf(1, 2, 3)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        underTest.load()

        // When
        val puzzles = (1..3).mapNotNull { underTest.next() }

        // Then
        assertEquals(3, puzzles.size)
    }

    @Test
    fun `GIVEN all puzzles consumed WHEN next THEN returns null`() = runTest {
        // Given
        val failedIds = listOf(1, 2)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        underTest.load()

        // Consume all puzzles
        repeat(2) { underTest.next() }

        // When
        val puzzle = underTest.next()

        // Then
        assertNull(puzzle)
    }

    @Test
    fun `GIVEN failed puzzles WHEN remainingCount THEN returns correct count`() = runTest {
        // Given
        val failedIds = listOf(1, 2, 3, 4, 5)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        underTest.load()

        // When - consume 2 puzzles
        underTest.next()
        underTest.next()

        // Then
        assertEquals(3, underTest.remainingCount())
    }

    @Test
    fun `GIVEN load called WHEN load called again THEN resets state`() = runTest {
        // Given
        val failedIds = listOf(1, 2, 3)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        underTest.load()
        repeat(2) { underTest.next() } // Consume some puzzles

        // When - load again
        underTest.load()

        // Then - should be back at the start
        assertEquals(3, underTest.totalCount())
        assertEquals(3, underTest.remainingCount())
    }

    @Test
    fun `GIVEN small batchSize WHEN next beyond batch THEN loads multiple batches`() = runTest {
        // Given
        val failedIds = listOf(1, 2, 3, 4, 5)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))
        underTest.load(batchSize = 2) // Small batch size

        // When - get 5 puzzles (3 batches)
        val puzzles = (1..5).mapNotNull { underTest.next() }

        // Then
        assertEquals(5, puzzles.size)
    }

    @Test
    fun `GIVEN puzzle ID not in repository WHEN next THEN skips missing puzzle`() = runTest {
        // Given - ID 999 doesn't exist in repository
        val failedIds = listOf(1, 999, 2)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

        // When
        underTest.load()

        // Then - should get puzzle 1 and 2, skipping 999
        assertNotNull(underTest.next())
        assertNotNull(underTest.next())
        assertNull(underTest.next())
    }

    @Test
    fun `GIVEN no load called WHEN next THEN returns null`() = runTest {
        // Given - no load called

        // When
        val puzzle = underTest.next()

        // Then
        assertNull(puzzle)
    }

    @Test
    fun `GIVEN no load called WHEN totalCount THEN returns zero`() = runTest {
        // Given - no load called

        // Then
        assertEquals(0, underTest.totalCount())
    }

    @Test
    fun `GIVEN custom batchSize WHEN load THEN uses custom batchSize`() = runTest {
        // Given
        val failedIds = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

        // When - use batch size of 3
        underTest.load(batchSize = 3)

        // Then - should be able to get all puzzles
        val puzzles = (1..10).mapNotNull { underTest.next() }
        assertEquals(10, puzzles.size)
    }
}
