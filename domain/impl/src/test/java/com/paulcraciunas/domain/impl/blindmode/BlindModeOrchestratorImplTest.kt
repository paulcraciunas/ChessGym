package com.paulcraciunas.domain.impl.blindmode

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.FakeChessEngine
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BlindModeOrchestratorImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private val engine = FakeChessEngine()
    private val serializer = FenSerializer(RealGameFactory())

    private val underTest = BlindModeOrchestratorImpl(
        chessEngine = engine,
        serializer = serializer,
        dispatcher = testDispatcher,
    )

    @Test
    fun `GIVEN new orchestrator WHEN initialize THEN engine is initialized`() = runTest(testDispatcher) {
        // When
        underTest.initialize()

        // Then
        assertTrue(engine.isInitialized)
    }

    @Test
    fun `GIVEN new orchestrator WHEN startGame THEN engine receives elo`() = runTest(testDispatcher) {
        // When
        underTest.startGame(elo = 1400)

        // Then
        assertEquals(1400, engine.currentElo)
    }

    @Test
    fun `GIVEN new orchestrator WHEN startGame THEN returns started game`() = runTest(testDispatcher) {
        // When
        val game = underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // Then
        assertEquals(Game.GameState.InProgress, game.state)
    }

    @Test
    fun `GIVEN new orchestrator WHEN startGame with side THEN game has white to move`() = runTest(testDispatcher) {
        // When
        val game = underTest.startGame(elo = ChessEngine.DEFAULT_ELO, side = Side.BLACK)

        // Then
        assertEquals(Side.WHITE, game.info.turn)
    }

    @Test
    fun `GIVEN new orchestrator WHEN startGame THEN game has correct rating`() = runTest(testDispatcher) {
        // When
        val game = underTest.startGame(elo = 1500)

        // Then
        assertEquals(1500, game.rating)
    }

    @Test
    fun `GIVEN started game with e2-e4 played WHEN requestEngineMove THEN returns valid ply`() = runTest(testDispatcher) {
        // Given
        val game = underTest.startGame(elo = ChessEngine.DEFAULT_ELO)
        game.play(Locus.e2, Locus.e4)

        // When
        val ply = underTest.requestEngineMove()

        // Then
        assertEquals(Locus.e7, ply.from)
        assertEquals(Locus.e5, ply.to)
        assertEquals(Piece.Pawn, ply.piece)
    }

    @Test
    fun `GIVEN started game WHEN requestEngineMove THEN engine receives FEN`() = runTest(testDispatcher) {
        // Given
        val game = underTest.startGame(elo = ChessEngine.DEFAULT_ELO)
        game.play(Locus.e2, Locus.e4)

        // When
        underTest.requestEngineMove()

        // Then
        assertNotNull(engine.lastReceivedFen)
    }

    @Test
    fun `GIVEN started game WHEN stop THEN engine is stopped`() = runTest(testDispatcher) {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        underTest.stop()

        // Then
        assertTrue(engine.isStopped)
    }
}
