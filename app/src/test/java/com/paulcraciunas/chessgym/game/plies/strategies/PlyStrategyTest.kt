package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Collections.singletonList

internal class PlyStrategyTest {
    private val on = Board().apply {
        add(piece = plyPiece, side = Side.WHITE, at = rookHome)
    }

    private lateinit var underTest: PlyStrategy

    @Test
    fun `GIVEN a single move top WHEN getting plies THEN return the single move`() {
        underTest = TestSingleTop()

        val plies = underTest.plies(from = rookHome, on = on, with = with)

        assertEquals(1, plies.size)
        assertInstanceOf(StandardPly::class.java, plies[0])
        assertEquals(side, plies[0].turn)
        assertEquals(plyPiece, plies[0].piece)
        assertEquals(rookHome, plies[0].from)
        assertEquals(rookHome.top(), plies[0].to)
    }

    @Test
    fun `GIVEN a single move top WHEN blocked THEN return nothing`() {
        underTest = TestSingleTop()
        on.add(piece = plyPiece, side = Side.WHITE, at = rookHome.top()!!)

        val plies = underTest.plies(from = rookHome, on = on, with = with)

        assertTrue(plies.isEmpty())
    }

    @Test
    fun `GIVEN missing piece at start WHEN getting single move THEN throw`() {
        underTest = TestSingleTop()
        on.remove(at = rookHome)

        assertThrows<AssertionError> { underTest.plies(from = rookHome, on = on, with = with) }
    }

    @Test
    fun `GIVEN a single move top WHEN checking attacks THEN return the single move`() {
        underTest = TestSingleTop()

        val canAttack =
            underTest.canAttack(from = rookHome, to = rookHome.top()!!, on = on, turn = side)

        assertEquals(true, canAttack)
    }

    @Test
    fun `GIVEN missing piece at start WHEN checking attacks for single move THEN throw`() {
        underTest = TestSingleTop()
        on.remove(at = rookHome)

        assertThrows<AssertionError> {
            underTest.canAttack(from = rookHome, to = rookHome.top()!!, on = on, turn = side)
        }
    }

    @Test
    fun `GIVEN a direction move top WHEN getting plies THEN return all the moves`() {
        underTest = TestSingleTopDirection()

        val plies = underTest.plies(from = rookHome, on = on, with = with)

        assertEquals(4, plies.size)
        var dest: Locus = rookHome
        plies.forEach {
            dest = dest.top()!!
            assertInstanceOf(StandardPly::class.java, it)
            assertEquals(side, it.turn)
            assertEquals(plyPiece, it.piece)
            assertEquals(rookHome, it.from)
            assertEquals(dest, it.to)
        }
    }

    @Test
    fun `GIVEN a direction move top WHEN blocked THEN return nothing`() {
        underTest = TestSingleTopDirection()
        on.add(piece = plyPiece, side = Side.WHITE, at = rookHome.top()!!)

        val plies = underTest.plies(from = rookHome, on = on, with = with)

        assertTrue(plies.isEmpty())
    }

    @Test
    fun `GIVEN missing piece at start WHEN getting direction move THEN throw`() {
        underTest = TestSingleTopDirection()
        on.remove(at = rookHome)

        assertThrows<AssertionError> { underTest.plies(from = rookHome, on = on, with = with) }
    }

    @Test
    fun `GIVEN a direction move top WHEN checking attacks THEN return a single move`() {
        underTest = TestSingleTopDirection()

        val canAttack =
            underTest.canAttack(from = rookHome, to = rookHome.top()!!, on = on, turn = side)

        assertEquals(true, canAttack)
    }

    @Test
    fun `GIVEN missing piece at start WHEN checking attacks for direction move THEN throw`() {
        underTest = TestSingleTopDirection()

        on.remove(at = rookHome)

        assertThrows<AssertionError> {
            underTest.canAttack(from = rookHome, to = rookHome.top()!!, on = on, turn = side)
        }
    }

    @Test
    fun `GIVEN a complex move top WHEN getting plies THEN return the complex move`() {
        underTest = TestComplexMove()

        val plies = underTest.plies(from = rookHome, on = on, with = with)

        assertEquals(1, plies.size)
        assertInstanceOf(StandardPly::class.java, plies[0])
        assertEquals(side, plies[0].turn)
        assertEquals(plyPiece, plies[0].piece)
        assertEquals(rookHome, plies[0].from)
        assertEquals(rookHome.top(), plies[0].to)
    }

    @Test
    fun `GIVEN missing piece at start WHEN getting complex move THEN throw`() {
        underTest = TestComplexMove()
        on.remove(at = rookHome)

        assertThrows<AssertionError> { underTest.plies(from = rookHome, on = on, with = with) }
    }

    @Test
    fun `GIVEN a complex move top WHEN checking attacks THEN return nothing`() {
        underTest = TestComplexMove()

        val canAttack =
            underTest.canAttack(from = rookHome, to = rookHome.top()!!, on = on, turn = side)

        assertEquals(false, canAttack)
    }

    @Test
    fun `GIVEN missing piece at start WHEN checking attacks for complex move THEN throw`() {
        underTest = TestComplexMove()
        on.remove(at = rookHome)

        assertThrows<AssertionError> {
            underTest.canAttack(from = rookHome, to = rookHome.top()!!, on = on, turn = side)
        }
    }

    private class TestSingleTop : PlyStrategy() {
        override val piece: Piece = plyPiece
        override fun simpleMoves(): Collection<Next> = singletonList(Locus::top)
    }

    private class TestSingleTopDirection : PlyStrategy() {
        override val piece: Piece = plyPiece
        override fun directions(): Collection<Next> = singletonList(Locus::top)
    }

    private class TestComplexMove : PlyStrategy() {
        override val piece: Piece = plyPiece
        override fun MutableList<Ply>.addComplexPlies(from: Locus, on: Board, with: GameState) {
            add(StandardPly(side, plyPiece, from = from, to = from.top()!!))
        }
    }

    private companion object {
        private val plyPiece = Piece.Rook
        private val side = Side.WHITE
        private val rookHome = Locus(File.e, Rank.`4`)
        private val with = GameState(turn = side)
    }
}
