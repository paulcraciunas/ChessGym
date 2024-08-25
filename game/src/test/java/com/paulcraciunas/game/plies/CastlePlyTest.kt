package com.paulcraciunas.game.plies

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Board
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.loc
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CastlePlyTest {
    private val on = Board()

    @Test
    fun `GIVEN white moving WHEN castling kingSide for THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = "e1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "h1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.WHITE, "g1".loc()))
        assertTrue(on.has(Piece.Rook, Side.WHITE, "f1".loc()))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN position occupied WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = "e1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "h1".loc())
        on.add(piece = Piece.Knight, side = Side.WHITE, at = "g1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN king missing WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "h1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN rook missing WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "h1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN white moving WHEN undoing kingSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = "g1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "f1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.WHITE, "e1".loc()))
        assertTrue(on.has(Piece.Rook, Side.WHITE, "h1".loc()))
    }

    @Test
    fun `GIVEN white moving WHEN castling queenSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = "e1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "a1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.WHITE, "c1".loc()))
        assertTrue(on.has(Piece.Rook, Side.WHITE, "d1".loc()))
    }

    @Test
    fun `GIVEN position occupied WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = "e1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "a1".loc())
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = "c1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN king missing WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "a1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN rook missing WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "a1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN white moving WHEN undoing queenSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = "c1".loc())
        on.add(piece = Piece.Rook, side = Side.WHITE, at = "d1".loc())
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.WHITE, "e1".loc()))
        assertTrue(on.has(Piece.Rook, Side.WHITE, "a1".loc()))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN castling kingSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = "e8".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "h8".loc())
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.KingSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.BLACK, "g8".loc()))
        assertTrue(on.has(Piece.Rook, Side.BLACK, "f8".loc()))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN undoing kingSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = "g8".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "f8".loc())
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.KingSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.BLACK, "e8".loc()))
        assertTrue(on.has(Piece.Rook, Side.BLACK, "h8".loc()))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN castling queenSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = "e8".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "a8".loc())
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.QueenSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.BLACK, "c8".loc()))
        assertTrue(on.has(Piece.Rook, Side.BLACK, "d8".loc()))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN undoing queenSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = "c8".loc())
        on.add(piece = Piece.Rook, side = Side.BLACK, at = "d8".loc())
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.QueenSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.BLACK, "e8".loc()))
        assertTrue(on.has(Piece.Rook, Side.BLACK, "a8".loc()))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `WHEN serializing to algebraic notation THEN return correct string`() {
        assertEquals(
            "O-O",
            CastlePly(turn = Side.BLACK, type = CastlePly.Type.KingSide).algebraic()
        )
        assertEquals(
            "O-O",
            CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide).algebraic()
        )
        assertEquals(
            "O-O-O",
            CastlePly(turn = Side.BLACK, type = CastlePly.Type.QueenSide).algebraic()
        )
        assertEquals(
            "O-O-O",
            CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide).algebraic()
        )
    }

    @Test
    fun `WHEN checking extraPass THEN return correct Locus`() {
        assertEquals("b1".loc(), CastlePly.Type.QueenSide.extraPass(Side.WHITE))
        assertEquals("b8".loc(), CastlePly.Type.QueenSide.extraPass(Side.BLACK))
        assertEquals(null, CastlePly.Type.KingSide.extraPass(Side.WHITE))
        assertEquals(null, CastlePly.Type.KingSide.extraPass(Side.BLACK))
    }

    @ParameterizedTest(name = "No captured piece for {1} castling for {0}")
    @MethodSource("castles")
    fun `WHEN getting captured piece THEN return null`(
        side: Side,
        type: CastlePly.Type,
    ) {
        assertNull(CastlePly(side, type).captured())
    }

    @ParameterizedTest(name = "Promotion disallowed for {1} castling for {0}")
    @MethodSource("castles")
    fun `WHEN accepting a piece for promotion THEN throw`(
        side: Side,
        type: CastlePly.Type,
    ) {
        assertThrows<AssertionError> { CastlePly(side, type).accept(Piece.Bishop) }
    }

    companion object {
        @JvmStatic
        fun castles(): List<Arguments> =
            listOf<Arguments>(
                Arguments.of(Side.BLACK, CastlePly.Type.KingSide),
                Arguments.of(Side.WHITE, CastlePly.Type.KingSide),
                Arguments.of(Side.BLACK, CastlePly.Type.QueenSide),
                Arguments.of(Side.WHITE, CastlePly.Type.QueenSide),
            )
    }
}
