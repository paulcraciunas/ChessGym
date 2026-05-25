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
    private val home = Locus.e4

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
            add(piece = Piece.Rook, side = side, at = Locus.e1)
            add(piece = Piece.Queen, side = side, at = Locus.e8)
            add(piece = Piece.Knight, side = side, at = Locus.f6)
            add(piece = Piece.Bishop, side = side, at = Locus.d5)
        }

        // When
        val result = listOf(Locus.d3, Locus.c2, Locus.b1, Locus.f5, Locus.g6, Locus.h7, Locus.f3, Locus.g2, Locus.h1).all {
            underTest.canAttack(piece = piece, side = side, from = home, to = it, board = on)
        }

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a board with pieces WHEN destination is occupied THEN return true`() {
        // Given
        val piece = Piece.Bishop
        val to = Locus.c2
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
        val to = Locus.b1
        val on = defaultBoardWith(piece, side, home).apply {
            add(piece = Piece.Bishop, side = side.other(), at = Locus.c2)
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
            Locus.d3, Locus.c2, Locus.b1, Locus.d4, Locus.c4, Locus.b4, Locus.a4, Locus.d5, Locus.c6, Locus.b7, Locus.a8,
            Locus.e6, Locus.e7, Locus.e8, Locus.f4, Locus.g4, Locus.h4, Locus.g6, Locus.h7, Locus.f3, Locus.f5
        ).all {
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
        val result = listOf(Locus.d2, Locus.c1, Locus.b2, Locus.d6, Locus.c3, Locus.a3, Locus.c7, Locus.b8, Locus.a7, Locus.f6, Locus.g7, Locus.h8).any {
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
            Locus.d4, Locus.c4, Locus.b4, Locus.a4, Locus.e5, Locus.e6, Locus.e7, Locus.e8, Locus.e3, Locus.e2, Locus.e1, Locus.f4, Locus.g4, Locus.h4,
        ).all {
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
            Locus.d6, Locus.c5, Locus.c3, Locus.d2, Locus.f6, Locus.g5, Locus.g3, Locus.f2,
        ).all {
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
            Locus.d5, Locus.c6, Locus.b7, Locus.a8, Locus.d3, Locus.c2, Locus.b1, Locus.f5, Locus.g6, Locus.h7, Locus.f3, Locus.g2, Locus.h1
        ).all {
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
            Locus.a2, Locus.a3, Locus.a4, Locus.a5, Locus.a6, Locus.a7, Locus.a8, Locus.b1, Locus.c1, Locus.d1, Locus.e1, Locus.f1, Locus.g1, Locus.h1,
            Locus.b2, Locus.c2, Locus.e2, Locus.f2, Locus.g2, Locus.h2, Locus.d3, Locus.d4, Locus.d5, Locus.d6, Locus.d7, Locus.d8, Locus.e3, Locus.f4,
            Locus.g5, Locus.h6, Locus.e5, Locus.h8,
        ).all {
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
            Locus.h5, Locus.h7
        ).any {
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
            Locus.d4, Locus.c4, Locus.b4, Locus.a4, Locus.f4, Locus.g4, Locus.h4, Locus.e5, Locus.e6, Locus.e7, Locus.e8,
            Locus.e3, Locus.e2, Locus.e1, Locus.d5, Locus.c6, Locus.b7, Locus.a8, Locus.f5, Locus.g6, Locus.h7, Locus.d3,
            Locus.c2, Locus.b1, Locus.f3, Locus.g2, Locus.h1
        ).toSet()
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
        val expected = setOf(Locus.d6, Locus.f6, Locus.c5, Locus.c3, Locus.d2, Locus.f2, Locus.g5, Locus.g3).toSet()
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
        val expected = setOf(Locus.d4, Locus.c4, Locus.b4, Locus.a4, Locus.f4, Locus.g4, Locus.h4, Locus.e5, Locus.e6, Locus.e7, Locus.e8, Locus.e3, Locus.e2, Locus.e1).toSet()
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
        val expected = setOf(Locus.d5, Locus.c6, Locus.b7, Locus.a8, Locus.f5, Locus.g6, Locus.h7, Locus.d3, Locus.c2, Locus.b1, Locus.f3, Locus.g2, Locus.h1).toSet()
        assertEquals(expected, validMoves)
    }

    @Test
    fun `GIVEN a board with pieces WHEN there are no valid moves THEN return empty set`() {
        // Given
        val piece = Piece.Rook
        val at = Locus.d1
        val on = Board().apply {
            add(piece = piece, side = side, at = home)
            add(piece = Piece.Queen, side = side, at = Locus.d2)
            add(piece = Piece.Bishop, side = side, at = Locus.c1)
            add(piece = Piece.Knight, side = side, at = Locus.e1)
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
        add(piece = Piece.Rook, side = side.other(), at = Locus.a1)
        add(piece = Piece.Queen, side = side.other(), at = Locus.d2)
        add(piece = Piece.Knight, side = side.other(), at = Locus.f7)
        add(piece = Piece.Bishop, side = side.other(), at = Locus.h8)
    }
}
