package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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

    @Test
    fun `GIVEN initialized WHEN next without start THEN returns null`() = runTest(testDispatcher) {
        // Given - no start called

        // Then
        assertThrows<ClosedReceiveChannelException> { underTest.next() }
    }

    @Test
    fun `GIVEN start called WHEN next THEN returns first puzzle`() = runTest(testDispatcher) {
        // Given
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }
        // When
        val puzzle = underTest.next()

        // Then
        assertNotNull(puzzle)
        assertEquals(RATING_START, puzzle.rating)

        job.cancel()
    }

    @Test
    fun `GIVEN start called WHEN next multiple times THEN returns puzzles with increasing ratings`() = runTest(testDispatcher) {
        // Given
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }

        // When
        val puzzles = (1..5).mapNotNull { underTest.next() }

        // Then
        assertEquals(5, puzzles.size)
        puzzles.forEachIndexed { index, puzzle ->
            assertEquals(RATING_START + index * INCREMENT, puzzle.rating)
        }

        job.cancel()
    }

    @Test
    fun `GIVEN batchSize puzzles consumed WHEN next THEN loads next batch`() = runTest(testDispatcher) {
        // Given
        val batchSize = 3
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = batchSize, ratingStart = RATING_START, increment = INCREMENT)
        }

        // Consume first batch
        repeat(batchSize) { underTest.next() }

        // When - request one more (triggers next batch load)
        val puzzle = underTest.next()

        // Then
        assertNotNull(puzzle)
        assertEquals(RATING_START + batchSize * INCREMENT, puzzle.rating)

        job.cancel()
    }

    @Test
    fun `GIVEN no more puzzles available WHEN next THEN returns null`() = runTest(testDispatcher) {
        // Given
        repository.clear()
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }

        // Then
        assertThrows<IllegalArgumentException> {  underTest.next() }

        job.cancel()
    }

    @Test
    fun `GIVEN start called WHEN start called again THEN resets and returns from beginning`() = runTest(testDispatcher) {
        // Given
        val job1 = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }
        repeat(5) { underTest.next() } // Consume some puzzles

        // When - start again
        val job2 = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }
        val puzzle = underTest.next()

        // Then - should be back at the start
        assertNotNull(puzzle)
        assertEquals(RATING_START, puzzle.rating)

        job1.cancel()
        job2.cancel()
    }

    @Test
    fun `GIVEN start with different ratingStart WHEN next THEN returns puzzles from new rating`() = runTest(testDispatcher) {
        // Given
        val newRatingStart = 1500
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = newRatingStart, increment = INCREMENT)
        }

        // When
        val puzzle = underTest.next()

        // Then
        assertNotNull(puzzle)
        assertEquals(newRatingStart, puzzle.rating)

        job.cancel()
    }

    @Test
    fun `GIVEN start with custom increment WHEN next multiple THEN uses custom increment`() = runTest(testDispatcher) {
        // Given
        val customIncrement = 100
        fakeRandom.returnValue = customIncrement
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = customIncrement)
        }

        // When
        val first = underTest.next()
        val second = underTest.next()

        // Then
        assertNotNull(first)
        assertNotNull(second)
        assertEquals(RATING_START, first.rating)
        assertEquals(RATING_START + customIncrement, second.rating)

        job.cancel()
    }

    @Test
    fun `GIVEN empty repository WHEN next THEN returns null`() = runTest(testDispatcher) {
        // Given
        repository.clear()
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }

        // Then
        assertThrows<IllegalArgumentException> { underTest.next() }

        job.cancel()
    }

    @Test
    fun `GIVEN small batchSize WHEN next beyond batch THEN loads multiple batches`() = runTest(testDispatcher) {
        // Given
        val batchSize = 2
        val job = backgroundScope.launch {
            underTest.start(scope = this, batchSize = batchSize, ratingStart = RATING_START, increment = INCREMENT)
        }

        // When - get 5 puzzles (3 batches)
        val puzzles = (1..5).mapNotNull { underTest.next() }

        // Then
        assertEquals(5, puzzles.size)

        job.cancel()
    }

    @Test
    fun `GIVEN transient error WHEN next THEN recovers and continues producing`() = runTest(testDispatcher) {
        // Given - repository that fails once then succeeds
        val failingPuzzleByRating = FailOnceGetPuzzleByRating(
            delegate = GetPuzzleByRatingImpl(repository, FakeAppSettingsRepository.default()),
            failAtCallIndex = 2,
        )
        val transientUnderTest = GetBufferedPuzzleSeriesImpl(
            getPuzzleByRating = failingPuzzleByRating,
            randomFactory = fakeRandom,
            ioDispatcher = testDispatcher,
        )
        val job = backgroundScope.launch {
            transientUnderTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }

        // When - get puzzles (should skip the failed one and continue)
        val puzzles = (1..4).mapNotNull { transientUnderTest.next() }

        // Then - should have recovered from the single failure
        assertEquals(4, puzzles.size)

        job.cancel()
    }

    @Test
    fun `GIVEN 3 consecutive errors WHEN next THEN channel closes and returns null`() = runTest(testDispatcher) {
        // Given - repository that fails persistently
        val failingPuzzleByRating = FailAlwaysGetPuzzleByRating()
        val failingUnderTest = GetBufferedPuzzleSeriesImpl(
            getPuzzleByRating = failingPuzzleByRating,
            randomFactory = fakeRandom,
            ioDispatcher = testDispatcher,
        )
        val job = backgroundScope.launch {
            failingUnderTest.start(scope = this, batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        }

        // Then
        assertThrows<RuntimeException>("Persistent failure") { failingUnderTest.next() }

        job.cancel()
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
