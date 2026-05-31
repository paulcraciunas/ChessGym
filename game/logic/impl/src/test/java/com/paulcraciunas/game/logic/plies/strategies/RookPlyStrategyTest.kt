package com.paulcraciunas.game.logic.plies.strategies

import com.paulcraciunas.game.logic.allLocationsExcept
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.assertMoves
import com.paulcraciunas.game.logic.assertNoMoves
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.strategies.RookPlyStrategy
import com.paulcraciunas.game.logic.surroundRook
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RookPlyStrategyTest {
    private val home = Locus.e4
    private val on = Board().apply {
        add(piece = Piece.Rook, side = Side.BLACK, at = home)
    }
    private val with = MutableGameInfo(turn = Side.BLACK)

    private val underTest = RookPlyStrategy()

    @Test
    fun `GIVEN non-rook WHEN getting plies THEN throw`() {
        on.remove(at = home)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @Test
    fun `GIVEN rook on e4 WHEN getting plies THEN return e file and fourth rank`() {
        underTest.plies(from = home, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Rook,
                home = home,
                locations = validLocations
            )
    }

    @Test
    fun `GIVEN rook surrounded by allies WHEN getting plies THEN return nothing`() {
        on.surroundRook(at = home, side = Side.BLACK)

        underTest.plies(from = home, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN rook surrounded by enemies WHEN getting plies THEN return captures`() {
        on.surroundRook(at = home, side = Side.WHITE)

        underTest.plies(from = home, on = on, with = with).assertMoves(
            turn = Side.BLACK,
            piece = Piece.Rook,
            home = home,
            neighbours,
        )
    }

    @Test
    fun `WHEN rook is on e4 THEN it can attack all e file and fourth rank`() {
        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN rook is surrounded by allies THEN it cannot attack`() {
        on.surroundRook(at = home, side = Side.BLACK)

        allLocationsExcept(home).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN rook is surrounded by enemies THEN it can attack them`() {
        on.surroundRook(at = home, side = Side.WHITE)

        neighbours
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, neighbours).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    companion object {
        val validLocations = listOf(
            // e file
            Locus.e1,
            Locus.e2,
            Locus.e3,
            Locus.e5,
            Locus.e6,
            Locus.e7,
            Locus.e8,
            // 4-th rank
            Locus.a4,
            Locus.b4,
            Locus.c4,
            Locus.d4,
            Locus.f4,
            Locus.g4,
            Locus.h4,
        )
        val neighbours = listOf(Locus.e3, Locus.e5, Locus.d4, Locus.f4)
    }
}
