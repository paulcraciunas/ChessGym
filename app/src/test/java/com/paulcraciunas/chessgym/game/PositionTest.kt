package com.paulcraciunas.chessgym.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

class PositionTest {

    @ParameterizedTest(name = "isValid should return true for {0} to {1}")
    @MethodSource("validPositions")
    fun `WHEN digital position within bounds THEN position is valid`(
        x: Int,
        y: Int
    ) {
        assertDoesNotThrow { Position(x, y) }
    }

    @Test
    fun `WHEN digital position outside of bounds THEN position is not valid`() {
        assertThrows<AssertionError> { Position(0, -1) }
        assertThrows<AssertionError> { Position(-1, 0) }
        assertThrows<AssertionError> { Position(8, 0) }
        assertThrows<AssertionError> { Position(0, 8) }
    }

    @ParameterizedTest(name = "toAlgebraic should return {1} for {0}")
    @MethodSource("algebraicChecks")
    fun `WHEN digital position within bounds THEN toAlgebraic is correct`(
        position: Position,
        algebraic: String
    ) {
        assertEquals(algebraic, position.toAlgebraic())
    }

    @ParameterizedTest(name = "Position.from should return {0} for {1}")
    @MethodSource("algebraicChecks")
    fun `WHEN algebraic position within bounds THEN from is correct`(
        position: Position,
        algebraic: String
    ) {
        assertEquals(position, Position.from(algebraic))
    }

    @Test
    fun `WHEN using Direction to compute next THEN new position includes delta`() {
        val position = Position(3, 3)
        assertEquals(Position(3, 4), position.next(Direction.TOP))
        assertEquals(Position(3, 2), position.next(Direction.DOWN))
        assertEquals(Position(2, 3), position.next(Direction.LEFT))
        assertEquals(Position(4, 3), position.next(Direction.RIGHT))
        assertEquals(Position(2, 4), position.next(Direction.TOP_LEFT))
        assertEquals(Position(4, 4), position.next(Direction.TOP_RIGHT))
        assertEquals(Position(2, 2), position.next(Direction.DOWN_LEFT))
        assertEquals(Position(4, 2), position.next(Direction.DOWN_RIGHT))
    }

    @Test
    fun `WHEN going outside of bounds THEN hasNext returns false`() {
        assertFalse(Position(7, 0).hasNext(CustomDelta(1, 0)))
        assertFalse(Position(0, 0).hasNext(CustomDelta(-1, 0)))
        assertFalse(Position(0, 7).hasNext(CustomDelta(0, 1)))
        assertFalse(Position(0, 0).hasNext(CustomDelta(0, -1)))
        assertFalse(Position(0, 0).hasNext(CustomDelta(8, 0)))
        assertFalse(Position(0, 0).hasNext(CustomDelta(-8, 0)))
        assertFalse(Position(0, 0).hasNext(CustomDelta(0, 8)))
        assertFalse(Position(0, 0).hasNext(CustomDelta(0, -8)))
    }

    @ParameterizedTest
    @EnumSource(Direction::class)
    fun `WHEN within bounds THEN hasNext returns true`(direction: Direction) {
        assertTrue(Position(3, 3).hasNext(direction))
    }

    companion object {
        @JvmStatic
        fun validPositions(): List<Arguments> =
            mutableListOf<Arguments>().apply {
                for (x in 0..7) {
                    for (y in 0..7) {
                        add(Arguments.of(x, y))
                    }
                }
            }

        @JvmStatic
        fun algebraicChecks(): List<Arguments> =
            listOf(
                Arguments.of(Position(0, 0), "a1"),
                Arguments.of(Position(0, 1), "a2"),
                Arguments.of(Position(0, 2), "a3"),
                Arguments.of(Position(0, 3), "a4"),
                Arguments.of(Position(0, 4), "a5"),
                Arguments.of(Position(0, 5), "a6"),
                Arguments.of(Position(0, 6), "a7"),
                Arguments.of(Position(0, 7), "a8"),
                Arguments.of(Position(7, 0), "h1"),
                Arguments.of(Position(7, 1), "h2"),
                Arguments.of(Position(7, 2), "h3"),
                Arguments.of(Position(7, 3), "h4"),
                Arguments.of(Position(7, 4), "h5"),
                Arguments.of(Position(7, 5), "h6"),
                Arguments.of(Position(7, 6), "h7"),
                Arguments.of(Position(7, 7), "h8"),
            )

        private class CustomDelta(override val dX: Int, override val dY: Int) : Delta
    }
}
