package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.RandomFactory
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetPuzzleSeriesImplTest {
    private val repository = FakePuzzleRepository.default(ratingStart = RATING_START, increment = INCREMENT)
    private val fakeRandom = FakeRandom()

    private val underTest = GetPuzzleSeriesImpl(repository, fakeRandom)

    @Test
    fun `GIVEN empty repository WHEN invoke THEN no puzzles are returned`() = runBlocking {
        // Given
        repository.clear()

        // When
        val puzzles = underTest(from = RATING_START)

        // Then
        assertEquals(0, puzzles.size)
    }

    @Test
    fun `GIVEN repository with puzzles WHEN invoke with count THEN returns requested count`() = runBlocking {
        // Given
        val count = 5

        // When
        val puzzles = underTest(count = count, from = RATING_START)

        // Then
        assertEquals(count, puzzles.size)
    }

    @Test
    fun `GIVEN repository WHEN invoke exceeds available puzzles THEN returns all available`() = runBlocking {
        // When
        val puzzles = underTest(from = RATING_START)

        // Then
        assertEquals(FakePuzzleRepository.SIZE, puzzles.size)
    }

    @Test
    fun `GIVEN custom increment WHEN invoke THEN uses increment parameter for rating progression`() = runBlocking {
        // Given
        val customIncrement = 100
        fakeRandom.returnValue = customIncrement
        
        // When - request 3 puzzles with custom increment
        underTest(count = 3, increment = customIncrement, from = RATING_START)

        // Then - verify random was called with correct increment bounds
        assertEquals(3, fakeRandom.callCount)
        fakeRandom.capturedBounds.forEach { (from, to) ->
            assertEquals(1, from)
            assertEquals(customIncrement, to)
        }
    }

    @Test
    fun `GIVEN default increment WHEN invoke without increment THEN uses default increment`() = runBlocking {
        // Given
        val defaultIncrement = 60 // GetPuzzleSeries.INCREMENT
        fakeRandom.returnValue = INCREMENT

        // When
        underTest(count = 2, from = RATING_START)

        // Then - verify random was called with default increment bounds
        fakeRandom.capturedBounds.forEach { (from, to) ->
            assertEquals(1, from)
            assertEquals(defaultIncrement, to)
        }
    }

    @Test
    fun `GIVEN puzzles WHEN invoke THEN returns puzzles with increasing ratings`() = runBlocking {
        // Given
        fakeRandom.returnValue = INCREMENT

        // When
        val puzzles = underTest(count = 5, increment = INCREMENT, from = RATING_START)

        // Then - verify ratings increase
        val ratings = puzzles.map { it.rating }
        assertEquals(listOf(
            RATING_START,
            RATING_START + INCREMENT,
            RATING_START + INCREMENT * 2,
            RATING_START + INCREMENT * 3,
            RATING_START + INCREMENT * 4
        ), ratings)
    }

    private class FakeRandom : RandomFactory {
        var returnValue: Int = INCREMENT
        var callCount: Int = 0
            private set
        val capturedBounds = mutableListOf<Pair<Int, Int>>()

        override fun nextInt(from: Int, to: Int): Int {
            callCount++
            capturedBounds.add(from to to)
            return returnValue
        }
    }

    private companion object {
        const val RATING_START = 1200
        const val INCREMENT = 50
    }
}
