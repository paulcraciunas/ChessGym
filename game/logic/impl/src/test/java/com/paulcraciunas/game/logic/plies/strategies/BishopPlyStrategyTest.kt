package com.paulcraciunas.game.logic.plies.strategies

import com.paulcraciunas.game.logic.allLocationsExcept
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.assertMoves
import com.paulcraciunas.game.logic.assertNoMoves
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.strategies.BishopPlyStrategy
import com.paulcraciunas.game.logic.surroundBishop
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class BishopPlyStrategyTest {
    private val home = Locus.e4
    private val on = Board().apply {
        add(piece = Piece.Bishop, side = Side.BLACK, at = home)
    }
    private val with = MutableGameInfo(turn = Side.BLACK)

    private val underTest = BishopPlyStrategy()

    @Test
    fun `GIVEN non-bishop WHEN getting plies THEN throw`() {
        on.remove(at = home)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @Test
    fun `GIVEN bishop on e4 WHEN getting plies THEN return diagonals`() {
        underTest.plies(from = home, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Bishop,
                home = home,
                locations = validLocations
            )
    }

    @Test
    fun `GIVEN bishop surrounded by allies WHEN getting plies THEN return nothing`() {
        on.surroundBishop(at = home, side = Side.BLACK)

        underTest.plies(from = home, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN bishop surrounded by enemies WHEN getting plies THEN return captures`() {
        on.surroundBishop(at = home, side = Side.WHITE)

        underTest.plies(from = home, on = on, with = with).assertMoves(
            turn = Side.BLACK,
            piece = Piece.Bishop,
            home = home,
            listOf(Locus.d5, Locus.d3, Locus.f5, Locus.f3),
        )
    }

    @Test
    fun `WHEN bishop is on e4 THEN it can attack both diagonals`() {
        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN bishop is surrounded by allies THEN it cannot attack`() {
        on.surroundBishop(at = home, side = Side.BLACK)

        allLocationsExcept(home).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN bishop is surrounded by enemies THEN it can attack them`() {
        on.surroundBishop(at = home, side = Side.WHITE)

        validAttacks
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, validAttacks).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    companion object {
        val validLocations = listOf(
            // First diagonal
            Locus.a8,
            Locus.b7,
            Locus.c6,
            Locus.d5,
            Locus.f3,
            Locus.g2,
            Locus.h1,
            // Second diagonal
            Locus.b1,
            Locus.c2,
            Locus.d3,
            Locus.f5,
            Locus.g6,
            Locus.h7,
        )
        val validAttacks = listOf(Locus.d5, Locus.d3, Locus.f5, Locus.f3)
    }
}
