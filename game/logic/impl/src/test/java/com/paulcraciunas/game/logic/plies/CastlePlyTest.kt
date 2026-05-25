package com.paulcraciunas.game.logic.plies

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.CastlePly
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
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.h1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.KingSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus.g1))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus.f1))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN position occupied WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.h1)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = Locus.g1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN king missing WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.h1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN rook missing WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.h1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN white moving WHEN undoing kingSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.g1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.f1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.KingSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus.e1))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus.h1))
    }

    @Test
    fun `GIVEN white moving WHEN castling queenSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.a1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.QueenSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus.c1))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus.d1))
    }

    @Test
    fun `GIVEN position occupied WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.e1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.a1)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = Locus.c1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN king missing WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.a1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN rook missing WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.a1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN white moving WHEN undoing queenSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus.c1)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus.d1)
        val ply = CastlePly(turn = Side.WHITE, type = CastleType.QueenSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus.e1))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus.a1))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN castling kingSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.e8)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.h8)
        val ply = CastlePly(turn = Side.BLACK, type = CastleType.KingSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus.g8))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus.f8))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN undoing kingSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.g8)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.f8)
        val ply = CastlePly(turn = Side.BLACK, type = CastleType.KingSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus.e8))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus.h8))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN castling queenSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.e8)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.a8)
        val ply = CastlePly(turn = Side.BLACK, type = CastleType.QueenSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus.c8))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus.d8))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `GIVEN black moving WHEN undoing queenSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus.c8)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus.d8)
        val ply = CastlePly(turn = Side.BLACK, type = CastleType.QueenSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus.e8))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus.a8))
        assertFalse(ply.isPawnMoveOrCapture())
    }

    @Test
    fun `WHEN serializing to algebraic notation THEN return correct string`() {
        assertEquals(
            "O-O",
            CastlePly(turn = Side.BLACK, type = CastleType.KingSide).algebraic()
        )
        assertEquals(
            "O-O",
            CastlePly(turn = Side.WHITE, type = CastleType.KingSide).algebraic()
        )
        assertEquals(
            "O-O-O",
            CastlePly(turn = Side.BLACK, type = CastleType.QueenSide).algebraic()
        )
        assertEquals(
            "O-O-O",
            CastlePly(turn = Side.WHITE, type = CastleType.QueenSide).algebraic()
        )
    }

    @Test
    fun `WHEN checking extraPass THEN return correct Locus`() {
        assertEquals(Locus.b1, CastleType.QueenSide.extraPass(Side.WHITE))
        assertEquals(Locus.b8, CastleType.QueenSide.extraPass(Side.BLACK))
        assertEquals(null, CastleType.KingSide.extraPass(Side.WHITE))
        assertEquals(null, CastleType.KingSide.extraPass(Side.BLACK))
    }

    @ParameterizedTest(name = "No captured piece for {1} castling for {0}")
    @MethodSource("castles")
    fun `WHEN getting captured piece THEN return null`(
        side: Side,
        type: CastleType,
    ) {
        assertNull(CastlePly(side, type).captured())
    }

    @ParameterizedTest(name = "Promotion disallowed for {1} castling for {0}")
    @MethodSource("castles")
    fun `WHEN accepting a piece for promotion THEN throw`(
        side: Side,
        type: CastleType,
    ) {
        assertThrows<AssertionError> { CastlePly(side, type).promote(Piece.Bishop) }
    }

    companion object {
        @JvmStatic
        fun castles(): List<Arguments> =
            listOf<Arguments>(
                Arguments.of(Side.BLACK, CastleType.KingSide),
                Arguments.of(Side.WHITE, CastleType.KingSide),
                Arguments.of(Side.BLACK, CastleType.QueenSide),
                Arguments.of(Side.WHITE, CastleType.QueenSide),
            )
    }
}
