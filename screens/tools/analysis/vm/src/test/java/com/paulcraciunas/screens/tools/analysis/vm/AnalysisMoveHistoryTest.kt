package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AnalysisMoveHistoryTest {
    private val gameFactory = RealGameFactory()
    private val fenSerializer = FenSerializer(gameFactory)
    private val underTest = AnalysisMoveHistory(fenSerializer)

    @Nested
    internal inner class Initialization {
        @Test
        fun `GIVEN starting FEN WHEN initialized THEN has no moves`() {
            underTest.initialize(STARTING_FEN)

            assertEquals(0, underTest.currentIndex)
            assertEquals(0, underTest.totalMoves)
            assertTrue(underTest.isAtLatestPosition)
        }

        @Test
        fun `GIVEN starting FEN WHEN sideToMoveAt zero THEN white`() {
            underTest.initialize(STARTING_FEN)

            assertEquals(Side.WHITE, underTest.sideToMoveAt(0))
        }

        @Test
        fun `GIVEN black to move FEN WHEN sideToMoveAt zero THEN black`() {
            val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
            underTest.initialize(fen)

            assertEquals(Side.BLACK, underTest.sideToMoveAt(0))
        }
    }

    @Nested
    internal inner class RecordingMoves {
        @Test
        fun `GIVEN initialized WHEN move recorded THEN index advances`() {
            underTest.initialize(STARTING_FEN)

            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)

            assertEquals(1, underTest.currentIndex)
            assertEquals(1, underTest.totalMoves)
            assertTrue(underTest.isAtLatestPosition)
        }

        @Test
        fun `GIVEN move recorded WHEN sideToMoveAt 1 THEN black`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)

            assertEquals(Side.BLACK, underTest.sideToMoveAt(1))
        }

        @Test
        fun `GIVEN multiple moves WHEN recorded THEN total moves increases`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.recordMove(Locus(File.e, Rank.`7`), Locus(File.e, Rank.`5`), null)

            assertEquals(2, underTest.currentIndex)
            assertEquals(2, underTest.totalMoves)
        }
    }

    @Nested
    internal inner class NavigationMethods {
        @Test
        fun `GIVEN moves played WHEN previousMove THEN index decreases`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)

            underTest.previousMove()

            assertEquals(0, underTest.currentIndex)
            assertFalse(underTest.isAtLatestPosition)
        }

        @Test
        fun `GIVEN at start WHEN previousMove THEN stays at zero`() {
            underTest.initialize(STARTING_FEN)

            underTest.previousMove()

            assertEquals(0, underTest.currentIndex)
        }

        @Test
        fun `GIVEN navigated back WHEN nextMove THEN index increases`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.previousMove()

            underTest.nextMove()

            assertEquals(1, underTest.currentIndex)
            assertTrue(underTest.isAtLatestPosition)
        }

        @Test
        fun `GIVEN at end WHEN nextMove THEN stays at end`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)

            underTest.nextMove()

            assertEquals(1, underTest.currentIndex)
        }

        @Test
        fun `GIVEN multiple moves WHEN jumpToStart THEN index is zero`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.recordMove(Locus(File.e, Rank.`7`), Locus(File.e, Rank.`5`), null)

            underTest.jumpToStart()

            assertEquals(0, underTest.currentIndex)
        }

        @Test
        fun `GIVEN navigated back WHEN jumpToEnd THEN index is at last`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.recordMove(Locus(File.e, Rank.`7`), Locus(File.e, Rank.`5`), null)
            underTest.jumpToStart()

            underTest.jumpToEnd()

            assertEquals(2, underTest.currentIndex)
            assertTrue(underTest.isAtLatestPosition)
        }
    }

    @Nested
    internal inner class TruncateAndReconstruct {
        @Test
        fun `GIVEN navigated back WHEN truncate THEN future moves removed`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.recordMove(Locus(File.e, Rank.`7`), Locus(File.e, Rank.`5`), null)
            underTest.jumpToStart()

            underTest.truncate()

            assertEquals(0, underTest.totalMoves)
            assertEquals(0, underTest.currentIndex)
        }

        @Test
        fun `GIVEN navigated to middle WHEN truncate THEN keeps moves up to index`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.recordMove(Locus(File.e, Rank.`7`), Locus(File.e, Rank.`5`), null)
            underTest.recordMove(Locus(File.d, Rank.`2`), Locus(File.d, Rank.`4`), null)
            underTest.previousMove()

            underTest.truncate()

            assertEquals(2, underTest.totalMoves)
            assertEquals(2, underTest.currentIndex)
        }

        @Test
        fun `GIVEN truncated WHEN reconstruct THEN game at correct position`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.recordMove(Locus(File.e, Rank.`7`), Locus(File.e, Rank.`5`), null)
            underTest.previousMove()
            underTest.truncate()

            val game = underTest.reconstructAtCurrentIndex()

            assertNotNull(game)
            assertEquals(Side.BLACK, game!!.info.turn)
        }

        @Test
        fun `GIVEN truncated WHEN new move recorded THEN builds on truncated history`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)
            underTest.recordMove(Locus(File.e, Rank.`7`), Locus(File.e, Rank.`5`), null)
            underTest.jumpToStart()
            underTest.truncate()

            underTest.recordMove(Locus(File.d, Rank.`2`), Locus(File.d, Rank.`4`), null)

            assertEquals(1, underTest.currentIndex)
            assertEquals(1, underTest.totalMoves)
        }
    }

    @Nested
    internal inner class FenReconstruction {
        @Test
        fun `GIVEN moves recorded WHEN fenAtIndex called THEN returns correct FEN`() {
            underTest.initialize(STARTING_FEN)
            underTest.recordMove(Locus(File.e, Rank.`2`), Locus(File.e, Rank.`4`), null)

            val fen = underTest.fenAtIndex(0)
            assertEquals(STARTING_FEN, fen)

            val fenAfterE4 = underTest.fenAtIndex(1)
            assertTrue(fenAfterE4.contains(" b "))
        }
    }

    companion object {
        private const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}
