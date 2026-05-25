package com.paulcraciunas.game.logic.plies.strategies

import com.paulcraciunas.game.logic.allLocationsExcept
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.assertMoves
import com.paulcraciunas.game.logic.assertNoMoves
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.strategies.KnightPlyStrategy
import com.paulcraciunas.game.logic.surroundQueen
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KnightPlyStrategyTest {
    private val home = Locus.e4
    private val on = Board().apply {
        add(piece = Piece.Knight, side = Side.BLACK, at = home)
    }
    private val with = MutableGameInfo(turn = Side.BLACK)

    private val underTest = KnightPlyStrategy()

    @Test
    fun `GIVEN non-knight WHEN getting plies THEN throw`() {
        on.remove(at = home)
        on.add(piece = Piece.King, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @Test
    fun `GIVEN knight on e4 WHEN getting plies THEN all 8 valid plies`() {
        underTest.plies(from = home, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = home,
                locations = validLocations
            )
    }

    @Test
    fun `GIVEN knight surrounded by allies WHEN getting plies THEN all 8 valid plies`() {
        on.surroundQueen(at = home, side = Side.BLACK)

        underTest.plies(from = home, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = home,
                locations = validLocations // Knights can jump!
            )
    }

    @Test
    fun `GIVEN knight plies occupied by allies WHEN getting plies THEN return nothing`() {
        validLocations.forEach {
            on.add(piece = Piece.Pawn, side = Side.BLACK, at = it)
        }

        underTest.plies(from = home, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN knight surrounded by enemies WHEN getting plies THEN all 8 valid plies`() {
        on.surroundQueen(at = home, side = Side.WHITE)

        underTest.plies(from = home, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = home,
                locations = validLocations // Knights can jump!
            )
    }

    @Test
    fun `GIVEN knight plies occupied by enemies WHEN getting plies THEN return all captures`() {
        validLocations.forEach {
            on.add(piece = Piece.Bishop, side = Side.WHITE, at = it)
        }

        underTest.plies(from = home, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = home,
                locations = validLocations // Knights can jump!
            )
    }

    @Test
    fun `GIVEN non-knight WHEN checking attacks THEN throw`() {
        on.remove(at = home)
        on.add(piece = Piece.King, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            Locus.all {
                underTest.canAttack(from = home, on = on, to = it, turn = with.turn)
            }
        }
    }

    @Test
    fun `WHEN knight is on e4 THEN it can attack all 8 jump squares`() {
        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN knight plies occupied by allies THEN it cannot attack`() {
        validLocations.forEach {
            on.add(piece = Piece.Bishop, side = Side.BLACK, at = it)
        }

        allLocationsExcept(home).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN knight plies occupied by enemies THEN it can attack them`() {
        validLocations.forEach {
            on.add(piece = Piece.Bishop, side = Side.WHITE, at = it)
        }

        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `GIVEN knight on a1 WHEN getting plies THEN both valid plies are returned`() {
        on.remove(at = home)
        val newHome = Locus.a1
        on.add(piece = Piece.Knight, side = Side.BLACK, at = newHome)

        underTest.plies(from = newHome, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = newHome,
                locations = listOf(Locus.b3, Locus.c2)
            )
    }

    @Test
    fun `GIVEN knight on h8 WHEN getting plies THEN both valid plies are returned`() {
        on.remove(at = home)
        val newHome = Locus.h8
        on.add(piece = Piece.Knight, side = Side.BLACK, at = newHome)

        underTest.plies(from = newHome, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = newHome,
                locations = listOf(Locus.g6, Locus.f7)
            )
    }

    private companion object {
        private val validLocations = listOf(
            Locus.f6,
            Locus.d6,
            Locus.c5,
            Locus.c3,
            Locus.d2,
            Locus.f2,
            Locus.g3,
            Locus.g5,
        )
    }
}
