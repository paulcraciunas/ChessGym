package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.domain.api.puzzles.PuzzleGenerationException
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetBufferedPuzzleSeriesImplTest {
    private val repository = FakePuzzleRepository.default(ratingStart = RATING_START, increment = INCREMENT)
    private val fakeRandom = FixedRandomFactory(returnValue = INCREMENT)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val underTest = GetBufferedPuzzleSeriesImpl(
        getPuzzleByRating = GetPuzzleByRatingImpl(repository, FakeAppSettingsRepository.default()),
        randomFactory = fakeRandom,
        ioDispatcher = testDispatcher,
    )

    @Nested
    internal inner class BasicEmissions {
        @Test
        fun `GIVEN valid puzzles WHEN collecting execute THEN emits first puzzle at starting rating`() = runTest(testDispatcher) {
            // When
            val puzzles = underTest.execute(
                bufferSize = BATCH_SIZE,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(1).toList()

            // Then
            assertEquals(1, puzzles.size)
            assertEquals(RATING_START, puzzles.first().rating)
        }

        @Test
        fun `GIVEN valid puzzles WHEN collecting multiple THEN emits puzzles with increasing ratings`() = runTest(testDispatcher) {
            // When
            val puzzles = underTest.execute(
                bufferSize = BATCH_SIZE,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(5).toList()

            // Then
            assertEquals(5, puzzles.size)
            puzzles.forEachIndexed { index, puzzle ->
                assertEquals(RATING_START + index * INCREMENT, puzzle.rating)
            }
        }

        @Test
        fun `GIVEN valid puzzles WHEN collecting beyond buffer THEN continues emitting`() = runTest(testDispatcher) {
            // When - collect more than buffer size
            val puzzles = underTest.execute(
                bufferSize = 3,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(7).toList()

            // Then
            assertEquals(7, puzzles.size)
        }
    }

    @Nested
    internal inner class RatingIncrement {
        @Test
        fun `GIVEN custom increment WHEN collecting THEN uses randomFactory for rating steps`() = runTest(testDispatcher) {
            // Given
            val customIncrement = 100
            fakeRandom.returnValue = customIncrement
            val customRepository = FakePuzzleRepository.default(ratingStart = RATING_START, increment = customIncrement)
            val customUnderTest = GetBufferedPuzzleSeriesImpl(
                getPuzzleByRating = GetPuzzleByRatingImpl(customRepository, FakeAppSettingsRepository.default()),
                randomFactory = fakeRandom,
                ioDispatcher = testDispatcher,
            )

            // When
            val puzzles = customUnderTest.execute(
                bufferSize = BATCH_SIZE,
                ratingStart = RATING_START,
                increment = customIncrement,
            ).take(3).toList()

            // Then
            assertEquals(RATING_START, puzzles[0].rating)
            assertEquals(RATING_START + customIncrement, puzzles[1].rating)
            assertEquals(RATING_START + 2 * customIncrement, puzzles[2].rating)
        }

        @Test
        fun `GIVEN different ratingStart WHEN collecting THEN starts from specified rating`() = runTest(testDispatcher) {
            // Given
            val newRatingStart = 1500

            // When
            val puzzles = underTest.execute(
                bufferSize = BATCH_SIZE,
                ratingStart = newRatingStart,
                increment = INCREMENT,
            ).take(1).toList()

            // Then
            assertEquals(newRatingStart, puzzles.first().rating)
        }
    }

    @Nested
    internal inner class ErrorRecovery {
        @Test
        fun `GIVEN transient error WHEN collecting THEN recovers and continues producing`() = runTest(testDispatcher) {
            // Given
            val failingPuzzleByRating = FailOnceGetPuzzleByRating(
                delegate = GetPuzzleByRatingImpl(repository, FakeAppSettingsRepository.default()),
                failAtCallIndex = 2,
            )
            val transientUnderTest = GetBufferedPuzzleSeriesImpl(
                getPuzzleByRating = failingPuzzleByRating,
                randomFactory = fakeRandom,
                ioDispatcher = testDispatcher,
            )

            // When
            val puzzles = transientUnderTest.execute(
                bufferSize = BATCH_SIZE,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(4).toList()

            // Then - should have recovered from the single failure
            assertEquals(4, puzzles.size)
        }

        @Test
        fun `GIVEN 3 consecutive failures WHEN collecting THEN throws PuzzleGenerationException`() = runTest(testDispatcher) {
            // Given
            val failingUnderTest = GetBufferedPuzzleSeriesImpl(
                getPuzzleByRating = FailAlwaysGetPuzzleByRating(),
                randomFactory = fakeRandom,
                ioDispatcher = testDispatcher,
            )

            // Then
            assertThrows<PuzzleGenerationException> {
                failingUnderTest.execute(
                    bufferSize = BATCH_SIZE,
                    ratingStart = RATING_START,
                    increment = INCREMENT,
                ).take(1).toList()
            }
        }

        @Test
        fun `GIVEN 2 consecutive failures before success WHEN collecting THEN recovers`() = runTest(testDispatcher) {
            // Given - fails on calls 1 and 2, succeeds on 3+
            val failingPuzzleByRating = FailNTimesGetPuzzleByRating(
                delegate = GetPuzzleByRatingImpl(repository, FakeAppSettingsRepository.default()),
                failCount = 2,
            )
            val recoveryUnderTest = GetBufferedPuzzleSeriesImpl(
                getPuzzleByRating = failingPuzzleByRating,
                randomFactory = fakeRandom,
                ioDispatcher = testDispatcher,
            )

            // When
            val puzzles = recoveryUnderTest.execute(
                bufferSize = BATCH_SIZE,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(2).toList()

            // Then
            assertEquals(2, puzzles.size)
        }

        @Test
        fun `GIVEN intermittent failures WHEN collecting THEN resets failure counter on success`() = runTest(testDispatcher) {
            // Given - fails on every 3rd call but recovers
            val failingPuzzleByRating = FailEveryNthGetPuzzleByRating(
                delegate = GetPuzzleByRatingImpl(repository, FakeAppSettingsRepository.default()),
                failEveryN = 3,
            )
            val intermittentUnderTest = GetBufferedPuzzleSeriesImpl(
                getPuzzleByRating = failingPuzzleByRating,
                randomFactory = fakeRandom,
                ioDispatcher = testDispatcher,
            )

            // When - collect enough to trigger multiple failures
            val puzzles = intermittentUnderTest.execute(
                bufferSize = BATCH_SIZE,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(5).toList()

            // Then - consecutive failure count resets on success, so no crash
            assertEquals(5, puzzles.size)
        }
    }

    @Nested
    internal inner class Cancellation {
        @Test
        fun `GIVEN collecting flow WHEN cancelled THEN stops producing`() = runTest(testDispatcher) {
            // Given
            val emissions = mutableListOf<Puzzle>()

            val job = backgroundScope.launch {
                underTest.execute(
                    bufferSize = BATCH_SIZE,
                    ratingStart = RATING_START,
                    increment = INCREMENT,
                ).collect { emissions.add(it) }
            }

            runCurrent()
            val countBeforeCancel = emissions.size
            assertTrue(countBeforeCancel > 0)

            // When
            job.cancel()
            runCurrent()

            // Then - no further growth
            assertEquals(countBeforeCancel, emissions.size)
        }
    }

    @Nested
    internal inner class BufferBehavior {
        @Test
        fun `GIVEN small buffer WHEN collecting THEN still produces all requested items`() = runTest(testDispatcher) {
            // When
            val puzzles = underTest.execute(
                bufferSize = 2,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(10).toList()

            // Then
            assertEquals(10, puzzles.size)
        }

        @Test
        fun `GIVEN large buffer WHEN collecting THEN still produces correctly`() = runTest(testDispatcher) {
            // When
            val puzzles = underTest.execute(
                bufferSize = 20,
                ratingStart = RATING_START,
                increment = INCREMENT,
            ).take(5).toList()

            // Then
            assertEquals(5, puzzles.size)
        }
    }

    @Nested
    internal inner class EmptyRepository {
        @Test
        fun `GIVEN no puzzles in repository WHEN collecting THEN throws PuzzleGenerationException`() = runTest(testDispatcher) {
            // Given
            repository.clear()

            // Then
            assertThrows<PuzzleGenerationException> {
                underTest.execute(
                    bufferSize = BATCH_SIZE,
                    ratingStart = RATING_START,
                    increment = INCREMENT,
                ).take(1).toList()
            }
        }
    }

    private companion object {
        const val RATING_START = 1200
        const val INCREMENT = 50
        const val BATCH_SIZE = 10
    }
}

private class FailOnceGetPuzzleByRating(
    private val delegate: GetPuzzleByRating,
    private val failAtCallIndex: Int,
) : GetPuzzleByRating {
    private var callCount = 0

    override suspend fun invoke(targetRating: Int): Puzzle {
        callCount++
        if (callCount == failAtCallIndex) {
            throw RuntimeException("Transient failure")
        }
        return delegate(targetRating)
    }
}

private class FailAlwaysGetPuzzleByRating : GetPuzzleByRating {
    override suspend fun invoke(targetRating: Int): Puzzle {
        throw RuntimeException("Persistent failure")
    }
}

private class FailNTimesGetPuzzleByRating(
    private val delegate: GetPuzzleByRating,
    private val failCount: Int,
) : GetPuzzleByRating {
    private var callCount = 0

    override suspend fun invoke(targetRating: Int): Puzzle {
        callCount++
        if (callCount <= failCount) {
            throw RuntimeException("Failure #$callCount")
        }
        return delegate(targetRating)
    }
}

private class FailEveryNthGetPuzzleByRating(
    private val delegate: GetPuzzleByRating,
    private val failEveryN: Int,
) : GetPuzzleByRating {
    private var callCount = 0

    override suspend fun invoke(targetRating: Int): Puzzle {
        callCount++
        if (callCount % failEveryN == 0) {
            throw RuntimeException("Intermittent failure at call #$callCount")
        }
        return delegate(targetRating)
    }
}
