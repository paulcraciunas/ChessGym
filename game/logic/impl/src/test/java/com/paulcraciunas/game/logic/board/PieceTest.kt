package com.paulcraciunas.game.logic.board

import com.paulcraciunas.game.logic.api.board.Piece
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PieceTest {
    @Test
    fun `WHEN calling code THEN return the 0-based equivalent`() {
        assertEquals(0, Piece.Pawn.code)
        assertEquals(1, Piece.Rook.code)
        assertEquals(2, Piece.Knight.code)
        assertEquals(3, Piece.Bishop.code)
        assertEquals(4, Piece.Queen.code)
        assertEquals(5, Piece.King.code)
    }

    @Test
    fun `WHEN calling algebraic notation THEN return correct string`() {
        assertEquals("", Piece.Pawn.alg())
        assertEquals("R", Piece.Rook.alg())
        assertEquals("N", Piece.Knight.alg())
        assertEquals("B", Piece.Bishop.alg())
        assertEquals("Q", Piece.Queen.alg())
        assertEquals("K", Piece.King.alg())
    }

    @Test
    fun `WHEN creating from invalid decimal THEN throw`() {
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 5]") {
            Piece.fromCode(-1)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 5]") {
            Piece.fromCode(6)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 5]") {
            Piece.fromCode(Int.MAX_VALUE)
        }
    }

    @Test
    fun `WHEN creating from valid algebraic notation THEN return correct piece`() {
        assertEquals(Piece.Pawn, Piece.fromAlgebraic(""))
        assertEquals(Piece.Queen, Piece.fromAlgebraic("Q"))
        assertEquals(Piece.Rook, Piece.fromAlgebraic("R"))
        assertEquals(Piece.Knight, Piece.fromAlgebraic("N"))
        assertEquals(Piece.Bishop, Piece.fromAlgebraic("B"))
        assertEquals(Piece.King, Piece.fromAlgebraic("K"))
    }

    @Test
    fun `WHEN creating from invalid algebraic notation THEN throw exception`() {
        assertThrows<IllegalArgumentException> {
            Piece.fromAlgebraic("P") // Pawn has no algebraic notation
        }
        assertThrows<IllegalArgumentException> {
            Piece.fromAlgebraic("X") // Invalid piece
        }
        assertThrows<IllegalArgumentException> {
            Piece.fromAlgebraic("1") // Number
        }
        assertThrows<IllegalArgumentException> {
            Piece.fromAlgebraic("!") // Special character
        }
        assertThrows<IllegalArgumentException> {
            Piece.fromAlgebraic("AB") // Multiple characters
        }
    }

    @Test
    fun `WHEN creating from algebraic notation with case sensitivity THEN return correct piece`() {
        // Test that only uppercase works
        assertEquals(Piece.Queen, Piece.fromAlgebraic("Q"))
        assertEquals(Piece.Rook, Piece.fromAlgebraic("R"))
        assertEquals(Piece.Knight, Piece.fromAlgebraic("N"))
        assertEquals(Piece.Bishop, Piece.fromAlgebraic("B"))
    }
}
