package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.RandomFactory
import com.paulcraciunas.puzzles.api.FakePuzzleRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetPuzzleSeriesImplTest {
    private val repository = FakePuzzleRepository.default(ratingStart = RATING_START, increment = INCREMENT)

    private val underTest = GetPuzzleSeriesImpl(repository, FakeRandom())

    @Test
    fun `WHEN puzzle repository is empty THEN no puzzles are returned`() = runBlocking {
        // GIVEN
        repository.clear()

        // WHEN
        val puzzles = underTest(from = 1200)

        // THEN
        assertEquals(0, puzzles.size)
    }

    @Test
    fun `WHEN puzzle repository does not contain desired puzzle THEN remaining puzzles are returned`() = runBlocking {
        // WHEN
        val puzzles = underTest(from = 1200)

        // THEN
        assertEquals(FakePuzzleRepository.SIZE, puzzles.size)
    }

    @Test
    fun `WHEN puzzle repository contains puzzles THEN return them`() = runBlocking {
        val count = 5

        val puzzles = underTest(count = count, from = 1200)

        assertEquals(count, puzzles.size)
    }

    private class FakeRandom : RandomFactory {
        override fun nextInt(from: Int, to: Int): Int = INCREMENT
    }

    private companion object {
        const val RATING_START = 1200
        const val INCREMENT = 50 // Chosen at random
    }
}
