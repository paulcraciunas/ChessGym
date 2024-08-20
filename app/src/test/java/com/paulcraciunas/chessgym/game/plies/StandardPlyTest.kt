package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
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
    }

    @Test
    fun `WHEN executing a capture THEN the piece is moved and the target captured`() {
        on.add(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.from)
        on.add(plyWithCapture.captured!!, plyWithCapture.turn.other(), plyWithCapture.to)

        plyWithCapture.exec(on)

        assertTrue(on.has(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.to))
        assertFalse(on.has(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.from))
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
    }

    @Test
    fun `WHEN undoing a capture THEN the captured piece is put back on the board`() {
        on.add(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.to)

        plyWithCapture.undo(on)

        assertTrue(on.has(plyWithCapture.piece, plyWithCapture.turn, plyWithCapture.from))
        assertTrue(on.has(plyWithCapture.captured!!, plyWithCapture.turn.other(), plyWithCapture.to))
    }

    private companion object {
        private val ply = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Rook,
            from = Locus(File.c, Rank.`3`),
            to = Locus(File.c, Rank.`7`),
            captured = null
        )
        private val plyWithCapture = StandardPly(
            turn = Side.WHITE,
            piece = Piece.Queen,
            from = Locus(File.f, Rank.`3`),
            to = Locus(File.f, Rank.`7`),
            captured = Piece.Pawn
        )
    }
}