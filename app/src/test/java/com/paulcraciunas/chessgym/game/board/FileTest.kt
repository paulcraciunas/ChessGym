package com.paulcraciunas.chessgym.game.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class FileTest {
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

    companion object {
        @JvmStatic
        fun nextFiles(): List<Arguments> =
            mutableListOf<Arguments>().apply {
                add(Arguments.of(File.a, File.b))
                add(Arguments.of(File.b, File.c))
                add(Arguments.of(File.c, File.d))
                add(Arguments.of(File.d, File.e))
                add(Arguments.of(File.e, File.f))
                add(Arguments.of(File.f, File.g))
                add(Arguments.of(File.g, File.h))
                add(Arguments.of(File.h, null))
            }

        @JvmStatic
        fun prevFiles(): List<Arguments> =
            mutableListOf<Arguments>().apply {
                add(Arguments.of(File.a, null))
                add(Arguments.of(File.b, File.a))
                add(Arguments.of(File.c, File.b))
                add(Arguments.of(File.d, File.c))
                add(Arguments.of(File.e, File.d))
                add(Arguments.of(File.f, File.e))
                add(Arguments.of(File.g, File.f))
                add(Arguments.of(File.h, File.g))
            }
    }
}