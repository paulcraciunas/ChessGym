package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealMoveValidator
import com.paulcraciunas.game.logic.impl.board.Board
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RealMoveValidatorTest {
    private val side = Side.BLACK
    private val home = "e4".loc()

    private val underTest = RealMoveValidator()

    @Test
    fun `GIVEN valid board WHEN attacking the original square THEN return false`() {
        // Given
        val piece = Piece.Bishop
        val on = defaultBoardWith(piece, side, home)

        // When
        val result = underTest.canAttack(piece = piece, side = side, from = home, to = home, board = on)

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN current piece is of same side THEN canAttack always returns true`() {
        // Given
        val piece = Piece.Bishop
        val on = Board().apply {
            add(piece = piece, side = side, at = home)
            add(piece = Piece.Rook, side = side, at = "e1".loc())
            add(piece = Piece.Queen, side = side, at = "e8".loc())
            add(piece = Piece.Knight, side = side, at = "f6".loc())
            add(piece = Piece.Bishop, side = side, at = "d5".loc())
        }

        // When
        val result = listOf("d3", "c2", "b1", "f5", "g6", "h7", "f3", "g2", "h1").map { it.loc() }.all {
            underTest.canAttack(piece = piece, side = side, from = home, to = it, board = on)
        }

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN destination is occupied THEN return true`() {
        // Given
        val piece = Piece.Bishop
        val to = "c2".loc()
        val on = defaultBoardWith(piece, side, home).apply {
            add(piece = Piece.Bishop, side = side.other(), at = to)
        }

        // When
        val result = underTest.canAttack(piece = piece, side = side, from = home, to = to, board = on)

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN destination is obstructed THEN return false`() {
        // Given
        val piece = Piece.Bishop
        val to = "b1".loc() // We'll put a bishop on C2
        val on = defaultBoardWith(piece, side, home).apply {
            add(piece = Piece.Bishop, side = side.other(), at = "c2".loc())
        }

        // When
        val result = underTest.canAttack(piece = piece, side = side, from = home, to = to, board = on)

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN checking if Queen can attack THEN return true for all valid squares`() {
        // Given
        val piece = Piece.Queen
        val on = defaultBoardWith(piece, side, home)

        // When
        val result = listOf(
            "d3", "c2", "b1", "d4", "c4", "b4", "a4", "d5", "c6", "b7", "a8",
            "e6", "e7", "e8", "f4", "g4", "h4", "g6", "h7", "f3", "f5"
        ).map { it.loc() }.all {
            underTest.canAttack(piece = piece, side = side, from = home, to = it, board = on)
        }

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN checking if Queen can attack THEN return false for invalid squares`() {
        // Given
        val piece = Piece.Queen
        val on = defaultBoardWith(piece, side, home)

        // When
        val result = listOf("d2", "c1", "b2", "d6", "c3", "a3", "c7", "b8", "a7", "f6", "g7", "h8").map { it.loc() }.any {
            underTest.canAttack(piece = piece, side = side, from = home, to = it, board = on)
        }

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN checking if Rook can attack THEN return true for all valid squares`() {
        // Given
        val piece = Piece.Rook
        val on = defaultBoardWith(piece, side, home)

        // When
        val result = listOf(
            "d4", "c4", "b4", "a4", "e5", "e6", "e7", "e8", "e3", "e2", "e1", "f4", "g4", "h4",
        ).map { it.loc() }.all {
            underTest.canAttack(piece = piece, side = side, from = home, to = it, board = on)
        }

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN checking if Knight can attack THEN return true for all valid squares`() {
        // Given
        val piece = Piece.Knight
        val on = defaultBoardWith(piece, side, home)

        // When
        val result = listOf(
            "d6", "c5", "c3", "d2", "f6", "g5", "g3", "f2",
        ).map { it.loc() }.all {
            underTest.canAttack(piece = piece, side = side, from = home, to = it, board = on)
        }

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN checking if Bishop can attack THEN return true for all valid squares`() {
        // Given
        val piece = Piece.Bishop
        val on = defaultBoardWith(piece, side, home)

        // When
        val result = listOf(
            "d5", "c6", "b7", "a8", "d3", "c2", "b1", "f5", "g6", "h7", "f3", "g2", "h1"
        ).map { it.loc() }.all {
            underTest.canAttack(piece = piece, side = side, from = home, to = it, board = on)
        }

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN square is under attack THEN return true`() {
        // Given
        val on = defaultBoard()

        // When
        val result = listOf(
            "a2", "a3", "a4", "a5", "a6", "a7", "a8", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
            "b2", "c2", "e2", "f2", "g2", "h2", "d3", "d4", "d5", "d6", "d7", "d8", "e3", "f4",
            "g5", "h6", "e5", "h8",
        ).map { it.loc() }.all {
            underTest.isSquareUnderAttack(square = it, attackingSide = side.other(), board = on)
        }

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN square is NOT under attack THEN return false`() {
        // Given
        val on = defaultBoard()

        // When
        val result = listOf(
            "h5", "h7"
        ).map { it.loc() }.any {
            underTest.isSquareUnderAttack(square = it, attackingSide = side.other(), board = on)
        }

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN fetching valid moves for Queen THEN return all valid moves`() {
        // Given
        val piece = Piece.Queen
        val on = defaultBoardWith(piece, side, home)

        // When
        val validMoves = underTest.getValidMoves(piece = piece, side = side, from = home, board = on, blockers = setOf(home))

        // Then
        val expected = setOf(
            "d4", "c4", "b4", "a4", "f4", "g4", "h4", "e5", "e6", "e7", "e8",
            "e3", "e2", "e1", "d5", "c6", "b7", "a8", "f5", "g6", "h7", "d3",
            "c2", "b1", "f3", "g2", "h1"
        ).map { it.loc() }.toSet()
        assertEquals(expected, validMoves)
    }

    @Test
    fun `GIVEN a board with pieces WHEN fetching valid moves for Knight THEN return all valid moves`() {
        // Given
        val piece = Piece.Knight
        val on = defaultBoardWith(piece, side, home)

        // When
        val validMoves = underTest.getValidMoves(piece = piece, side = side, from = home, board = on, blockers = setOf(home))

        // Then
        val expected = setOf("d6", "f6", "c5", "c3", "d2", "f2", "g5", "g3").map { it.loc() }.toSet()
        assertEquals(expected, validMoves)
    }

    @Test
    fun `GIVEN a board with pieces WHEN fetching valid moves for Rook THEN return all valid moves`() {
        // Given
        val piece = Piece.Rook
        val on = defaultBoardWith(piece, side, home)

        // When
        val validMoves = underTest.getValidMoves(piece = piece, side = side, from = home, board = on, blockers = setOf(home))

        // Then
        val expected = setOf("d4", "c4", "b4", "a4", "f4", "g4", "h4", "e5", "e6", "e7", "e8", "e3", "e2", "e1").map { it.loc() }.toSet()
        assertEquals(expected, validMoves)
    }

    @Test
    fun `GIVEN a board with pieces WHEN fetching valid moves for Bishop THEN return all valid moves`() {
        // Given
        val piece = Piece.Bishop
        val on = defaultBoardWith(piece, side, home)

        // When
        val validMoves = underTest.getValidMoves(piece = piece, side = side, from = home, board = on, blockers = setOf(home))

        // Then
        val expected = setOf("d5", "c6", "b7", "a8", "f5", "g6", "h7", "d3", "c2", "b1", "f3", "g2", "h1").map { it.loc() }.toSet()
        assertEquals(expected, validMoves)
    }

    @Test
    fun `GIVEN a board with pieces WHEN there are no valid moves THEN return empty set`() {
        // Given
        val piece = Piece.Rook
        val at = "d1".loc()
        val on = Board().apply {
            add(piece = piece, side = side, at = home)
            add(piece = Piece.Queen, side = side, at = "d2".loc())
            add(piece = Piece.Bishop, side = side, at = "c1".loc())
            add(piece = Piece.Knight, side = side, at = "e1".loc())
        }

        // When
        val validMoves = underTest.getValidMoves(piece = piece, side = side, from = at, board = on, blockers = setOf(home))

        // Then
        assertTrue(validMoves.isEmpty())
    }

    private fun defaultBoardWith(piece: Piece, side: Side, home: Locus) = defaultBoard().apply {
        add(piece = piece, side = side, at = home)
    }

    private fun defaultBoard() = Board().apply {
        add(piece = Piece.Rook, side = side.other(), at = "a1".loc())
        add(piece = Piece.Queen, side = side.other(), at = "d2".loc())
        add(piece = Piece.Knight, side = side.other(), at = "f7".loc())
        add(piece = Piece.Bishop, side = side.other(), at = "h8".loc())
    }
}
