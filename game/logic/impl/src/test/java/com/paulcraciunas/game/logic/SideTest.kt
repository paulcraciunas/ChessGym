package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.Side
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SideTest {
    @Test
    fun `WHEN calling fromCode() THEN return the 0-based equivalent`() {
        assertEquals(0, Side.WHITE.code)
        assertEquals(1, Side.BLACK.code)
    }

    @Test
    fun `WHEN calling other THEN return opposite side`() {
        assertEquals(Side.BLACK, Side.WHITE.other())
        assertEquals(Side.WHITE, Side.BLACK.other())
    }

    @Test
    fun `WHEN creating from invalid decimal THEN throw`() {
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 1]") {
            Side.fromCode(-1)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 1]") {
            Side.fromCode(2)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 1]") {
            Side.fromCode(Int.MAX_VALUE)
        }
    }
}
