package com.paulcraciunas.game.logic.plies

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.EnPassentPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnPassentPlyTest {
    private val on = Board()

    private val underTest = EnPassentPly(
        turn = Side.WHITE,
        from = Locus.e5,
        to = Locus.d6,
        passedLoc = Locus.d5
    )

    @Test
    fun `WHEN executing an en passent THEN the pawn moves and the other pawn is captured`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.e5)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.d5)

        underTest.exec(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, Locus.d6))
        on.forEachPiece(Side.WHITE) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals(Locus.d6, locus)
        }
        on.forEachPiece(Side.BLACK) { piece, locus ->
            throw IllegalStateException("Not expecting $piece at $locus")
        }
        assertTrue(on.isEmpty(Locus.e5))
        assertTrue(on.isEmpty(Locus.d5))
    }

    @Test
    fun `WHEN piece is present where the pawn should end up THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.e5)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus.d6)

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN there is no pawn to capture en passent THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.e5)

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN attempting to capture en passent a non-pawn THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.e5)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus.d6)

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN undoing an en passent THEN the pawn moves back and the other pawn is put back`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.d6)

        underTest.undo(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, Locus.e5))
        assertTrue(on.has(Piece.Pawn, Side.BLACK, Locus.d5))
        on.forEachPiece(Side.WHITE) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals(Locus.e5, locus)
        }
        on.forEachPiece(Side.BLACK) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals(Locus.d5, locus)
        }
        assertTrue(on.isEmpty(Locus.d6))
    }

    @Test
    fun `GIVEN destination is occupied WHEN undoing an en passent THEN throw`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus.d6)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus.e5)

        assertThrows<AssertionError> { underTest.undo(on) }
    }

    @Test
    fun `WHEN serializing to algebraic notation THEN return correct string`() {
        assertEquals("exd6", underTest.algebraic())
    }

    @Test
    fun `WHEN getting captured piece THEN return pawn`() {
        assertEquals(Piece.Pawn, underTest.captured())
        assertTrue(underTest.isPawnMoveOrCapture())
    }

    @Test
    fun `WHEN accepting a piece for promotion THEN throw`() {
        assertThrows<AssertionError> { underTest.promote(Piece.Bishop) }
    }
}
