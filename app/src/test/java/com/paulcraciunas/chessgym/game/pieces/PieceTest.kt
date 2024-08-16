package com.paulcraciunas.chessgym.game.pieces

import com.paulcraciunas.chessgym.game.Delta
import com.paulcraciunas.chessgym.game.Direction
import com.paulcraciunas.chessgym.game.Position
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.toPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class PieceTest {
    private lateinit var underTest: Griffon

    @ParameterizedTest(name = "movesInDirection should return empty if starting at {0} going {1}")
    @MethodSource("noMoves")
    fun `WHEN at dead end THEN movesInDirection returns empty list`(
        startingPos: String,
        direction: Direction
    ) {
        underTest = Griffon(pos = startingPos)

        val result = underTest.addAllMovesInDirection(direction = direction)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `WHEN on a1 THEN movesInDirection RIGHT returns all moves until a8`() {
        underTest = Griffon(pos = "a1")
        val expectedMoves = listOf("b1", "c1", "d1", "e1", "f1", "g1", "h1").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.RIGHT)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on h4 THEN movesInDirection LEFT returns all moves until a4`() {
        underTest = Griffon(pos = "h4")
        val expectedMoves = listOf("g4", "f4", "e4", "d4", "c4", "b4", "a4").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.LEFT)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on a1 THEN movesInDirection TOP returns all moves until a8`() {
        underTest = Griffon(pos = "a1")
        val expectedMoves = listOf("a2", "a3", "a4", "a5", "a6", "a7", "a8").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.TOP)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on e8 THEN movesInDirection DOWN returns all moves until e1`() {
        underTest = Griffon(pos = "e8")
        val expectedMoves = listOf("e7", "e6", "e5", "e4", "e3", "e2", "e1").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.DOWN)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on a1 THEN movesInDirection TOP_RIGHT returns all moves until h8`() {
        underTest = Griffon(pos = "a1")
        val expectedMoves = listOf("b2", "c3", "d4", "e5", "f6", "g7", "h8").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.TOP_RIGHT)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on h1 THEN movesInDirection TOP_LEFT returns all moves until a8`() {
        underTest = Griffon(pos = "h1")
        val expectedMoves = listOf("g2", "f3", "e4", "d5", "c6", "b7", "a8").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.TOP_LEFT)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on a8 THEN movesInDirection DOWN_RIGHT returns all moves until h1`() {
        underTest = Griffon(pos = "a8")
        val expectedMoves = listOf("b7", "c6", "d5", "e4", "f3", "g2", "h1").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.DOWN_RIGHT)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on h8 THEN movesInDirection DOWN_LEFT returns all moves until a1`() {
        underTest = Griffon(pos = "h8")
        val expectedMoves = listOf("g7", "f6", "e5", "d4", "c3", "b2", "a1").map { it.toPosition() }

        val result = underTest.addAllMovesInDirection(Direction.DOWN_LEFT)

        assertEquals(expectedMoves, result)
    }

    @Test
    fun `WHEN on e4 THEN addMoves adds all surrounding squares if allowed`() {
        underTest = Griffon(pos = "e4")
        val expectedMoves =
            listOf("e5", "d5", "d4", "d3", "e3", "f3", "f4", "f5").map { it.toPosition() }

        val result = underTest.getSimpleMoves()

        // We don't care about the order, really
        assertTrue(expectedMoves.containsAll(result))
        assertTrue(result.containsAll(expectedMoves))
    }

    @Test
    fun `WHEN delta is zero THEN addMoves returns empty list`() {
        // This is to prevent an accidental infinite loop, since Delta is an interface and we can't
        // enforce invariants on interface data
        underTest = Griffon(pos = "e4")

        val result = underTest.getInfiniteMoves()

        assertTrue(result.isEmpty())
    }

    companion object {
        @JvmStatic
        fun noMoves(): List<Arguments> =
            listOf(
                Arguments.of("a1", Direction.LEFT),
                Arguments.of("a1", Direction.TOP_LEFT),
                Arguments.of("a2", Direction.TOP_LEFT),
                Arguments.of("h1", Direction.RIGHT),
                Arguments.of("h1", Direction.TOP_RIGHT),
                Arguments.of("h2", Direction.TOP_RIGHT),
                Arguments.of("e1", Direction.DOWN),
                Arguments.of("e8", Direction.TOP),
                Arguments.of("e1", Direction.DOWN_LEFT),
                Arguments.of("a2", Direction.DOWN_LEFT),
                Arguments.of("e1", Direction.DOWN_RIGHT),
                Arguments.of("h2", Direction.DOWN_RIGHT),
            )
    }
}

// If only we could add patches to Chess, like in StarCraft or CS2
private class Griffon(pos: String) : Piece(Side.WHITE, pos.toPosition()) {
    override fun allPossibleMoves(): List<Position> =
        TODO("It's unclear how it would move, but presumably it would fly all over")

    fun addAllMovesInDirection(direction: Direction): List<Position> =
        mutableListOf<Position>().apply { addMovesInDirection(direction) }

    fun getSimpleMoves(): List<Position> =
        mutableListOf<Position>().apply { addMoves(Direction.entries) }

    fun getInfiniteMoves(): List<Position> =
        mutableListOf<Position>().apply { addMoves(listOf(InfiniteMove())) }

    private class InfiniteMove(override val dX: Int = 0, override val dY: Int = 0) : Delta

    override val symbol: Char = 'G'
    override val value: Int = Int.MAX_VALUE
}
