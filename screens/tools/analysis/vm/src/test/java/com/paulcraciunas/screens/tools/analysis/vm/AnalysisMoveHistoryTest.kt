package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
            underTest.initialize(STARTING_FEN, Side.WHITE)

            assertEquals(0, underTest.currentIndex)
            assertEquals(0, underTest.size())
            assertTrue(underTest.isAtEnd())
        }

        @Test
        fun `GIVEN starting FEN WHEN initialized THEN currentSide is white`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)

            assertEquals(Side.WHITE, underTest.currentSide())
        }

        @Test
        fun `GIVEN black to move FEN WHEN initialized THEN currentSide is black`() {
            underTest.initialize(FEN_AFTER_E4, Side.BLACK)

            assertEquals(Side.BLACK, underTest.currentSide())
        }
    }

    @Nested
    internal inner class RecordingMoves {
        @Test
        fun `GIVEN initialized WHEN move recorded THEN index advances`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)

            assertEquals(1, underTest.currentIndex)
            assertEquals(1, underTest.size())
            assertTrue(underTest.isAtEnd())
        }

        @Test
        fun `GIVEN move recorded WHEN currentSide THEN returns next side to move`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)

            assertEquals(Side.BLACK, underTest.currentSide())
        }

        @Test
        fun `GIVEN multiple moves WHEN recorded THEN total moves increases`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            recordMove(E7, E5, FEN_AFTER_E4_E5, Side.WHITE)

            assertEquals(2, underTest.currentIndex)
            assertEquals(2, underTest.size())
        }

        @Test
        fun `GIVEN initialized WHEN recordMove with game THEN extracts data from game`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            val game = fenSerializer.from(STARTING_FEN)
            game.start()
            val ply = game.plies(E2).first { it.to == E4 }
            game.play(ply)

            underTest.recordMove(game)

            assertEquals(1, underTest.size())
            assertEquals(Side.BLACK, underTest.currentSide())
            assertTrue(underTest.currentFen().contains(" b "))
        }
    }

    @Nested
    internal inner class NavigationMethods {
        @Test
        fun `GIVEN moves played WHEN previousMove THEN index decreases`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)

            underTest.previousMove()

            assertEquals(0, underTest.currentIndex)
            assertFalse(underTest.isAtEnd())
        }

        @Test
        fun `GIVEN at start WHEN previousMove THEN stays at zero`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)

            underTest.previousMove()

            assertEquals(0, underTest.currentIndex)
        }

        @Test
        fun `GIVEN navigated back WHEN nextMove THEN index increases`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            underTest.previousMove()

            underTest.nextMove()

            assertEquals(1, underTest.currentIndex)
            assertTrue(underTest.isAtEnd())
        }

        @Test
        fun `GIVEN at end WHEN nextMove THEN stays at end`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)

            underTest.nextMove()

            assertEquals(1, underTest.currentIndex)
        }

        @Test
        fun `GIVEN multiple moves WHEN jumpToStart THEN index is zero`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            recordMove(E7, E5, FEN_AFTER_E4_E5, Side.WHITE)

            underTest.jumpToStart()

            assertEquals(0, underTest.currentIndex)
        }

        @Test
        fun `GIVEN navigated back WHEN jumpToEnd THEN index is at last`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            recordMove(E7, E5, FEN_AFTER_E4_E5, Side.WHITE)
            underTest.jumpToStart()

            underTest.jumpToEnd()

            assertEquals(2, underTest.currentIndex)
            assertTrue(underTest.isAtEnd())
        }
    }

    @Nested
    internal inner class TruncateAndReconstruct {
        @Test
        fun `GIVEN navigated back WHEN truncate THEN future moves removed`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            recordMove(E7, E5, FEN_AFTER_E4_E5, Side.WHITE)
            underTest.jumpToStart()

            underTest.truncate()

            assertEquals(0, underTest.size())
            assertEquals(0, underTest.currentIndex)
        }

        @Test
        fun `GIVEN navigated to middle WHEN truncate THEN keeps moves up to index`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            recordMove(E7, E5, FEN_AFTER_E4_E5, Side.WHITE)
            recordMove(D2, D4, FEN_AFTER_E4_E5_D4, Side.BLACK)
            underTest.previousMove()

            underTest.truncate()

            assertEquals(2, underTest.size())
            assertEquals(2, underTest.currentIndex)
        }

        @Test
        fun `GIVEN truncated WHEN new move recorded THEN builds on truncated history`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            recordMove(E7, E5, FEN_AFTER_E4_E5, Side.WHITE)
            underTest.jumpToStart()
            underTest.truncate()

            recordMove(D2, D4, FEN_AFTER_D4, Side.BLACK)

            assertEquals(1, underTest.currentIndex)
            assertEquals(1, underTest.size())
        }
    }

    @Nested
    internal inner class FenRetrieval {
        @Test
        fun `GIVEN initialized WHEN currentFen THEN returns initial FEN`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)

            assertEquals(STARTING_FEN, underTest.currentFen())
        }

        @Test
        fun `GIVEN move recorded WHEN currentFen THEN returns recorded FEN`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)

            assertEquals(FEN_AFTER_E4, underTest.currentFen())
        }

        @Test
        fun `GIVEN navigated back WHEN currentFen THEN returns FEN at that position`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            recordMove(E7, E5, FEN_AFTER_E4_E5, Side.WHITE)
            underTest.previousMove()

            assertEquals(FEN_AFTER_E4, underTest.currentFen())
        }

        @Test
        fun `GIVEN navigated to start WHEN currentFen THEN returns initial FEN`() {
            underTest.initialize(STARTING_FEN, Side.WHITE)
            recordMove(E2, E4, FEN_AFTER_E4, Side.BLACK)
            underTest.jumpToStart()

            assertEquals(STARTING_FEN, underTest.currentFen())
        }
    }

    private fun recordMove(from: Locus, to: Locus, resultFen: String, sideToMove: Side) {
        underTest.recordMove(from, to, null, resultFen, sideToMove)
    }

    companion object {
        private const val STARTING_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        private const val FEN_AFTER_E4 =
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
        private const val FEN_AFTER_E4_E5 =
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2"
        private const val FEN_AFTER_E4_E5_D4 =
            "rnbqkbnr/pppp1ppp/8/4p3/3PP3/8/PPP2PPP/RNBQKBNR b KQkq d3 0 2"
        private const val FEN_AFTER_D4 =
            "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"

        private val E2 = Locus(File.e, Rank.`2`)
        private val E4 = Locus(File.e, Rank.`4`)
        private val E7 = Locus(File.e, Rank.`7`)
        private val E5 = Locus(File.e, Rank.`5`)
        private val D2 = Locus(File.d, Rank.`2`)
        private val D4 = Locus(File.d, Rank.`4`)
    }
}
