package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GenerateMoveThePieceBoard
import com.paulcraciunas.domain.api.MoveResult
import com.paulcraciunas.domain.api.MoveThePieceBoardData
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.game.logic.impl.board.Board
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MoveThePieceGameEngineImplTest {
    private val playerLocus = "d4".loc()
    private val simpleBoard = Board().apply {
        add(Piece.Rook, side = Side.BLACK, "a1".loc())
        add(Piece.Rook, side = Side.WHITE, playerLocus)
    }
    private val gameFactory = RealGameFactory()

    @Test
    fun `GIVEN game not started WHEN getState THEN throws exception`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerPieceLocus = "d4".loc(), board = Board()))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)

        // When/Then
        assertThrows(IllegalStateException::class.java) {
            underTest.getState()
        }
    }

    @Test
    fun `GIVEN game started WHEN getState THEN returns correct initial state`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, simpleBoard))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)

        // When
        underTest.startGame(
            piece = Piece.Rook,
            requiredMoves = 2,
            opposingPieceCount = 1,
            isTrainingMode = true,
        )
        val state = underTest.getState()

        // Then
        assertEquals(Piece.Rook, state.playerPiece)
        assertEquals(playerLocus, state.playerPieceLocus)
        assertEquals(simpleBoard, state.board)
        assertEquals(setOf(playerLocus), state.visitedSquares)
        assertEquals(2, state.movesRemaining)
        assertEquals(0, state.currentScore)
        assertEquals(1, state.level)
        assertTrue(state.isTrainingMode)
        assertFalse(state.isGameOver)
        assertFalse(state.wasCaptured)
    }

    @Test
    fun `GIVEN valid move WHEN makeMove THEN returns Success`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, simpleBoard))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Rook, 2, 1, true)

        // When - move rook to e4 (valid horizontal move)
        val result = underTest.makeMove(Locus(File.e, Rank.`4`))

        // Then
        assertTrue(result is MoveResult.Success)
        val state = (result as MoveResult.Success).newState
        assertEquals(Locus(File.e, Rank.`4`), state.playerPieceLocus)
        assertEquals(1, state.movesRemaining)
    }

    @Test
    fun `GIVEN invalid move WHEN makeMove THEN returns Invalid`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, simpleBoard))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Rook, 2, 1, true)

        // When - try diagonal move (invalid for rook)
        val result = underTest.makeMove(Locus(File.e, Rank.`5`))

        // Then
        assertTrue(result is MoveResult.Invalid)
    }

    @Test
    fun `GIVEN move to attacked square WHEN makeMove THEN returns Captured`() {
        // Given - place a black rook that attacks d1
        val board = Board().apply {
            add(Piece.Rook, side = Side.BLACK, "d1".loc())
        }
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, board))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Rook, 2, 1, true)

        // When - move down the d-file (attacked by black rook)
        val result = underTest.makeMove(Locus(File.d, Rank.`2`))

        // Then
        assertTrue(result is MoveResult.Captured)
        val state = underTest.getState()
        assertTrue(state.isGameOver)
        assertTrue(state.wasCaptured)
    }

    @Test
    fun `GIVEN last move of level WHEN makeMove THEN returns LevelComplete`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, simpleBoard))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Rook, 1, 1, true) // Only 1 move required

        // When
        val result = underTest.makeMove("e4".loc())

        // Then
        assertTrue(result is MoveResult.LevelComplete)
        val state = (result as MoveResult.LevelComplete).newState
        assertEquals(1, state.currentScore)
        assertEquals(2, state.level)
    }

    @Test
    fun `GIVEN visited square WHEN makeMove THEN returns Invalid`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, simpleBoard))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Rook, 3, 1, true)

        // When - move to e4, then try to move back to d4 (already visited)
        underTest.makeMove("e4".loc())
        val result = underTest.makeMove("d4".loc())

        // Then
        assertTrue(result is MoveResult.Invalid)
    }

    @Test
    fun `GIVEN reset called WHEN getState THEN throws exception`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, Board()))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Rook, 1, 0, true)

        // When
        underTest.reset()

        // Then
        assertThrows(IllegalStateException::class.java) {
            underTest.getState()
        }
    }

    @Test
    fun `GIVEN training mode WHEN advancing levels THEN piece stays the same`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, simpleBoard))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Bishop, 1, 0, true)

        // When - complete level
        val result = underTest.makeMove("e5".loc()) // Valid bishop move

        // Then
        assertTrue(result is MoveResult.LevelComplete)
        val state = (result as MoveResult.LevelComplete).newState
        assertEquals(Piece.Bishop, state.playerPiece) // Same piece in training mode
    }

    @Test
    fun `GIVEN non-training mode WHEN advancing levels THEN piece changes`() {
        // Given
        val generateBoard = FakeGenerateMoveThePieceBoard(MoveThePieceBoardData(playerLocus, simpleBoard))
        val underTest = MoveThePieceGameEngineImpl(generateBoard, gameFactory)
        underTest.startGame(Piece.Bishop, 1, 0, false) // Non-training, starts with Bishop

        // When - complete level
        val result = underTest.makeMove("e5".loc()) // Valid bishop move

        // Then
        assertTrue(result is MoveResult.LevelComplete)
        val state = (result as MoveResult.LevelComplete).newState
        assertEquals(Piece.Rook, state.playerPiece) // Cycles to next piece
    }

    private class FakeGenerateMoveThePieceBoard(private val result: MoveThePieceBoardData) : GenerateMoveThePieceBoard {
        override fun invoke(piece: Piece, requiredMoves: Int, opposingPieceCount: Int): MoveThePieceBoardData = result
    }
}
