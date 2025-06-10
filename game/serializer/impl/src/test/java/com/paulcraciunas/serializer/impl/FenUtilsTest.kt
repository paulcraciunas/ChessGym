package com.paulcraciunas.serializer.impl

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.loc
import com.paulcraciunas.serializer.api.SerializeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FenUtilsTest {
    @Test
    fun `WHEN invalid en passent location THEN throw`() {
        assertThrows<SerializeException>("Invalid en-passent location: a9") {
            "a9".loadEnPassent()
        }
    }

    @Test
    fun `WHEN invalid en passent rank THEN throw`() {
        assertThrows<SerializeException>("Invalid en-passent rank: 2") {
            "a2".loadEnPassent()
        }
    }

    @Test
    fun `WHEN en passent is missing THEN return null`() {
        assertNull("-".loadEnPassent())
    }

    @Test
    fun `WHEN valid white en passent THEN return correct ply`() {
        "a3".loadEnPassent()?.let {
            assertEquals(Side.WHITE, it.turn)
            assertEquals(Piece.Pawn, it.piece)
            assertEquals("a2".loc(), it.from)
            assertEquals("a4".loc(), it.to)
        } ?: fail()
    }

    @Test
    fun `WHEN valid black en passent THEN return correct ply`() {
        "g6".loadEnPassent()?.let {
            assertEquals(Side.BLACK, it.turn)
            assertEquals(Piece.Pawn, it.piece)
            assertEquals("g7".loc(), it.from)
            assertEquals("g5".loc(), it.to)
        } ?: fail()
    }
}