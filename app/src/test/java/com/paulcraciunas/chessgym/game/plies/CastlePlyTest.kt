package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CastlePlyTest {
    private val on = Board()

    @Test
    fun `GIVEN white moving WHEN castling kingSide for THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus(File.e, Rank.`1`))
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.h, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus(File.g, Rank.`1`)))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus(File.f, Rank.`1`)))
    }

    @Test
    fun `GIVEN position occupied WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus(File.e, Rank.`1`))
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.h, Rank.`1`))
        on.add(piece = Piece.Knight, side = Side.WHITE, at = Locus(File.g, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN king missing WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.h, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN rook missing WHEN castling kingSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.h, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN white moving WHEN undoing kingSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus(File.g, Rank.`1`))
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.f, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.KingSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus(File.h, Rank.`1`)))
    }

    @Test
    fun `GIVEN white moving WHEN castling queenSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus(File.e, Rank.`1`))
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.a, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus(File.c, Rank.`1`)))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus(File.d, Rank.`1`)))
    }

    @Test
    fun `GIVEN position occupied WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus(File.e, Rank.`1`))
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.a, Rank.`1`))
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = Locus(File.c, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN king missing WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.a, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN rook missing WHEN castling queenSide THEN throw`() {
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.a, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        assertThrows<AssertionError> { ply.exec(on) }
    }

    @Test
    fun `GIVEN white moving WHEN undoing queenSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.WHITE, at = Locus(File.c, Rank.`1`))
        on.add(piece = Piece.Rook, side = Side.WHITE, at = Locus(File.d, Rank.`1`))
        val ply = CastlePly(turn = Side.WHITE, type = CastlePly.Type.QueenSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.WHITE, Locus(File.e, Rank.`1`)))
        assertTrue(on.has(Piece.Rook, Side.WHITE, Locus(File.a, Rank.`1`)))
    }

    @Test
    fun `GIVEN black moving WHEN castling kingSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus(File.e, Rank.`8`))
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(File.h, Rank.`8`))
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.KingSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus(File.g, Rank.`8`)))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus(File.f, Rank.`8`)))
    }

    @Test
    fun `GIVEN black moving WHEN undoing kingSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus(File.g, Rank.`8`))
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(File.f, Rank.`8`))
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.KingSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus(File.h, Rank.`8`)))
    }

    @Test
    fun `GIVEN black moving WHEN castling queenSide THEN the king and rook move correctly`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus(File.e, Rank.`8`))
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(File.a, Rank.`8`))
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.QueenSide)

        ply.exec(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus(File.c, Rank.`8`)))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus(File.d, Rank.`8`)))
    }

    @Test
    fun `GIVEN black moving WHEN undoing queenSide castle THEN the king and rook move back`() {
        on.add(piece = Piece.King, side = Side.BLACK, at = Locus(File.c, Rank.`8`))
        on.add(piece = Piece.Rook, side = Side.BLACK, at = Locus(File.d, Rank.`8`))
        val ply = CastlePly(turn = Side.BLACK, type = CastlePly.Type.QueenSide)

        ply.undo(on)

        assertTrue(on.has(Piece.King, Side.BLACK, Locus(File.e, Rank.`8`)))
        assertTrue(on.has(Piece.Rook, Side.BLACK, Locus(File.a, Rank.`8`)))
    }
}