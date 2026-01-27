package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class GetBufferedPuzzleSeriesImplTest {
    private val repository = FakePuzzleRepository.default(ratingStart = RATING_START, increment = INCREMENT)
    private val fakeRandom = FixedRandomFactory(returnValue = INCREMENT)

    private val underTest = GetBufferedPuzzleSeriesImpl(
        getPuzzleByRating = GetPuzzleByRatingImpl(repository, FakeAppSettingsRepository.default()),
        randomFactory = fakeRandom
    )

    @Test
    fun `GIVEN initialized WHEN next without invoke THEN returns null`() = runBlocking {
        // Given - no invoke called

        // When
        val puzzle = underTest.next()

        // Then
        assertNull(puzzle)
    }

    @Test
    fun `GIVEN invoke called WHEN next THEN returns first puzzle`() = runBlocking {
        // Given
        underTest(batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)

        // When
        val puzzle = underTest.next()

        // Then
        assertNotNull(puzzle)
        assertEquals(RATING_START, puzzle!!.rating)
    }

    @Test
    fun `GIVEN invoke called WHEN next multiple times THEN returns puzzles with increasing ratings`() = runBlocking {
        // Given
        underTest(batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)

        // When
        val puzzles = (1..5).mapNotNull { underTest.next() }

        // Then
        assertEquals(5, puzzles.size)
        puzzles.forEachIndexed { index, puzzle ->
            assertEquals(RATING_START + index * INCREMENT, puzzle.rating)
        }
    }

    @Test
    fun `GIVEN batchSize puzzles consumed WHEN next THEN loads next batch`() = runBlocking {
        // Given
        val batchSize = 3
        underTest(batchSize = batchSize, ratingStart = RATING_START, increment = INCREMENT)

        // Consume first batch
        repeat(batchSize) { underTest.next() }

        // When - request one more (triggers next batch load)
        val puzzle = underTest.next()

        // Then
        assertNotNull(puzzle)
        assertEquals(RATING_START + batchSize * INCREMENT, puzzle!!.rating)
    }

    @Test
    fun `GIVEN no more puzzles available WHEN next THEN returns null`() = runBlocking {
        // Given
        repository.clear()
        underTest(batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)

        // When
        val puzzle = underTest.next()

        // Then
        assertNull(puzzle)
    }

    @Test
    fun `GIVEN invoke called WHEN invoke called again THEN resets and returns from beginning`() = runBlocking {
        // Given
        underTest(batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        repeat(5) { underTest.next() } // Consume some puzzles

        // When - invoke again
        underTest(batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)
        val puzzle = underTest.next()

        // Then - should be back at the start
        assertNotNull(puzzle)
        assertEquals(RATING_START, puzzle!!.rating)
    }

    @Test
    fun `GIVEN invoke with different ratingStart WHEN next THEN returns puzzles from new rating`() = runBlocking {
        // Given
        val newRatingStart = 1500
        underTest(batchSize = BATCH_SIZE, ratingStart = newRatingStart, increment = INCREMENT)

        // When
        val puzzle = underTest.next()

        // Then
        assertNotNull(puzzle)
        assertEquals(newRatingStart, puzzle!!.rating)
    }

    @Test
    fun `GIVEN invoke with custom increment WHEN next multiple THEN uses custom increment`() = runBlocking {
        // Given
        val customIncrement = 100
        fakeRandom.returnValue = customIncrement
        underTest(batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = customIncrement)

        // When
        val first = underTest.next()
        val second = underTest.next()

        // Then
        assertNotNull(first)
        assertNotNull(second)
        assertEquals(RATING_START, first!!.rating)
        assertEquals(RATING_START + customIncrement, second!!.rating)
    }

    @Test
    fun `GIVEN empty repository WHEN next THEN returns null`() = runBlocking {
        // Given
        repository.clear()
        underTest(batchSize = BATCH_SIZE, ratingStart = RATING_START, increment = INCREMENT)

        // When
        val puzzle = underTest.next()

        // Then
        assertNull(puzzle)
    }

    @Test
    fun `GIVEN small batchSize WHEN next beyond batch THEN loads multiple batches`() = runBlocking {
        // Given
        val batchSize = 2
        underTest(batchSize = batchSize, ratingStart = RATING_START, increment = INCREMENT)

        // When - get 5 puzzles (3 batches)
        val puzzles = (1..5).mapNotNull { underTest.next() }

        // Then
        assertEquals(5, puzzles.size)
    }

    private companion object {
        const val RATING_START = 1200
        const val INCREMENT = 50
        const val BATCH_SIZE = 10
    }
}
