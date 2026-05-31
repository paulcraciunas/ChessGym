package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ScriptedOpponentTest {
    private val gameFactory = RealGameFactory()

    @Nested
    internal inner class CanPlay {
        @Test
        fun `GIVEN puzzle in progress WHEN canPlay THEN returns true`() {
            val puzzle = buildPuzzle()
            puzzle.start()
            puzzle.playNextMove()
            puzzle.play(Locus.e7, Locus.e5)

            val underTest = ScriptedOpponent().apply { load(puzzle) }

            assertTrue(underTest.canPlay())
        }

        @Test
        fun `GIVEN puzzle over WHEN canPlay THEN returns false`() {
            val puzzle = buildPuzzle()
            puzzle.start()
            puzzle.playNextMove()
            puzzle.resign()

            val underTest = ScriptedOpponent().apply { load(puzzle) }

            assertFalse(underTest.canPlay())
        }
    }

    @Nested
    internal inner class PlayNext {
        @Test
        fun `GIVEN puzzle in progress WHEN playNext THEN opponent move is played`() = runTest {
            val puzzle = buildPuzzle()
            puzzle.start()
            puzzle.playNextMove()
            puzzle.play(Locus.e7, Locus.e5)

            val underTest = ScriptedOpponent().apply { load(puzzle) }
            val result = underTest.playNext()

            assertTrue(result)
            assertNotNull(puzzle.board.at(Locus.f3))
        }

        @Test
        fun `GIVEN puzzle over WHEN playNext THEN returns false`() = runTest {
            val puzzle = buildPuzzle()
            puzzle.start()
            puzzle.playNextMove()
            puzzle.resign()

            val underTest = ScriptedOpponent().apply { load(puzzle) }
            val result = underTest.playNext()

            assertFalse(result)
        }

        @Test
        fun `GIVEN puzzle after wrong move WHEN playNext THEN returns false`() = runTest {
            val puzzle = buildPuzzle()
            puzzle.start()
            puzzle.playNextMove()
            puzzle.play(Locus.d7, Locus.d5)

            val underTest = ScriptedOpponent().apply { load(puzzle) }
            val result = underTest.playNext()

            assertFalse(result)
            assertEquals(Puzzle.State.Failed, puzzle.state)
        }

        @Test
        fun `GIVEN multi-move puzzle WHEN playing correct sequence THEN reaches success`() = runTest {
            val puzzle = buildPuzzle()
            puzzle.start()
            puzzle.playNextMove()

            val underTest = ScriptedOpponent().apply { load(puzzle) }

            puzzle.play(Locus.e7, Locus.e5)
            assertTrue(underTest.playNext())

            puzzle.play(Locus.b8, Locus.c6)
            assertFalse(underTest.playNext())
            assertEquals(Puzzle.State.Success, puzzle.state)
        }
    }

    @Nested
    internal inner class Lifecycle {
        @Test
        fun `GIVEN ScriptedOpponent WHEN prepare THEN completes without error`() = runTest {
            val underTest = ScriptedOpponent().apply { load(buildPuzzle().also { it.start(); it.playNextMove() }) }
            underTest.prepare()
        }

        @Test
        fun `GIVEN ScriptedOpponent WHEN shutdown THEN completes without error`() = runTest {
            val underTest = ScriptedOpponent().apply { load(buildPuzzle().also { it.start(); it.playNextMove() }) }
            underTest.shutdown()
        }
    }

    @Nested
    internal inner class NoOpOpponentTest {
        @Test
        fun `GIVEN NoOpOpponent WHEN canPlay THEN returns false`() {
            assertFalse(NoOpOpponent.canPlay())
        }

        @Test
        fun `GIVEN NoOpOpponent WHEN playNext THEN returns false`() = runTest {
            assertFalse(NoOpOpponent.playNext())
        }

        @Test
        fun `GIVEN NoOpOpponent WHEN prepare THEN completes without error`() = runTest {
            NoOpOpponent.prepare()
        }

        @Test
        fun `GIVEN NoOpOpponent WHEN shutdown THEN completes without error`() = runTest {
            NoOpOpponent.shutdown()
        }
    }

    private fun buildPuzzle(): Puzzle = gameFactory.builder()
        .withDefaultBoard()
        .withRating(1200)
        .withMoves(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
        .buildPuzzle()
}
