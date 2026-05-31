package com.paulcraciunas.serializer.impl

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.api.SerializeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FenUtilsTest {
    private val gameFactory = RealGameFactory()

    private val underTest = gameFactory.builder()

    @Test
    fun `WHEN invalid en passent location THEN throw`() {
        assertThrows<SerializeException>("Invalid en-passent location: a9") {
            underTest.withEnPassent("a9")
        }
    }

    @Test
    fun `WHEN invalid en passent rank THEN throw`() {
        assertThrows<SerializeException>("Invalid en-passent rank: 2") {
            underTest.withEnPassent("a2")
        }
    }

    @Test
    fun `WHEN en passent is missing THEN return null`() {
        assertNull(underTest.withEnPassent("-").buildGame().info.lastPly)
    }

    @Test
    fun `WHEN valid white en passent THEN return correct ply`() {
        underTest.withEnPassent("a3").buildGame().info.lastPly?.let {
            assertEquals(Side.WHITE, it.turn)
            assertEquals(Piece.Pawn, it.piece)
            assertEquals(Locus.a2, it.from)
            assertEquals(Locus.a4, it.to)
        } ?: fail()
    }

    @Test
    fun `WHEN valid black en passent THEN return correct ply`() {
        underTest.withEnPassent("g6").buildGame().info.lastPly?.let {
            assertEquals(Side.BLACK, it.turn)
            assertEquals(Piece.Pawn, it.piece)
            assertEquals(Locus.g7, it.from)
            assertEquals(Locus.g5, it.to)
        } ?: fail()
    }
}