package com.paulcraciunas.chessgym.game.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FileTest {
    @ParameterizedTest(name = "next of {0} is {1}")
    @MethodSource("nextFiles")
    fun `WHEN calling next() THEN return the next file`(current: File, next: File?) {
        assertEquals(next, current.next())
    }

    @ParameterizedTest(name = "prev of {0} is {1}")
    @MethodSource("prevFiles")
    fun `WHEN calling prev() THEN return the previous file`(current: File, prev: File?) {
        assertEquals(prev, current.prev())
    }

    @Test
    fun `WHEN calling dec() THEN return the 0-based equivalent`() {
        assertEquals(0, File.a.dec())
        assertEquals(1, File.b.dec())
        assertEquals(2, File.c.dec())
        assertEquals(3, File.d.dec())
        assertEquals(4, File.e.dec())
        assertEquals(5, File.f.dec())
        assertEquals(6, File.g.dec())
        assertEquals(7, File.h.dec())
    }

    @Test
    fun `WHEN creating from decimal THEN return 0-based equivalent file`() {
        assertEquals(File.a, File.fromDec(0))
        assertEquals(File.b, File.fromDec(1))
        assertEquals(File.c, File.fromDec(2))
        assertEquals(File.d, File.fromDec(3))
        assertEquals(File.e, File.fromDec(4))
        assertEquals(File.f, File.fromDec(5))
        assertEquals(File.g, File.fromDec(6))
        assertEquals(File.h, File.fromDec(7))
    }

    @Test
    fun `WHEN creating from invalid decimal THEN throw`() {
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 7]") {
            File.fromDec(-1)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 7]") {
            File.fromDec(8)
        }
        assertThrows<IllegalArgumentException>("Wrong decimal value. Expecting [0 - 7]") {
            File.fromDec(Int.MAX_VALUE)
        }
    }

    @Test
    fun `WHEN loading from char THEN return correct file`() {
        assertEquals(File.a, 'a'.toFile())
        assertEquals(File.b, 'b'.toFile())
        assertEquals(File.c, 'c'.toFile())
        assertEquals(File.d, 'd'.toFile())
        assertEquals(File.e, 'e'.toFile())
        assertEquals(File.f, 'f'.toFile())
        assertEquals(File.g, 'g'.toFile())
        assertEquals(File.h, 'h'.toFile())
    }

    @Test
    fun `WHEN loading from invalid char THEN return null`() {
        assertNull('0'.toFile())
        assertNull('i'.toFile())
        assertNull('j'.toFile())
        assertNull('z'.toFile())
        assertNull('/'.toFile())
        assertNull('%'.toFile())
    }

    companion object {
        @JvmStatic
        fun nextFiles(): List<Arguments> =
            listOf<Arguments>(
                Arguments.of(File.a, File.b),
                Arguments.of(File.b, File.c),
                Arguments.of(File.c, File.d),
                Arguments.of(File.d, File.e),
                Arguments.of(File.e, File.f),
                Arguments.of(File.f, File.g),
                Arguments.of(File.g, File.h),
                Arguments.of(File.h, null),
            )

        @JvmStatic
        fun prevFiles(): List<Arguments> =
            listOf<Arguments>(
                Arguments.of(File.a, null),
                Arguments.of(File.b, File.a),
                Arguments.of(File.c, File.b),
                Arguments.of(File.d, File.c),
                Arguments.of(File.e, File.d),
                Arguments.of(File.f, File.e),
                Arguments.of(File.g, File.f),
                Arguments.of(File.h, File.g),
            )
    }
}