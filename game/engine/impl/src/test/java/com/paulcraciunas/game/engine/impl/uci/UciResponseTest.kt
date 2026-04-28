package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UciResponseTest {
    @Nested
    internal inner class DoneFactoryTest {
        private val factory = ResponseFactory.DoneFactory

        @Test
        fun `GIVEN no input WHEN construct THEN returns Done`() {
            val result = factory.construct()

            assertNotNull(result)
            assertTrue(result is UciResponse.Done)
        }

        @Test
        fun `GIVEN any input WHEN construct THEN returns Done`() {
            val result = factory.construct("anything")

            assertNotNull(result)
            assertTrue(result is UciResponse.Done)
        }
    }

    @Nested
    internal inner class InitializedFactoryTest {
        private val factory = ResponseFactory.InitializedFactory

        @Test
        fun `GIVEN null WHEN construct THEN returns null`() {
            assertNull(factory.construct(null))
        }

        @Test
        fun `GIVEN uciok WHEN construct THEN returns Initialized`() {
            val result = factory.construct("uciok")

            assertNotNull(result)
            assertTrue(result is UciResponse.Initialized)
        }

        @Test
        fun `GIVEN uciok with whitespace WHEN construct THEN returns Initialized`() {
            val result = factory.construct("  uciok  ")

            assertNotNull(result)
        }

        @Test
        fun `GIVEN random text WHEN construct THEN returns null`() {
            assertNull(factory.construct("id name Stockfish 11"))
        }
    }

    @Nested
    internal inner class ReadyFactoryTest {
        private val factory = ResponseFactory.ReadyFactory

        @Test
        fun `GIVEN null WHEN construct THEN returns null`() {
            assertNull(factory.construct(null))
        }

        @Test
        fun `GIVEN readyok WHEN construct THEN returns Ready`() {
            val result = factory.construct("readyok")

            assertNotNull(result)
            assertTrue(result is UciResponse.Ready)
        }

        @Test
        fun `GIVEN random text WHEN construct THEN returns null`() {
            assertNull(factory.construct("info depth 12"))
        }
    }

    @Nested
    internal inner class BestMoveFactoryTest {
        private val factory = ResponseFactory.BestMoveFactory

        @Test
        fun `GIVEN null WHEN construct THEN returns null`() {
            assertNull(factory.construct(null))
        }

        @Test
        fun `GIVEN bestmove e2e4 WHEN construct THEN returns BestMove with correct loci`() {
            val result = factory.construct("bestmove e2e4")

            assertNotNull(result)
            val bestMove = result as UciResponse.BestMove
            assertEquals(Locus(File.e, Rank.`2`), bestMove.engineMove.from)
            assertEquals(Locus(File.e, Rank.`4`), bestMove.engineMove.to)
            assertNull(bestMove.engineMove.promotion)
        }

        @Test
        fun `GIVEN bestmove with promotion WHEN construct THEN returns promotion piece`() {
            val result = factory.construct("bestmove a7a8q")

            assertNotNull(result)
            val bestMove = result as UciResponse.BestMove
            assertEquals(Locus(File.a, Rank.`7`), bestMove.engineMove.from)
            assertEquals(Locus(File.a, Rank.`8`), bestMove.engineMove.to)
            assertEquals(Piece.Queen, bestMove.engineMove.promotion)
        }

        @Test
        fun `GIVEN bestmove with knight promotion WHEN construct THEN returns Knight`() {
            val result = factory.construct("bestmove c7c8n")

            assertNotNull(result)
            val bestMove = result as UciResponse.BestMove
            assertEquals(Piece.Knight, bestMove.engineMove.promotion)
        }

        @Test
        fun `GIVEN bestmove with ponder WHEN construct THEN parses correctly`() {
            val result = factory.construct("bestmove e2e4 ponder e7e5")

            assertNotNull(result)
            val bestMove = result as UciResponse.BestMove
            assertEquals(Locus(File.e, Rank.`2`), bestMove.engineMove.from)
            assertEquals(Locus(File.e, Rank.`4`), bestMove.engineMove.to)
        }

        @Test
        fun `GIVEN info line WHEN construct THEN returns null`() {
            assertNull(factory.construct("info depth 15 seldepth 20"))
        }

        @Test
        fun `GIVEN bestmove with invalid format WHEN construct THEN throws`() {
            assertThrows<IllegalArgumentException> {
                factory.construct("bestmove ab")
            }
        }
    }
}

