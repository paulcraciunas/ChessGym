package com.paulcraciunas.game.logic.plies

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StandardPlyTest {
    private val on = Board()

    @Test
    fun `WHEN executing a ply THEN that piece is moved on the board`() {
        on.add(ply.piece, ply.turn, ply.from)

        ply.exec(on)

        assertTrue(on.has(ply.piece, ply.turn, ply.to))
        assertFalse(on.has(ply.piece, ply.turn, ply.from))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `WHEN executing a capture THEN the piece is moved and the target captured`() {
        on.add(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.from)
        on.add(plyWithCapture.captured()!!, plyWithCapture.turn.other(), plyWithCapture.to)

        plyWithCapture.exec(on)

        assertTrue(on.has(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.to))
        assertFalse(on.has(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.from))
        assertTrue(plyWithCapture.isPawnMoveOrCapture())
    }

    @Test
    fun `WHEN the capture is incorrect THEN then throw`() {
        on.add(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.from)
        on.add(Piece.Bishop, plyWithCapture.turn.other(), plyWithCapture.to)

        assertThrows<AssertionError> {
            plyWithCapture.exec(on)
        }
    }

    @Test
    fun `WHEN undoing a ply THEN that piece is moved back on the board`() {
        on.add(ply.piece, ply.turn, ply.to)

        ply.undo(on)

        assertTrue(on.has(ply.piece, ply.turn, ply.from))
        assertFalse(on.has(ply.piece, ply.turn, ply.to))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `WHEN undoing a capture THEN the captured piece is put back on the board`() {
        on.add(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.to)

        plyWithCapture.undo(on)

        assertTrue(on.has(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.from))
        assertTrue(
            on.has(plyWithCapture.captured()!!, plyWithCapture.turn.other(), plyWithCapture.to)
        )
        assertTrue(plyWithCapture.isPawnMoveOrCapture())
    }

    @Test
    fun `WHEN serializing to algebraic notation THEN return correct string`() {
        assertEquals("Rc7", ply.algebraic())
        assertEquals("Qxf7", plyWithCapture.algebraic())
        assertEquals("e4", StandardPly(Side.WHITE, Piece.Pawn, Locus.e2, Locus.e4).algebraic())
    }

    @Test
    fun `WHEN serializing pawn capture THEN return correct string`() {
        assertEquals(
            "exd5", StandardPly(
                Side.WHITE,
                Piece.Pawn,
                Locus.e4,
                Locus.d5,
                captured = Piece.Pawn
            ).algebraic()
        )
    }

    @Test
    fun `WHEN serializing with file ambiguity THEN return correct string`() {
        assertEquals(
            "Nbd2", StandardPly(
                Side.WHITE,
                Piece.Knight,
                Locus.b1,
                Locus.d2,
                disambiguate = Ply.Disambiguate.File
            ).algebraic()
        )
    }

    @Test
    fun `WHEN serializing with rank ambiguity THEN return correct string`() {
        assertEquals(
            "R1xa7", StandardPly(
                Side.WHITE,
                Piece.Rook,
                Locus.a1,
                Locus.a7,
                captured = Piece.Queen,
                disambiguate = Ply.Disambiguate.Rank
            ).algebraic()
        )
    }

    private companion object {
        private val ply = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Rook,
            from = Locus.c3,
            to = Locus.c7,
            captured = null
        )
        private val plyWithCapture = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Queen,
            from = Locus.f3,
            to = Locus.f7,
            captured = Piece.Pawn
        )
    }
}