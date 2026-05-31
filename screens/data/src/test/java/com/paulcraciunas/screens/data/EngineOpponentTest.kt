package com.paulcraciunas.screens.data

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.FakeEngineOrchestrator
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EngineOpponentTest {
    private val gameFactory = RealGameFactory()
    private val fakeOrchestrator = FakeEngineOrchestrator()

    private lateinit var game: Game
    private lateinit var underTest: EngineOpponent

    @BeforeEach
    fun setup() {
        game = gameFactory.builder()
            .withDefaultBoard()
            .withRating(1500)
            .buildGame()
        game.start()
        underTest = EngineOpponent(fakeOrchestrator).apply { load(game) }
    }

    @Test
    fun `GIVEN engine WHEN prepare THEN starts new game with correct elo`() = runTest {
        underTest.prepare()

        assertEquals(1500, fakeOrchestrator.currentElo)
    }

    @Test
    fun `GIVEN engine WHEN prepare with new game THEN updates elo`() = runTest {
        underTest.prepare()

        val newGame = gameFactory.builder()
            .withDefaultBoard()
            .withRating(1800)
            .buildGame()
        newGame.start()
        underTest = EngineOpponent(fakeOrchestrator).apply { load(newGame) }
        underTest.prepare()

        assertEquals(1800, fakeOrchestrator.currentElo)
    }

    @Test
    fun `GIVEN game in progress WHEN canPlay THEN returns true`() {
        assertTrue(underTest.canPlay())
    }

    @Test
    fun `GIVEN game finished WHEN canPlay THEN returns false`() {
        game.resign()
        assertFalse(underTest.canPlay())
    }

    @Test
    fun `GIVEN game in progress WHEN playNext THEN engine move is played on game`() = runTest {
        underTest.prepare()
        game.play(Locus.e2, Locus.e4)

        val ply = game.plies(Locus.e7).first { it.to == Locus.e5 }
        fakeOrchestrator.enqueueMoves(ply)

        val result = underTest.playNext()

        assertTrue(result)
        assertEquals(Side.WHITE, game.info.turn)
    }

    @Test
    fun `GIVEN game finished WHEN playNext THEN returns false`() = runTest {
        game.resign()
        val result = underTest.playNext()
        assertFalse(result)
    }

    @Test
    fun `GIVEN running engine WHEN shutdown THEN stops orchestrator`() = runTest {
        underTest.prepare()
        underTest.shutdown()
        assertTrue(fakeOrchestrator.isStopped)
    }

    @Test
    fun `GIVEN game without rating WHEN prepare THEN uses default elo`() = runTest {
        val unratedGame = gameFactory.builder()
            .withDefaultBoard()
            .buildGame()
        unratedGame.start()
        val opponent = EngineOpponent(fakeOrchestrator).apply { load(unratedGame) }

        opponent.prepare()

        assertEquals(ChessEngine.DEFAULT_ELO, fakeOrchestrator.currentElo)
    }
}