internal class UciCommandTest {
    @Test
    fun `GIVEN Init WHEN protocol THEN returns uci`() {
        assertEquals("uci", UciCommand.Init.protocol())
    }

    @Test
    fun `GIVEN IsReady WHEN protocol THEN returns isready`() {
        assertEquals("isready", UciCommand.IsReady.protocol())
    }

    @Test
    fun `GIVEN NewGame WHEN protocol THEN returns ucinewgame`() {
        assertEquals("ucinewgame", UciCommand.NewGame.protocol())
    }

    @Test
    fun `GIVEN Quit WHEN protocol THEN returns quit`() {
        assertEquals("quit", UciCommand.Quit.protocol())
    }

    @Test
    fun `GIVEN Stop WHEN protocol THEN returns stop`() {
        assertEquals("stop", UciCommand.Stop.protocol())
    }

    @Test
    fun `GIVEN LimitElo WHEN protocol THEN returns setoption command`() {
        assertEquals(
            "setoption name UCI_LimitStrength value true",
            UciCommand.LimitElo.protocol(),
        )
    }

    @Test
    fun `GIVEN SetElo 1400 WHEN protocol THEN returns correct setoption`() {
        assertEquals(
            "setoption name UCI_Elo value 1400",
            UciCommand.SetElo(1400).protocol(),
        )
    }

    @Test
    fun `GIVEN SetPosition WHEN protocol THEN returns position fen`() {
        val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"

        assertEquals(
            "position fen $fen",
            UciCommand.SetPosition(fen).protocol(),
        )
    }

    @Test
    fun `GIVEN SetMoveTime WHEN protocol THEN returns go movetime`() {
        assertEquals(
            "go movetime 2000",
            UciCommand.SetMoveTime(2000).protocol(),
        )
    }

    @Test
    fun `GIVEN SetMoveTime default WHEN protocol THEN returns go movetime 1000`() {
        assertTrue(UciCommand.SetMoveTime().protocol().contains("go movetime"))
    }

    @Test
    fun `GIVEN Init WHEN responseFactory THEN returns InitializedFactory`() {
        assertTrue(UciCommand.Init.responseFactory() is ResponseFactory.InitializedFactory)
    }

    @Test
    fun `GIVEN IsReady WHEN responseFactory THEN returns ReadyFactory`() {
        assertTrue(UciCommand.IsReady.responseFactory() is ResponseFactory.ReadyFactory)
    }

    @Test
    fun `GIVEN SetMoveTime WHEN responseFactory THEN returns BestMoveFactory`() {
        assertTrue(UciCommand.SetMoveTime().responseFactory() is ResponseFactory.BestMoveFactory)
    }

    @Test
    fun `GIVEN NewGame WHEN responseFactory THEN returns DoneFactory`() {
        assertTrue(UciCommand.NewGame.responseFactory() is ResponseFactory.DoneFactory)
    }

    @Test
    fun `GIVEN DisableLimitStrength WHEN protocol THEN returns setoption command`() {
        assertEquals(
            "setoption name UCI_LimitStrength value false",
            UciCommand.DisableLimitStrength.protocol(),
        )
    }

    @Test
    fun `GIVEN SetMultiPV 3 WHEN protocol THEN returns correct setoption`() {
        assertEquals(
            "setoption name MultiPV value 3",
            UciCommand.SetMultiPV(3).protocol(),
        )
    }

    @Test
    fun `GIVEN GoInfinite WHEN protocol THEN returns go infinite`() {
        assertEquals("go infinite", UciCommand.GoInfinite.protocol())
    }

    @Test
    fun `GIVEN GoInfinite WHEN responseFactory THEN returns DoneFactory`() {
        assertTrue(UciCommand.GoInfinite.responseFactory() is ResponseFactory.DoneFactory)
    }

    @Test
    fun `GIVEN DisableLimitStrength WHEN responseFactory THEN returns DoneFactory`() {
        assertTrue(UciCommand.DisableLimitStrength.responseFactory() is ResponseFactory.DoneFactory)
    }

    @Test
    fun `GIVEN SetMultiPV WHEN responseFactory THEN returns DoneFactory`() {
        assertTrue(UciCommand.SetMultiPV(3).responseFactory() is ResponseFactory.DoneFactory)
    }
}
