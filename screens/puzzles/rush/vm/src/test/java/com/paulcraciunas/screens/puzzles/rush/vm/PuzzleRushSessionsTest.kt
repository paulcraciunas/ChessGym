package com.paulcraciunas.screens.puzzles.rush.vm

import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.NoPuzzleException
import com.paulcraciunas.domain.api.puzzles.PuzzleGenerationException
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.screens.data.NoOpNavigation
import com.paulcraciunas.screens.data.NoOpSolution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PuzzleRushSessionsTest {
    private val fakeSeries = object : GetBufferedPuzzleSeries {
        var result: Flow<Puzzle> = flow { }
        override fun execute(bufferSize: Int, ratingStart: Int, increment: Int): Flow<Puzzle> = result
    }
    private val underTest = PuzzleRushSessions(getBufferedPuzzleSeries = fakeSeries)

    @Test
    fun `GIVEN puzzle emitted WHEN invoked THEN produces BoardSession with correct rating and player`() = runTest {
        // Given
        val puzzle = buildPuzzle(rating = 1200)
        fakeSeries.result = flow { emit(puzzle) }

        // When
        val sessions = underTest.invoke().toList()

        // Then
        assertEquals(1, sessions.size)
        sessions.first().apply {
            assertEquals(NoOpNavigation, navigation)
            assertEquals(NoOpSolution, solution)
            assertEquals(1200, boardState().rating)
            assertEquals(Side.BLACK, boardState().player)
        }
    }

    @Test
    fun `GIVEN multiple puzzles emitted WHEN invoked THEN collects correct number of BoardSessions`() = runTest {
        // Given
        val puzzles = listOf(
            buildPuzzle(rating = 1200),
            buildPuzzle(rating = 1300),
            buildPuzzle(rating = 1400),
        )
        fakeSeries.result = flow { puzzles.forEach { emit(it) } }

        // When
        val sessions = underTest.invoke().toList()

        // Then
        assertEquals(3, sessions.size)
        assertEquals(1200, sessions[0].boardState().rating)
        assertEquals(1300, sessions[1].boardState().rating)
        assertEquals(1400, sessions[2].boardState().rating)
    }

    @Test
    fun `GIVEN NoPuzzleException cause WHEN invoked THEN flow completes normally`() = runTest {
        // Given
        fakeSeries.result = flow {
            throw PuzzleGenerationException(
                message = "No puzzles left",
                cause = NoPuzzleException("Exhausted"),
            )
        }

        // When
        val sessions = underTest.invoke().toList()

        // Then
        assertTrue(sessions.isEmpty())
    }

    @Test
    fun `GIVEN PuzzleGenerationException with other cause WHEN invoked THEN exception is rethrown`() = runTest {
        // Given
        val cause = RuntimeException("disk full")
        fakeSeries.result = flow {
            throw PuzzleGenerationException(
                message = "DB error",
                cause = cause,
            )
        }

        // When
        val thrown = runCatching { underTest.invoke().toList() }.exceptionOrNull()

        // Then
        assertNotNull(thrown)
        assertTrue(thrown is PuzzleGenerationException)
        assertEquals(cause, thrown?.cause)
    }

    @Test
    fun `GIVEN unexpected exception WHEN invoked THEN exception is rethrown`() = runTest {
        // Given
        fakeSeries.result = flow { throw RuntimeException("unexpected") }

        // When
        val thrown = runCatching { underTest.invoke().toList() }.exceptionOrNull()

        // Then
        assertNotNull(thrown)
        assertTrue(thrown is RuntimeException)
        assertEquals("unexpected", thrown?.message)
    }

    @Test
    fun `GIVEN empty flow WHEN invoked THEN collects nothing`() = runTest {
        // Given
        fakeSeries.result = flow { }

        // When
        val sessions = underTest.invoke().toList()

        // Then
        assertTrue(sessions.isEmpty())
    }

    private fun buildPuzzle(rating: Int = DEFAULT_RATING, moves: List<String> = DEFAULT_MOVES): Puzzle = RealGameFactory().builder()
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(moves)
        .buildPuzzle()

    private companion object {
        private const val DEFAULT_RATING = 1200
        private val DEFAULT_MOVES = listOf("e2e4", "e7e5", "g1f3", "b8c6")
    }
}
