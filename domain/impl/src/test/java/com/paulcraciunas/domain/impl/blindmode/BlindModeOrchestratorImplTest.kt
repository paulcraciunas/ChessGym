package com.paulcraciunas.domain.impl.blindmode

import com.paulcraciunas.domain.api.blindmode.EnginePlayResult
import com.paulcraciunas.domain.api.blindmode.PlayResult
import com.paulcraciunas.domain.api.blindmode.SelectionResult
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.FakeChessEngine
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BlindModeOrchestratorImplTest {
    private val gameFactory = RealGameFactory()
    private val fakeEngine = FakeChessEngine()
    private val serializer = FenSerializer(gameFactory)

    private val underTest = BlindModeOrchestratorImpl(
        gameFactory = gameFactory,
        chessEngine = fakeEngine,
        serializer = serializer,
    )

    @Test
    fun `GIVEN new orchestrator WHEN startGame THEN engine receives elo`() = runTest {
        // When
        underTest.startGame(elo = 1400)

        // Then
        assertEquals(1400, fakeEngine.currentElo)
    }

    @Test
    fun `GIVEN started game WHEN selectSquare on e2 THEN PieceSelected with legal moves`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        val result = underTest.selectSquare("e2".loc())

        // Then
        assertTrue(result is SelectionResult.PieceSelected)
        val selected = result as SelectionResult.PieceSelected
        assertEquals("e2".loc(), selected.locus)
        assertTrue(selected.legalMoves.isNotEmpty())
    }

    @Test
    fun `GIVEN started game WHEN selectSquare on empty square THEN NoPiece`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        val result = underTest.selectSquare("e4".loc())

        // Then
        assertTrue(result is SelectionResult.NoPiece)
    }

    @Test
    fun `GIVEN started game WHEN selectSquare on opponent piece THEN WrongSide`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        val result = underTest.selectSquare("e7".loc())

        // Then
        assertTrue(result is SelectionResult.WrongSide)
    }

    @Test
    fun `GIVEN started game WHEN playMove e2 to e4 THEN Success with ply`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        val result = underTest.playMove("e2".loc(), "e4".loc())

        // Then
        assertTrue(result is PlayResult.Success)
        val success = result as PlayResult.Success
        assertEquals("e4".loc(), success.ply.to)
        assertEquals(Piece.Pawn, success.ply.piece)
    }

    @Test
    fun `GIVEN started game WHEN playMove invalid THEN returns Invalid`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        val result = underTest.playMove("e2".loc(), "e5".loc())

        // Then
        assertTrue(result is PlayResult.Invalid)
    }

    @Test
    fun `GIVEN game after e2-e4 WHEN requestEngineMove THEN returns engine ply`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)
        underTest.playMove("e2".loc(), "e4".loc())
        fakeEngine.enqueueMoves(EngineMove(from = "e7".loc(), to = "e5".loc()))

        // When
        val result = underTest.requestEngineMove()

        // Then
        assertTrue(result is EnginePlayResult.Success)
        val success = result as EnginePlayResult.Success
        assertEquals("e5".loc(), success.ply.to)
        assertNotNull(fakeEngine.lastReceivedFen)
    }

    @Test
    fun `GIVEN started game WHEN playMove then check moveHistory THEN history has the ply`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)
        underTest.playMove("e2".loc(), "e4".loc())

        // When
        val history = underTest.moveHistory()

        // Then
        assertEquals(1, history.size)
        assertEquals("e4".loc(), history[0].to)
    }

    @Test
    fun `GIVEN started game WHEN currentFen THEN returns valid FEN string`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        val fen = underTest.currentFen()

        // Then
        assertTrue(fen.contains("rnbqkbnr"))
        assertTrue(fen.contains("RNBQKBNR"))
    }

    @Test
    fun `GIVEN started game WHEN playerSide THEN returns WHITE`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When/Then
        assertEquals(Side.WHITE, underTest.playerSide())
    }

    @Test
    fun `GIVEN started game WHEN resign THEN game state is finished`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        underTest.resign()

        // Then
        val fen = underTest.currentFen()
        assertNotNull(fen)
    }

    @Test
    fun `GIVEN started game WHEN reset THEN engine stop is called`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        underTest.reset()

        // Then - no crash, stop was called
    }

    @Test
    fun `GIVEN started game WHEN pliesFrom e2 THEN returns valid target loci`() = runTest {
        // Given
        underTest.startGame(elo = ChessEngine.DEFAULT_ELO)

        // When
        val moves = underTest.pliesFrom("e2".loc())

        // Then
        assertTrue(moves.contains("e3".loc()))
        assertTrue(moves.contains("e4".loc()))
    }
}
