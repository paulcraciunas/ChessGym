package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.loc
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnPassentPlyTest {
    private val on = Board()

    private val underTest = EnPassentPly(
        turn = Side.WHITE,
        from = "e5".loc(),
        to = "d6".loc(),
        passedLoc = "d5".loc()
    )

    @Test
    fun `WHEN executing an en passent THEN the pawn moves and the other pawn is captured`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "e5".loc())
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "d5".loc())

        underTest.exec(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, "d6".loc()))
        on.forEachPiece(Side.WHITE) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals("d6".loc(), locus)
        }
        on.forEachPiece(Side.BLACK) { piece, locus ->
            throw IllegalStateException("Not expecting $piece at $locus")
        }
        assertTrue(on.isEmpty("e5".loc()))
        assertTrue(on.isEmpty("d5".loc()))
    }

    @Test
    fun `WHEN piece is present where the pawn should end up THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "e5".loc())
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = "d6".loc())

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN there is no pawn to capture en passent THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "e5".loc())

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN attempting to capture en passent a non-pawn THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "e5".loc())
        on.add(piece = Piece.Knight, side = Side.BLACK, at = "d6".loc())

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN undoing an en passent THEN the pawn moves back and the other pawn is put back`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "d6".loc())

        underTest.undo(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, "e5".loc()))
        assertTrue(on.has(Piece.Pawn, Side.BLACK, "d5".loc()))
        on.forEachPiece(Side.WHITE) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals("e5".loc(), locus)
        }
        on.forEachPiece(Side.BLACK) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals("d5".loc(), locus)
        }
        assertTrue(on.isEmpty("d6".loc()))
    }

    @Test
    fun `GIVEN destination is occupied WHEN undoing an en passent THEN throw`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = "d6".loc())
        on.add(piece = Piece.Knight, side = Side.BLACK, at = "e5".loc())

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
        assertThrows<AssertionError> { underTest.accept(Piece.Bishop) }
    }
}
