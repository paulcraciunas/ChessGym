package com.paulcraciunas.chessgym.game.board

import com.paulcraciunas.chessgym.game.Side.BLACK
import com.paulcraciunas.chessgym.game.Side.WHITE
import com.paulcraciunas.chessgym.game.board.File.a
import com.paulcraciunas.chessgym.game.board.File.b
import com.paulcraciunas.chessgym.game.board.File.c
import com.paulcraciunas.chessgym.game.board.File.d
import com.paulcraciunas.chessgym.game.board.File.e
import com.paulcraciunas.chessgym.game.board.File.f
import com.paulcraciunas.chessgym.game.board.File.g
import com.paulcraciunas.chessgym.game.board.File.h
import com.paulcraciunas.chessgym.game.board.Rank.`1`
import com.paulcraciunas.chessgym.game.board.Rank.`2`
import com.paulcraciunas.chessgym.game.board.Rank.`7`
import com.paulcraciunas.chessgym.game.board.Rank.`8`
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BoardFactoryTest {

    private val underTest = BoardFactory.defaultBoard()

    @Test
    fun `WHEN creating a default board THEN it has all expected starting pieces`() {
        assertTrue(underTest.has(Piece.Rook, WHITE, Locus(a, `1`)))
        assertTrue(underTest.has(Piece.Knight, WHITE, Locus(b, `1`)))
        assertTrue(underTest.has(Piece.Bishop, WHITE, Locus(c, `1`)))
        assertTrue(underTest.has(Piece.Queen, WHITE, Locus(d, `1`)))
        assertTrue(underTest.has(Piece.King, WHITE, Locus(e, `1`)))
        assertTrue(underTest.has(Piece.Bishop, WHITE, Locus(f, `1`)))
        assertTrue(underTest.has(Piece.Knight, WHITE, Locus(g, `1`)))
        assertTrue(underTest.has(Piece.Rook, WHITE, Locus(h, `1`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(a, `2`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(b, `2`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(c, `2`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(d, `2`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(e, `2`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(f, `2`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(g, `2`)))
        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(h, `2`)))
        assertTrue(underTest.has(Piece.Rook, BLACK, Locus(a, `8`)))
        assertTrue(underTest.has(Piece.Knight, BLACK, Locus(b, `8`)))
        assertTrue(underTest.has(Piece.Bishop, BLACK, Locus(c, `8`)))
        assertTrue(underTest.has(Piece.Queen, BLACK, Locus(d, `8`)))
        assertTrue(underTest.has(Piece.King, BLACK, Locus(e, `8`)))
        assertTrue(underTest.has(Piece.Bishop, BLACK, Locus(f, `8`)))
        assertTrue(underTest.has(Piece.Knight, BLACK, Locus(g, `8`)))
        assertTrue(underTest.has(Piece.Rook, BLACK, Locus(h, `8`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(a, `7`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(b, `7`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(c, `7`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(d, `7`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(e, `7`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(f, `7`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(g, `7`)))
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(h, `7`)))
    }

    @Test
    fun `WHEN creating multiple default boards THEN they have their own sets of pieces`() {
        val other = BoardFactory.defaultBoard()

        other.remove(Locus(a, `2`))

        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(a, `2`)))
    }
}
