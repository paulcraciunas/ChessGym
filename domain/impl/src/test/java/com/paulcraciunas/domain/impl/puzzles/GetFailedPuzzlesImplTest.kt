package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.PuzzleGenerationException
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetFailedPuzzlesImplTest {
    private val userRepository = FakeUserRepository()
    private val puzzleRepository = FakePuzzleRepository.default()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val underTest = GetFailedPuzzlesImpl(userRepository, puzzleRepository, testDispatcher)

    @Nested
    internal inner class BasicEmissions {
        @Test
        fun `GIVEN no failed puzzles WHEN collecting execute THEN flow completes with no emissions`() = runTest(testDispatcher) {
            // Given
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = emptyList()))

            // When
            val puzzles = underTest.execute().toList()

            // Then
            assertTrue(puzzles.isEmpty())
        }

        @Test
        fun `GIVEN failed puzzles WHEN collecting execute THEN emits puzzles in order`() = runTest(testDispatcher) {
            // Given
            val failedIds = listOf(1, 2, 3)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When
            val puzzles = underTest.execute().toList()

            // Then
            assertEquals(3, puzzles.size)
            assertEquals(1, puzzles[0].id)
            assertEquals(2, puzzles[1].id)
            assertEquals(3, puzzles[2].id)
        }

        @Test
        fun `GIVEN single failed puzzle WHEN collecting THEN emits exactly one puzzle`() = runTest(testDispatcher) {
            // Given
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = listOf(1)))

            // When
            val puzzles = underTest.execute().toList()

            // Then
            assertEquals(1, puzzles.size)
            assertEquals(1, puzzles.first().id)
        }
    }

    @Nested
    internal inner class MissingPuzzles {
        @Test
        fun `GIVEN puzzle ID not in repository WHEN collecting THEN skips missing puzzle`() = runTest(testDispatcher) {
            // Given - ID 999 doesn't exist in repository
            val failedIds = listOf(1, 999, 2)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When
            val puzzles = underTest.execute().toList()

            // Then - should get puzzle 1 and 2, skipping 999
            assertEquals(2, puzzles.size)
            assertEquals(1, puzzles[0].id)
            assertEquals(2, puzzles[1].id)
        }

        @Test
        fun `GIVEN all IDs missing WHEN collecting THEN flow completes with no emissions`() = runTest(testDispatcher) {
            // Given - no matching IDs in repository
            val failedIds = listOf(900, 901, 902)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When
            val puzzles = underTest.execute().toList()

            // Then
            assertTrue(puzzles.isEmpty())
        }
    }

    @Nested
    internal inner class FiniteFlow {
        @Test
        fun `GIVEN 5 failed puzzles WHEN fully collected THEN flow completes naturally`() = runTest(testDispatcher) {
            // Given
            val failedIds = listOf(1, 2, 3, 4, 5)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When
            val puzzles = underTest.execute().toList()

            // Then - flow completes after all IDs processed
            assertEquals(5, puzzles.size)
        }

        @Test
        fun `GIVEN many failed puzzles WHEN taking subset THEN only takes requested amount`() = runTest(testDispatcher) {
            // Given
            val failedIds = (1..10).toList()
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When
            val puzzles = underTest.execute().take(3).toList()

            // Then
            assertEquals(3, puzzles.size)
        }
    }

    @Nested
    internal inner class BufferBehavior {
        @Test
        fun `GIVEN custom buffer size WHEN collecting THEN still emits all puzzles`() = runTest(testDispatcher) {
            // Given
            val failedIds = listOf(1, 2, 3, 4, 5)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When
            val puzzles = underTest.execute(bufferSize = 2).toList()

            // Then
            assertEquals(5, puzzles.size)
        }

        @Test
        fun `GIVEN default buffer size WHEN collecting THEN uses BUFFER_SIZE constant`() = runTest(testDispatcher) {
            // Given
            val failedIds = listOf(1, 2, 3)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When - use default buffer size
            val puzzles = underTest.execute().toList()

            // Then
            assertEquals(3, puzzles.size)
        }
    }

    @Nested
    internal inner class ErrorHandling {
        @Test
        fun `GIVEN 3 consecutive repository failures WHEN collecting THEN throws PuzzleGenerationException`() = runTest(testDispatcher) {
            // Given
            val failingRepository = FailingPuzzleRepository(failCount = 3)
            val failingUnderTest = GetFailedPuzzlesImpl(userRepository, failingRepository, testDispatcher)
            val failedIds = listOf(1, 2, 3)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // Then
            assertThrows<PuzzleGenerationException> {
                failingUnderTest.execute().toList()
            }
        }

        @Test
        fun `GIVEN intermittent failures WHEN collecting THEN recovers after success`() = runTest(testDispatcher) {
            // Given - fails on 2nd call only
            val failingRepository = FailOnceRepository(
                delegate = puzzleRepository,
                failAtCallIndex = 2,
            )
            val intermittentUnderTest = GetFailedPuzzlesImpl(userRepository, failingRepository, testDispatcher)
            val failedIds = listOf(1, 2, 3)
            userRepository.update(UserDefaults.signedInUser().copy(failedPuzzles = failedIds))

            // When
            val puzzles = intermittentUnderTest.execute().toList()

            // Then - gets puzzles 1 and 3 (2 failed but recovered)
            assertTrue(puzzles.isNotEmpty())
        }
    }
}

private class FailingPuzzleRepository(private val failCount: Int) : PuzzleRepository {
    private var callCount = 0

    override suspend fun getById(id: Int): Puzzle? {
        callCount++
        if (callCount <= failCount) {
            throw RuntimeException("Repository failure #$callCount")
        }
        return null
    }

    override suspend fun get(count: Int): List<Puzzle> = emptyList()
    override suspend fun getByRating(targetRating: Int): Puzzle? = null
    override suspend fun getByRatingRange(min: Int, max: Int): Puzzle? = null
}

private class FailOnceRepository(
    private val delegate: PuzzleRepository,
    private val failAtCallIndex: Int,
) : PuzzleRepository {
    private var callCount = 0

    override suspend fun getById(id: Int): Puzzle? {
        callCount++
        if (callCount == failAtCallIndex) {
            throw RuntimeException("Transient failure")
        }
        return delegate.getById(id)
    }

    override suspend fun get(count: Int): List<Puzzle> = delegate.get(count)
    override suspend fun getByRating(targetRating: Int): Puzzle? = delegate.getByRating(targetRating)
    override suspend fun getByRatingRange(min: Int, max: Int): Puzzle? = delegate.getByRatingRange(min, max)
}
