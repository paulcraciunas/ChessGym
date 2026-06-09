package com.paulcraciunas.screens.data

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.FakeEngineOrchestrator
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class EngineOpponentTest {
    private val gameFactory = RealGameFactory()
    private val orchestrator = FakeEngineOrchestrator()

    @Nested
    internal inner class Init {
        @Test
        fun `GIVEN game with rating WHEN init THEN starts engine with that rating`() = runTest {
            val game = buildGame(rating = 2000).also { it.start() }
            val underTest = EngineOpponent(orchestrator, game)

            underTest.init()

            assertEquals(2000, orchestrator.currentElo)
        }

        @Test
        fun `GIVEN game without rating WHEN init THEN starts engine with DEFAULT_ELO`() = runTest {
            val game = buildGame().also { it.start() }
            val underTest = EngineOpponent(orchestrator, game)

            underTest.init()

            assertEquals(ChessEngine.DEFAULT_ELO, orchestrator.currentElo)
        }
    }

    @Nested
    internal inner class CanPlay {
        @Test
        fun `GIVEN game in Ready state WHEN canPlay THEN returns true`() {
            val game = buildGame()
            val underTest = EngineOpponent(orchestrator, game)

            assertTrue(underTest.canPlay())
        }

        @Test
        fun `GIVEN game in InProgress state WHEN canPlay THEN returns true`() {
            val game = buildGame().also { it.start() }
            val underTest = EngineOpponent(orchestrator, game)

            assertTrue(underTest.canPlay())
        }

        @Test
        fun `GIVEN game in Finished state WHEN canPlay THEN returns false`() {
            val game = buildGame().also { it.start(); it.resign() }
            val underTest = EngineOpponent(orchestrator, game)

            assertFalse(underTest.canPlay())
        }
    }

    @Nested
    internal inner class PlayNext {
        @Test
        fun `GIVEN game in progress WHEN playNext THEN requests engine move and plays it`() =
            runTest {
                val game = buildGame().also { it.start() }
                val move = game.plies(Locus.e2).first()
                orchestrator.enqueueMoves(move)
                val underTest = EngineOpponent(orchestrator, game)

                val result = underTest.playNext()

                assertTrue(result)
                assertEquals(Game.GameState.InProgress, game.state)
            }

        @Test
        fun `GIVEN game in progress WHEN playNext THEN advances the game state`() = runTest {
            val game = buildGame().also { it.start() }
            val initialHistorySize = game.historySize
            val move = game.plies(Locus.e2).first()
            orchestrator.enqueueMoves(move)
            val underTest = EngineOpponent(orchestrator, game)

            underTest.playNext()

            assertEquals(initialHistorySize + 1, game.historySize)
        }

        @Test
        fun `GIVEN game finished WHEN playNext THEN returns false without requesting move`() =
            runTest {
                val game = buildGame().also { it.start(); it.resign() }
                val underTest = EngineOpponent(orchestrator, game)

                val result = underTest.playNext()

                assertFalse(result)
            }

        @Test
        fun `GIVEN game leading to checkmate WHEN playNext THEN game finishes`() = runTest {
            val game = buildGame().also { it.start() }
            game.play(Locus.f2, Locus.f3)
            game.play(Locus.e7, Locus.e5)
            game.play(Locus.g2, Locus.g4)

            val checkmate = game.ply(Locus.d8, Locus.h4)!!
            orchestrator.enqueueMoves(checkmate)
            val underTest = EngineOpponent(orchestrator, game)

            val result = underTest.playNext()

            assertTrue(result)
            assertTrue(game.state is Game.GameState.Finished)
        }
    }

    @Nested
    internal inner class Shutdown {
        @Test
        fun `GIVEN engine opponent WHEN shutdown THEN stops the orchestrator`() = runTest {
            val game = buildGame().also { it.start() }
            val underTest = EngineOpponent(orchestrator, game)

            underTest.shutdown()

            assertTrue(orchestrator.isStopped)
        }
    }

    private fun buildGame(rating: Int? = null): Game = gameFactory.builder()
        .withDefaultBoard()
        .apply { rating?.let { withRating(it) } }
        .buildGame()
}
