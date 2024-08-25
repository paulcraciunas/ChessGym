package com.paulcraciunas.game.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class RankTest {
    @ParameterizedTest(name = "next of {0} is {1}")
    @MethodSource("nextRanks")
    fun `WHEN calling next() THEN return the next rank`(current: Rank, next: Rank?) {
        assertEquals(next, current.next())
    }

    @ParameterizedTest(name = "prev of {0} is {1}")
    @MethodSource("prevRanks")
    fun `WHEN calling prev() THEN return the previous rank`(current: Rank, prev: Rank?) {
        assertEquals(prev, current.prev())
    }

    @Test
    fun `WHEN calling dec() THEN return the 0-based equivalent`() {
        assertEquals(0, Rank.`1`.dec())
        assertEquals(1, Rank.`2`.dec())
        assertEquals(2, Rank.`3`.dec())
        assertEquals(3, Rank.`4`.dec())
        assertEquals(4, Rank.`5`.dec())
        assertEquals(5, Rank.`6`.dec())
        assertEquals(6, Rank.`7`.dec())
        assertEquals(7, Rank.`8`.dec())
    }

    @Test
    fun `WHEN creating from decimal THEN return 0-based equivalent rank`() {
        assertEquals(Rank.`1`, Rank.fromDec(0))
        assertEquals(Rank.`2`, Rank.fromDec(1))
        assertEquals(Rank.`3`, Rank.fromDec(2))
        assertEquals(Rank.`4`, Rank.fromDec(3))
        assertEquals(Rank.`5`, Rank.fromDec(4))
        assertEquals(Rank.`6`, Rank.fromDec(5))
        assertEquals(Rank.`7`, Rank.fromDec(6))
        assertEquals(Rank.`8`, Rank.fromDec(7))
    }

    @Test
    fun `WHEN creating from invalid decimal THEN throw`() {
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 7]") {
            Rank.fromDec(-1)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 7]") {
            Rank.fromDec(8)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 7]") {
            Rank.fromDec(Int.MAX_VALUE)
        }
    }

    @Test
    fun `WHEN loading from char THEN return correct rank`() {
        assertEquals(Rank.`1`, '1'.toRank())
        assertEquals(Rank.`2`, '2'.toRank())
        assertEquals(Rank.`3`, '3'.toRank())
        assertEquals(Rank.`4`, '4'.toRank())
        assertEquals(Rank.`5`, '5'.toRank())
        assertEquals(Rank.`6`, '6'.toRank())
        assertEquals(Rank.`7`, '7'.toRank())
        assertEquals(Rank.`8`, '8'.toRank())
    }

    @Test
    fun `WHEN loading from invalid char THEN return null`() {
        assertNull('a'.toRank())
        assertNull('-'.toRank())
        assertNull('9'.toRank())
        assertNull('0'.toRank())
        assertNull('/'.toRank())
        assertNull('%'.toRank())
    }

    companion object {
        @JvmStatic
        fun nextRanks(): List<Arguments> =
            mutableListOf<Arguments>().apply {
                add(Arguments.of(Rank.`1`, Rank.`2`))
                add(Arguments.of(Rank.`2`, Rank.`3`))
                add(Arguments.of(Rank.`3`, Rank.`4`))
                add(Arguments.of(Rank.`4`, Rank.`5`))
                add(Arguments.of(Rank.`5`, Rank.`6`))
                add(Arguments.of(Rank.`6`, Rank.`7`))
                add(Arguments.of(Rank.`7`, Rank.`8`))
                add(Arguments.of(Rank.`8`, null))
            }

        @JvmStatic
        fun prevRanks(): List<Arguments> =
            mutableListOf<Arguments>().apply {
                add(Arguments.of(Rank.`1`, null))
                add(Arguments.of(Rank.`2`, Rank.`1`))
                add(Arguments.of(Rank.`3`, Rank.`2`))
                add(Arguments.of(Rank.`4`, Rank.`3`))
                add(Arguments.of(Rank.`5`, Rank.`4`))
                add(Arguments.of(Rank.`6`, Rank.`5`))
                add(Arguments.of(Rank.`7`, Rank.`6`))
                add(Arguments.of(Rank.`8`, Rank.`7`))
            }
    }
}