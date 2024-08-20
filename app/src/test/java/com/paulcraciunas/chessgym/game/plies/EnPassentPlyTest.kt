package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnPassentPlyTest {
    private val on = Board()

    private val underTest = EnPassentPly(
        turn = Side.WHITE,
        from = Locus(File.e, Rank.`5`),
        to = Locus(File.d, Rank.`6`),
        passedLoc = Locus(File.d, Rank.`5`)
    )

    @Test
    fun `WHEN executing an en passent THEN the pawn moves and the other pawn is captured`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus(File.e, Rank.`5`))
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus(File.d, Rank.`5`))

        underTest.exec(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, Locus(File.d, Rank.`6`)))
        on.forEachPiece(Side.WHITE) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals(Locus(File.d, Rank.`6`), locus)
        }
        on.forEachPiece(Side.BLACK) { piece, locus ->
            throw IllegalStateException("Not expecting $piece at $locus")
        }
        assertTrue(on.isEmpty(Locus(File.e, Rank.`5`)))
        assertTrue(on.isEmpty(Locus(File.d, Rank.`5`)))
    }

    @Test
    fun `WHEN piece is present where the pawn should end up THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus(File.e, Rank.`5`))
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = Locus(File.d, Rank.`6`))

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN there is no pawn to capture en passent THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus(File.e, Rank.`5`))

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN attempting to capture en passent a non-pawn THEN exec throws`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus(File.e, Rank.`5`))
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus(File.d, Rank.`6`))

        assertThrows<AssertionError> { underTest.exec(on) }
    }

    @Test
    fun `WHEN undoing an en passent THEN the pawn moves back and the other pawn is put back`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus(File.d, Rank.`6`))

        underTest.undo(on)

        assertTrue(on.has(Piece.Pawn, Side.WHITE, Locus(File.e, Rank.`5`)))
        assertTrue(on.has(Piece.Pawn, Side.BLACK, Locus(File.d, Rank.`5`)))
        on.forEachPiece(Side.WHITE) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals(Locus(File.e, Rank.`5`), locus)
        }
        on.forEachPiece(Side.BLACK) { piece, locus ->
            assertEquals(Piece.Pawn, piece)
            assertEquals(Locus(File.d, Rank.`5`), locus)
        }
        assertTrue(on.isEmpty(Locus(File.d, Rank.`6`)))
    }

    @Test
    fun `GIVEN destination is occupied WHEN undoing an en passent THEN throw`() {
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = Locus(File.d, Rank.`6`))
        on.add(piece = Piece.Knight, side = Side.BLACK, at = Locus(File.e, Rank.`5`))

        assertThrows<AssertionError> { underTest.undo(on) }
    }
}
