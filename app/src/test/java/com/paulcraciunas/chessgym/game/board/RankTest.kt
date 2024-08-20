package com.paulcraciunas.chessgym.game.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class RankTest {
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