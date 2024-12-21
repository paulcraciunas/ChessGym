package com.paulcraciunas.game.board

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
}
