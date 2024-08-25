package com.paulcraciunas.game.board

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.Side.BLACK
import com.paulcraciunas.game.Side.WHITE
import com.paulcraciunas.game.board.File.a
import com.paulcraciunas.game.board.File.b
import com.paulcraciunas.game.board.File.c
import com.paulcraciunas.game.board.File.d
import com.paulcraciunas.game.board.File.e
import com.paulcraciunas.game.board.File.f
import com.paulcraciunas.game.board.File.g
import com.paulcraciunas.game.board.File.h
import com.paulcraciunas.game.board.Rank.`1`
import com.paulcraciunas.game.board.Rank.`2`
import com.paulcraciunas.game.board.Rank.`3`
import com.paulcraciunas.game.board.Rank.`4`
import com.paulcraciunas.game.board.Rank.`5`
import com.paulcraciunas.game.board.Rank.`6`
import com.paulcraciunas.game.board.Rank.`7`
import com.paulcraciunas.game.board.Rank.`8`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class BoardTest {

    private val underTest = Board()

    @Test
    fun `WHEN creating a new board THEN it has no pieces`() {
        underTest.forEach { piece, locus ->
            throw IllegalStateException("$piece at $locus not expected")
        }
        underTest.forEachPiece(WHITE) { piece, locus ->
            throw IllegalStateException("$piece at $locus not expected")
        }
        Locus.all {
            assertFalse(underTest.has(BLACK, at = it))
            assertFalse(underTest.has(WHITE, at = it))
            assertTrue(underTest.isEmpty(at = it))
            assertNull(underTest.at(at = it))
        }
        assertNull(underTest.king(WHITE))
        assertNull(underTest.king(BLACK))
    }

    @ParameterizedTest(name = "Adding {0} to the board works as expected")
    @EnumSource(value = Piece::class)
    fun `WHEN adding a new piece THEN that piece can be found`(expected: Piece) {
        val at = Locus(e, `5`)

        underTest.add(piece = expected, side = WHITE, at = at)

        assertFalse(underTest.has(BLACK, at = at))
        assertFalse(underTest.has(expected, BLACK, at = at))
        assertTrue(underTest.has(WHITE, at = at))
        assertTrue(underTest.has(expected, WHITE, at = at))
        assertFalse(underTest.isEmpty(at = at))
        underTest.forEachPiece(WHITE) { piece, locus ->
            assertEquals(expected, piece)
            assertEquals(locus, at)
        }
        underTest.forEach { piece, locus ->
            assertEquals(expected, piece)
            assertEquals(locus, at)
        }
        if (expected == Piece.King) {
            assertEquals(at, underTest.king(WHITE))
            assertNull(underTest.king(BLACK))
        }
    }

    @ParameterizedTest(name = "Cannot add {0} to the board at an occupied square")
    @EnumSource(value = Piece::class)
    fun `GIVEN a piece at a location WHEN adding a piece of the same side THEN throw`(expected: Piece) {
        val at = Locus(f, `4`)
        underTest.add(piece = Piece.Queen, side = WHITE, at = at)

        assertThrows<AssertionError> {
            underTest.add(piece = expected, side = WHITE, at = at)
        }
    }

    @ParameterizedTest(name = "Cannot add {0} to the board at an occupied square")
    @EnumSource(value = Piece::class)
    fun `GIVEN a piece at a location WHEN adding a piece of opposite same side THEN throw`(expected: Piece) {
        val at = Locus(c, `2`)
        underTest.add(piece = Piece.King, side = WHITE, at = at)

        assertThrows<AssertionError> {
            underTest.add(piece = expected, side = BLACK, at = at)
        }
    }

    @Test
    fun `WHEN loading board from another THEN the two boards have the same layout`() {
        val other = Board()
        addSomePieces(other)

        underTest.from(other)

        // Verify the boards are identical in every way
        underTest.forEach { piece, locus ->
            Side.entries.forEach {
                assertEquals(other.has(piece, it, locus), underTest.has(piece, it, locus))
                assertEquals(other.has(it, locus), underTest.has(it, locus))
            }
            assertEquals(piece, other.at(locus))
            assertEquals(piece, underTest.at(locus))
        }
        Side.entries.forEach {
            assertEquals(other.king(it), underTest.king(it))
            underTest.forEachPiece(it) { piece, locus ->
                assertEquals(piece, other.at(locus))
                assertEquals(piece, underTest.at(locus))
            }
        }
        Locus.all {
            assertEquals(other.at(at = it), underTest.at(at = it))
        }
    }

    @Test
    fun `WHEN comparing similar boards THEN they are equal and have the same hashcode`() {
        val other = Board()

        addSomePieces(other)
        addSomePieces(underTest)

        assertEquals(other, underTest)
        assertTrue(other == underTest)
        assertEquals(other.hashCode(), underTest.hashCode())
    }

    @Test
    fun `WHEN comparing different boards THEN they are not equal and have different hashcode`() {
        val other = Board()

        addSomePieces(other)
        addSomePieces(underTest)
        underTest.remove(Locus(a, `2`))

        assertNotEquals(other, underTest)
        assertFalse(other == underTest)
        assertNotEquals(other.hashCode(), underTest.hashCode())
    }

    @Test
    fun `WHEN removing an existing piece THEN that piece can no longer be found`() {
        val at = Locus(d, `5`)
        underTest.add(Piece.King, WHITE, at)

        val removed = underTest.remove(at)

        assertEquals(Piece.King, removed)
        assertTrue(underTest.isEmpty(at))
    }

    @Test
    fun `WHEN removing a non-existing piece THEN return null`() {
        assertNull(underTest.remove(Locus(a, `2`)))
    }

    @Test
    fun `WHEN removing a piece from a copied board THEN the original board is unaffected`() {
        val other = Board()
        other.add(Piece.Pawn, WHITE, Locus(a, `2`))

        underTest.from(other)
        other.remove(Locus(a, `2`))

        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(a, `2`)))
    }

    @Test
    fun `WHEN moving at an empty location THEN throw`() {
        assertThrows<AssertionError> {
            underTest.move(from = Locus(a, `2`), to = Locus(a, `4`), WHITE)
        }
    }

    @Test
    fun `WHEN moving an existing piece THEN that piece is at the new location`() {
        val from = Locus(a, `2`)
        val to = Locus(a, `4`)
        underTest.add(Piece.Rook, WHITE, from)

        val captured = underTest.move(from = from, to = to, WHITE)

        assertNull(captured)
        assertFalse(underTest.has(Piece.Rook, WHITE, from))
        assertTrue(underTest.has(Piece.Rook, WHITE, to))
    }

    @Test
    fun `WHEN capturing a piece THEN that piece is returned`() {
        underTest.add(Piece.Bishop, WHITE, Locus(e, `5`))
        underTest.add(Piece.Pawn, BLACK, Locus(d, `4`))

        val captured = underTest.move(from = Locus(d, `4`), to = Locus(e, `5`), BLACK)

        assertEquals(Piece.Bishop, captured)
        assertTrue(underTest.has(Piece.Pawn, BLACK, Locus(e, `5`)))
        assertNull(underTest.at(Locus(d, `4`)))
    }

    @Test
    fun `WHEN capturing a piece from the same side THEN throw`() {
        underTest.add(Piece.Bishop, WHITE, Locus(e, `5`))
        underTest.add(Piece.Pawn, WHITE, Locus(d, `4`))

        assertThrows<AssertionError> {
            underTest.move(from = Locus(d, `4`), to = Locus(e, `5`), WHITE)
        }
    }

    private fun addSomePieces(on: Board) {
        on.add(Piece.Pawn, WHITE, Locus(a, `2`))
        on.add(Piece.Rook, WHITE, Locus(a, `1`))
        on.add(Piece.Knight, WHITE, Locus(b, `2`))
        on.add(Piece.Bishop, WHITE, Locus(c, `3`))
        on.add(Piece.Queen, WHITE, Locus(d, `4`))
        on.add(Piece.King, WHITE, Locus(d, `5`))
        on.add(Piece.King, BLACK, Locus(e, `5`))
        on.add(Piece.Bishop, BLACK, Locus(f, `6`))
        on.add(Piece.Knight, BLACK, Locus(g, `7`))
        on.add(Piece.Rook, BLACK, Locus(h, `8`))
        on.add(Piece.Pawn, BLACK, Locus(h, `6`))
    }
}
