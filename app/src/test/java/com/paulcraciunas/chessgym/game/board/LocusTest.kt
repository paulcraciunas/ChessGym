package com.paulcraciunas.chessgym.game.board

import com.paulcraciunas.chessgym.game.Side
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LocusTest {

    @Test
    fun `WHEN calling top() THEN return the correct locus`() {
        assertEquals(Locus(File.a, Rank.`2`), Locus(File.a, Rank.`1`).top())
        assertEquals(Locus(File.a, Rank.`3`), Locus(File.a, Rank.`2`).top())
        assertEquals(Locus(File.a, Rank.`8`), Locus(File.a, Rank.`7`).top())
        assertEquals(null, Locus(File.a, Rank.`8`).top())
        assertEquals(Locus(File.h, Rank.`2`), Locus(File.h, Rank.`1`).top())
        assertEquals(Locus(File.h, Rank.`3`), Locus(File.h, Rank.`2`).top())
        assertEquals(Locus(File.h, Rank.`8`), Locus(File.h, Rank.`7`).top())
        assertEquals(null, Locus(File.h, Rank.`8`).top())
    }

    @Test
    fun `WHEN calling down() THEN return the correct locus`() {
        assertEquals(Locus(File.a, Rank.`1`), Locus(File.a, Rank.`2`).down())
        assertEquals(Locus(File.a, Rank.`2`), Locus(File.a, Rank.`3`).down())
        assertEquals(Locus(File.a, Rank.`7`), Locus(File.a, Rank.`8`).down())
        assertEquals(null, Locus(File.a, Rank.`1`).down())
        assertEquals(Locus(File.h, Rank.`1`), Locus(File.h, Rank.`2`).down())
        assertEquals(Locus(File.h, Rank.`2`), Locus(File.h, Rank.`3`).down())
        assertEquals(Locus(File.h, Rank.`7`), Locus(File.h, Rank.`8`).down())
        assertEquals(null, Locus(File.h, Rank.`1`).down())
    }

    @Test
    fun `WHEN calling left() THEN return the correct locus`() {
        assertEquals(Locus(File.a, Rank.`1`), Locus(File.b, Rank.`1`).left())
        assertEquals(Locus(File.b, Rank.`1`), Locus(File.c, Rank.`1`).left())
        assertEquals(Locus(File.g, Rank.`1`), Locus(File.h, Rank.`1`).left())
        assertEquals(null, Locus(File.a, Rank.`1`).left())
        assertEquals(Locus(File.a, Rank.`8`), Locus(File.b, Rank.`8`).left())
        assertEquals(Locus(File.b, Rank.`8`), Locus(File.c, Rank.`8`).left())
        assertEquals(Locus(File.g, Rank.`8`), Locus(File.h, Rank.`8`).left())
        assertEquals(null, Locus(File.a, Rank.`8`).left())
    }

    @Test
    fun `WHEN calling right() THEN return the correct locus`() {
        assertEquals(Locus(File.b, Rank.`1`), Locus(File.a, Rank.`1`).right())
        assertEquals(Locus(File.c, Rank.`1`), Locus(File.b, Rank.`1`).right())
        assertEquals(Locus(File.h, Rank.`1`), Locus(File.g, Rank.`1`).right())
        assertEquals(null, Locus(File.h, Rank.`1`).right())
        assertEquals(Locus(File.b, Rank.`8`), Locus(File.a, Rank.`8`).right())
        assertEquals(Locus(File.c, Rank.`8`), Locus(File.b, Rank.`8`).right())
        assertEquals(Locus(File.h, Rank.`8`), Locus(File.g, Rank.`8`).right())
        assertEquals(null, Locus(File.h, Rank.`8`).right())
    }

    @Test
    fun `WHEN calling topLeft() THEN return the correct locus`() {
        assertEquals(Locus(File.g, Rank.`2`), Locus(File.h, Rank.`1`).topLeft())
        assertEquals(Locus(File.f, Rank.`3`), Locus(File.g, Rank.`2`).topLeft())
        assertEquals(Locus(File.e, Rank.`4`), Locus(File.f, Rank.`3`).topLeft())
        assertEquals(Locus(File.d, Rank.`5`), Locus(File.e, Rank.`4`).topLeft())
        assertEquals(Locus(File.c, Rank.`6`), Locus(File.d, Rank.`5`).topLeft())
        assertEquals(Locus(File.b, Rank.`7`), Locus(File.c, Rank.`6`).topLeft())
        assertEquals(Locus(File.a, Rank.`8`), Locus(File.b, Rank.`7`).topLeft())
        assertEquals(null, Locus(File.a, Rank.`1`).topLeft())
        assertEquals(null, Locus(File.a, Rank.`2`).topLeft())
        assertEquals(null, Locus(File.a, Rank.`8`).topLeft())
        assertEquals(null, Locus(File.d, Rank.`8`).topLeft())
        assertEquals(null, Locus(File.h, Rank.`8`).topLeft())
    }

    @Test
    fun `WHEN calling topRight() THEN return the correct locus`() {
        assertEquals(Locus(File.b, Rank.`2`), Locus(File.a, Rank.`1`).topRight())
        assertEquals(Locus(File.c, Rank.`3`), Locus(File.b, Rank.`2`).topRight())
        assertEquals(Locus(File.d, Rank.`4`), Locus(File.c, Rank.`3`).topRight())
        assertEquals(Locus(File.e, Rank.`5`), Locus(File.d, Rank.`4`).topRight())
        assertEquals(Locus(File.f, Rank.`6`), Locus(File.e, Rank.`5`).topRight())
        assertEquals(Locus(File.g, Rank.`7`), Locus(File.f, Rank.`6`).topRight())
        assertEquals(Locus(File.h, Rank.`8`), Locus(File.g, Rank.`7`).topRight())
        assertEquals(null, Locus(File.h, Rank.`1`).topRight())
        assertEquals(null, Locus(File.h, Rank.`2`).topRight())
        assertEquals(null, Locus(File.h, Rank.`8`).topRight())
        assertEquals(null, Locus(File.e, Rank.`8`).topRight())
        assertEquals(null, Locus(File.a, Rank.`8`).topRight())
    }

    @Test
    fun `WHEN calling downLeft() THEN return the correct locus`() {
        assertEquals(Locus(File.g, Rank.`7`), Locus(File.h, Rank.`8`).downLeft())
        assertEquals(Locus(File.f, Rank.`6`), Locus(File.g, Rank.`7`).downLeft())
        assertEquals(Locus(File.e, Rank.`5`), Locus(File.f, Rank.`6`).downLeft())
        assertEquals(Locus(File.d, Rank.`4`), Locus(File.e, Rank.`5`).downLeft())
        assertEquals(Locus(File.c, Rank.`3`), Locus(File.d, Rank.`4`).downLeft())
        assertEquals(Locus(File.b, Rank.`2`), Locus(File.c, Rank.`3`).downLeft())
        assertEquals(Locus(File.a, Rank.`1`), Locus(File.b, Rank.`2`).downLeft())
        assertEquals(null, Locus(File.a, Rank.`1`).downLeft())
        assertEquals(null, Locus(File.a, Rank.`2`).downLeft())
        assertEquals(null, Locus(File.a, Rank.`8`).downLeft())
        assertEquals(null, Locus(File.b, Rank.`1`).downLeft())
        assertEquals(null, Locus(File.h, Rank.`1`).downLeft())
    }

    @Test
    fun `WHEN calling downRight() THEN return the correct locus`() {
        assertEquals(Locus(File.b, Rank.`7`), Locus(File.a, Rank.`8`).downRight())
        assertEquals(Locus(File.c, Rank.`6`), Locus(File.b, Rank.`7`).downRight())
        assertEquals(Locus(File.d, Rank.`5`), Locus(File.c, Rank.`6`).downRight())
        assertEquals(Locus(File.e, Rank.`4`), Locus(File.d, Rank.`5`).downRight())
        assertEquals(Locus(File.f, Rank.`3`), Locus(File.e, Rank.`4`).downRight())
        assertEquals(Locus(File.g, Rank.`2`), Locus(File.f, Rank.`3`).downRight())
        assertEquals(Locus(File.h, Rank.`1`), Locus(File.g, Rank.`2`).downRight())
        assertEquals(null, Locus(File.h, Rank.`1`).downRight())
        assertEquals(null, Locus(File.h, Rank.`2`).downRight())
        assertEquals(null, Locus(File.h, Rank.`8`).downRight())
        assertEquals(null, Locus(File.f, Rank.`1`).downRight())
        assertEquals(null, Locus(File.a, Rank.`1`).downRight())
    }

    @Test
    fun `WHEN calling side() on black squares THEN return Side BLACK`() {
        assertEquals(Side.BLACK, Locus(File.a, Rank.`1`).side())
        assertEquals(Side.BLACK, Locus(File.b, Rank.`2`).side())
        assertEquals(Side.BLACK, Locus(File.c, Rank.`3`).side())
        assertEquals(Side.BLACK, Locus(File.d, Rank.`4`).side())
        assertEquals(Side.BLACK, Locus(File.e, Rank.`5`).side())
        assertEquals(Side.BLACK, Locus(File.f, Rank.`6`).side())
        assertEquals(Side.BLACK, Locus(File.g, Rank.`7`).side())
        assertEquals(Side.BLACK, Locus(File.h, Rank.`8`).side())
        assertEquals(Side.BLACK, Locus(File.a, Rank.`7`).side())
        assertEquals(Side.BLACK, Locus(File.b, Rank.`6`).side())
        assertEquals(Side.BLACK, Locus(File.c, Rank.`5`).side())
        assertEquals(Side.BLACK, Locus(File.d, Rank.`4`).side())
        assertEquals(Side.BLACK, Locus(File.e, Rank.`3`).side())
        assertEquals(Side.BLACK, Locus(File.f, Rank.`2`).side())
        assertEquals(Side.BLACK, Locus(File.g, Rank.`1`).side())
    }


    @Test
    fun `WHEN calling side() on white squares THEN return Side WHITE`() {
        assertEquals(Side.WHITE, Locus(File.a, Rank.`8`).side())
        assertEquals(Side.WHITE, Locus(File.b, Rank.`7`).side())
        assertEquals(Side.WHITE, Locus(File.c, Rank.`6`).side())
        assertEquals(Side.WHITE, Locus(File.d, Rank.`5`).side())
        assertEquals(Side.WHITE, Locus(File.e, Rank.`4`).side())
        assertEquals(Side.WHITE, Locus(File.f, Rank.`3`).side())
        assertEquals(Side.WHITE, Locus(File.g, Rank.`2`).side())
        assertEquals(Side.WHITE, Locus(File.h, Rank.`1`).side())
        assertEquals(Side.WHITE, Locus(File.a, Rank.`8`).side())
        assertEquals(Side.WHITE, Locus(File.h, Rank.`7`).side())
        assertEquals(Side.WHITE, Locus(File.g, Rank.`6`).side())
        assertEquals(Side.WHITE, Locus(File.f, Rank.`5`).side())
        assertEquals(Side.WHITE, Locus(File.e, Rank.`4`).side())
        assertEquals(Side.WHITE, Locus(File.d, Rank.`3`).side())
        assertEquals(Side.WHITE, Locus(File.c, Rank.`2`).side())
        assertEquals(Side.WHITE, Locus(File.b, Rank.`1`).side())
    }
}
