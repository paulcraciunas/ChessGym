package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.boardvis.MoveThePieceBoardData
import com.paulcraciunas.domain.api.general.SequentialRandomFactory
import com.paulcraciunas.domain.impl.GenerateRandomLociImpl
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GenerateMoveThePieceBoardImplTest {
    private val randomFactory = SequentialRandomFactory()
    private val gameFactory = RealGameFactory()
    private val moveValidator = gameFactory.moveValidator()

    private val underTest = GenerateMoveThePieceBoardImpl(
        GenerateRandomLociImpl(randomFactory), randomFactory, gameFactory
    )

    @Test
    fun `GIVEN valid parameters WHEN invoke THEN returns board with player piece`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 1,
            opposingPieceCount = 2
        )

        // Then
        assertNotNull(result.playerPieceLocus)
        assertTrue(result.board.has(Piece.Rook, Side.WHITE, result.playerPieceLocus))
    }

    @Test
    fun `GIVEN valid parameters WHEN invoke THEN returns board with opposing pieces up to requested count`() {
        // Given
        val requestedCount = 4

        // When
        val result = underTest(
            piece = Piece.Bishop,
            requiredMoves = 1,
            opposingPieceCount = requestedCount
        )

        // Then
        assertTrue(result.opposingPieces.size <= requestedCount)
        assertPathIsSafe(Piece.Bishop, result)
    }

    @Test
    fun `GIVEN valid parameters WHEN invoke THEN player position is not same as any opposing piece`() {
        // When
        val result = underTest(
            piece = Piece.Knight,
            requiredMoves = 2,
            opposingPieceCount = 4
        )

        // Then
        assertTrue(result.playerPieceLocus !in result.opposingPieces.keys)
    }

    @Test
    fun `GIVEN Rook piece WHEN invoke THEN generates valid board with safe path`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 2,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertPathIsSafe(Piece.Rook, result)
    }

    @Test
    fun `GIVEN Bishop piece WHEN invoke THEN generates valid board with safe path`() {
        // When
        val result = underTest(
            piece = Piece.Bishop,
            requiredMoves = 2,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertPathIsSafe(Piece.Bishop, result)
    }

    @Test
    fun `GIVEN Knight piece WHEN invoke THEN generates valid board with safe path`() {
        // When
        val result = underTest(
            piece = Piece.Knight,
            requiredMoves = 2,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertPathIsSafe(Piece.Knight, result)
    }

    @Test
    fun `GIVEN Queen piece WHEN invoke THEN generates valid board with safe path`() {
        // When
        val result = underTest(
            piece = Piece.Queen,
            requiredMoves = 2,
            opposingPieceCount = 4
        )

        // Then
        assertNotNull(result)
        assertPathIsSafe(Piece.Queen, result)
    }

    @Test
    fun `GIVEN multiple required moves WHEN invoke THEN valid path exists for all moves`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 4,
            opposingPieceCount = 3
        )

        // Then
        assertNotNull(result)
        assertPathIsSafe(Piece.Rook, result)
    }

    @Test
    fun `GIVEN many opposing pieces WHEN invoke THEN no opposing piece attacks the path`() {
        // When
        val result = underTest(
            piece = Piece.Knight,
            requiredMoves = 2,
            opposingPieceCount = 8
        )

        // Then
        assertNotNull(result)
        assertPathIsSafe(Piece.Knight, result)
    }

    @Test
    fun `GIVEN many opposing pieces requested WHEN invoke THEN places as many as safely possible`() {
        // Given - requesting a lot of opposing pieces on a constrained board
        val requestedCount = 10

        // When
        val result = underTest(
            piece = Piece.Queen,
            requiredMoves = 3,
            opposingPieceCount = requestedCount
        )

        // Then - should place pieces without any attacking the path
        assertNotNull(result)
        assertTrue(result.opposingPieces.size <= requestedCount)
        assertPathIsSafe(Piece.Queen, result)
    }

    @Test
    fun `GIVEN single move WHEN invoke THEN player has at least one valid safe move`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 1,
            opposingPieceCount = 3
        )

        // Then
        val validMoves = moveValidator.getValidMoves(
            piece = Piece.Rook,
            side = Side.WHITE,
            from = result.playerPieceLocus,
            board = result.board,
            blockers = setOf(result.playerPieceLocus) + result.opposingPieces.keys
        )
        val safeMoves = validMoves.filter { move ->
            !moveValidator.isSquareUnderAttack(move, Side.BLACK, result.board)
        }
        assertTrue(safeMoves.isNotEmpty()) {
            "Player should have at least one safe move from ${result.playerPieceLocus}"
        }
    }

    @Test
    fun `GIVEN zero opposing pieces WHEN invoke THEN returns board with only player piece`() {
        // When
        val result = underTest(
            piece = Piece.Bishop,
            requiredMoves = 2,
            opposingPieceCount = 0
        )

        // Then
        assertNotNull(result)
        assertTrue(result.opposingPieces.isEmpty())
        assertTrue(result.board.has(Piece.Bishop, Side.WHITE, result.playerPieceLocus))
    }

    @Test
    fun `GIVEN any configuration WHEN invoke THEN all opposing pieces are black`() {
        // When
        val result = underTest(
            piece = Piece.Rook,
            requiredMoves = 1,
            opposingPieceCount = 4
        )

        // Then
        result.opposingPieces.forEach { (locus, piece) ->
            assertTrue(result.board.has(piece, Side.BLACK, locus)) {
                "Opposing piece $piece at $locus should be black"
            }
        }
    }

    // Verifies that a valid path exists from the player's starting position using DFS.
    private fun assertPathIsSafe(
        piece: Piece,
        boardData: MoveThePieceBoardData,
    ) {
        val hasValidPath = dfsValidPath(
            piece = piece,
            currentLocus = boardData.playerPieceLocus,
            board = boardData.board,
            visitedSquares = setOf(boardData.playerPieceLocus),
            remainingMoves = boardData.opposingPieces.size.coerceAtLeast(1)
        )
        assertTrue(hasValidPath) {
            "Expected a valid safe path from ${boardData.playerPieceLocus} for $piece"
        }
    }

    private fun dfsValidPath(
        piece: Piece,
        currentLocus: Locus,
        board: IBoard,
        visitedSquares: Set<Locus>,
        remainingMoves: Int,
    ): Boolean {
        if (remainingMoves <= 0) return true

        val validMoves = moveValidator.getValidMoves(
            piece = piece,
            side = Side.WHITE,
            from = currentLocus,
            board = board,
            blockers = visitedSquares
        )

        val safeMoves = validMoves.filter { move ->
            !moveValidator.isSquareUnderAttack(move, Side.BLACK, board)
        }

        return safeMoves.any { move ->
            dfsValidPath(piece, move, board, visitedSquares + move, remainingMoves - 1)
        }
    }
}
