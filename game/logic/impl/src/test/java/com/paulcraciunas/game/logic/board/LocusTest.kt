package com.paulcraciunas.game.logic.board

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class LocusTest {

    @Test
    fun `WHEN calling top() THEN return the correct locus`() {
        assertEquals(Locus.a2, Locus.a1.top())
        assertEquals(Locus.a3, Locus.a2.top())
        assertEquals(Locus.a8, Locus.a7.top())
        assertEquals(null, Locus.a8.top())
        assertEquals(Locus.h2, Locus.h1.top())
        assertEquals(Locus.h3, Locus.h2.top())
        assertEquals(Locus.h8, Locus.h7.top())
        assertEquals(null, Locus.h8.top())
    }

    @Test
    fun `WHEN calling down() THEN return the correct locus`() {
        assertEquals(Locus.a1, Locus.a2.down())
        assertEquals(Locus.a2, Locus.a3.down())
        assertEquals(Locus.a7, Locus.a8.down())
        assertEquals(null, Locus.a1.down())
        assertEquals(Locus.h1, Locus.h2.down())
        assertEquals(Locus.h2, Locus.h3.down())
        assertEquals(Locus.h7, Locus.h8.down())
        assertEquals(null, Locus.h1.down())
    }

    @Test
    fun `WHEN calling left() THEN return the correct locus`() {
        assertEquals(Locus.a1, Locus.b1.left())
        assertEquals(Locus.b1, Locus.c1.left())
        assertEquals(Locus.g1, Locus.h1.left())
        assertEquals(null, Locus.a1.left())
        assertEquals(Locus.a8, Locus.b8.left())
        assertEquals(Locus.b8, Locus.c8.left())
        assertEquals(Locus.g8, Locus.h8.left())
        assertEquals(null, Locus.a8.left())
    }

    @Test
    fun `WHEN calling right() THEN return the correct locus`() {
        assertEquals(Locus.b1, Locus.a1.right())
        assertEquals(Locus.c1, Locus.b1.right())
        assertEquals(Locus.h1, Locus.g1.right())
        assertEquals(null, Locus.h1.right())
        assertEquals(Locus.b8, Locus.a8.right())
        assertEquals(Locus.c8, Locus.b8.right())
        assertEquals(Locus.h8, Locus.g8.right())
        assertEquals(null, Locus.h8.right())
    }

    @Test
    fun `WHEN calling topLeft() THEN return the correct locus`() {
        assertEquals(Locus.g2, Locus.h1.topLeft())
        assertEquals(Locus.f3, Locus.g2.topLeft())
        assertEquals(Locus.e4, Locus.f3.topLeft())
        assertEquals(Locus.d5, Locus.e4.topLeft())
        assertEquals(Locus.c6, Locus.d5.topLeft())
        assertEquals(Locus.b7, Locus.c6.topLeft())
        assertEquals(Locus.a8, Locus.b7.topLeft())
        assertEquals(null, Locus.a1.topLeft())
        assertEquals(null, Locus.a2.topLeft())
        assertEquals(null, Locus.a8.topLeft())
        assertEquals(null, Locus.d8.topLeft())
        assertEquals(null, Locus.h8.topLeft())
    }

    @Test
    fun `WHEN calling topRight() THEN return the correct locus`() {
        assertEquals(Locus.b2, Locus.a1.topRight())
        assertEquals(Locus.c3, Locus.b2.topRight())
        assertEquals(Locus.d4, Locus.c3.topRight())
        assertEquals(Locus.e5, Locus.d4.topRight())
        assertEquals(Locus.f6, Locus.e5.topRight())
        assertEquals(Locus.g7, Locus.f6.topRight())
        assertEquals(Locus.h8, Locus.g7.topRight())
        assertEquals(null, Locus.h1.topRight())
        assertEquals(null, Locus.h2.topRight())
        assertEquals(null, Locus.h8.topRight())
        assertEquals(null, Locus.e8.topRight())
        assertEquals(null, Locus.a8.topRight())
    }

    @Test
    fun `WHEN calling downLeft() THEN return the correct locus`() {
        assertEquals(Locus.g7, Locus.h8.downLeft())
        assertEquals(Locus.f6, Locus.g7.downLeft())
        assertEquals(Locus.e5, Locus.f6.downLeft())
        assertEquals(Locus.d4, Locus.e5.downLeft())
        assertEquals(Locus.c3, Locus.d4.downLeft())
        assertEquals(Locus.b2, Locus.c3.downLeft())
        assertEquals(Locus.a1, Locus.b2.downLeft())
        assertEquals(null, Locus.a1.downLeft())
        assertEquals(null, Locus.a2.downLeft())
        assertEquals(null, Locus.a8.downLeft())
        assertEquals(null, Locus.b1.downLeft())
        assertEquals(null, Locus.h1.downLeft())
    }

    @Test
    fun `WHEN calling downRight() THEN return the correct locus`() {
        assertEquals(Locus.b7, Locus.a8.downRight())
        assertEquals(Locus.c6, Locus.b7.downRight())
        assertEquals(Locus.d5, Locus.c6.downRight())
        assertEquals(Locus.e4, Locus.d5.downRight())
        assertEquals(Locus.f3, Locus.e4.downRight())
        assertEquals(Locus.g2, Locus.f3.downRight())
        assertEquals(Locus.h1, Locus.g2.downRight())
        assertEquals(null, Locus.h1.downRight())
        assertEquals(null, Locus.h2.downRight())
        assertEquals(null, Locus.h8.downRight())
        assertEquals(null, Locus.f1.downRight())
        assertEquals(null, Locus.a1.downRight())
    }

    @Test
    fun `WHEN calling side() on black squares THEN return Side BLACK`() {
        assertEquals(Side.BLACK, Locus.a1.side())
        assertEquals(Side.BLACK, Locus.b2.side())
        assertEquals(Side.BLACK, Locus.c3.side())
        assertEquals(Side.BLACK, Locus.d4.side())
        assertEquals(Side.BLACK, Locus.e5.side())
        assertEquals(Side.BLACK, Locus.f6.side())
        assertEquals(Side.BLACK, Locus.g7.side())
        assertEquals(Side.BLACK, Locus.h8.side())
        assertEquals(Side.BLACK, Locus.a7.side())
        assertEquals(Side.BLACK, Locus.b6.side())
        assertEquals(Side.BLACK, Locus.c5.side())
        assertEquals(Side.BLACK, Locus.d4.side())
        assertEquals(Side.BLACK, Locus.e3.side())
        assertEquals(Side.BLACK, Locus.f2.side())
        assertEquals(Side.BLACK, Locus.g1.side())
    }

    @Test
    fun `WHEN calling side() on white squares THEN return Side WHITE`() {
        assertEquals(Side.WHITE, Locus.a8.side())
        assertEquals(Side.WHITE, Locus.b7.side())
        assertEquals(Side.WHITE, Locus.c6.side())
        assertEquals(Side.WHITE, Locus.d5.side())
        assertEquals(Side.WHITE, Locus.e4.side())
        assertEquals(Side.WHITE, Locus.f3.side())
        assertEquals(Side.WHITE, Locus.g2.side())
        assertEquals(Side.WHITE, Locus.h1.side())
        assertEquals(Side.WHITE, Locus.a8.side())
        assertEquals(Side.WHITE, Locus.h7.side())
        assertEquals(Side.WHITE, Locus.g6.side())
        assertEquals(Side.WHITE, Locus.f5.side())
        assertEquals(Side.WHITE, Locus.e4.side())
        assertEquals(Side.WHITE, Locus.d3.side())
        assertEquals(Side.WHITE, Locus.c2.side())
        assertEquals(Side.WHITE, Locus.b1.side())
    }

    @Test
    fun `WHEN loading from algebraic THEN return correct Locus`() {
        assertEquals(Locus.a8, Locus.from("a8"))
        assertEquals(Locus.b7, Locus.from("b7"))
        assertEquals(Locus.c6, Locus.from("c6"))
        assertEquals(Locus.d5, Locus.from("d5"))
        assertEquals(Locus.e4, Locus.from("e4"))
        assertEquals(Locus.f3, Locus.from("f3"))
        assertEquals(Locus.g2, Locus.from("g2"))
        assertEquals(Locus.h1, Locus.from("h1"))
        assertEquals(Locus.a8, Locus.from("a8"))
        assertEquals(Locus.h7, Locus.from("h7"))
        assertEquals(Locus.g6, Locus.from("g6"))
        assertEquals(Locus.f5, Locus.from("f5"))
        assertEquals(Locus.e4, Locus.from("e4"))
        assertEquals(Locus.d3, Locus.from("d3"))
        assertEquals(Locus.c2, Locus.from("c2"))
        assertEquals(Locus.b1, Locus.from("b1"))
    }

    @Test
    fun `WHEN creating from invalid algebraic THEN return null`() {
        File.entries.forEach {
            assertNull(Locus.from(it.toString()))
        }
        Rank.entries.forEach {
            assertNull(Locus.from(it.toString()))
        }
        assertNull(Locus.from("a9"))
        assertNull(Locus.from("h9"))
        assertNull(Locus.from("i4"))
        assertNull(Locus.from("i1"))
    }
}
