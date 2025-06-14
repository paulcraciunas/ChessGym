package com.paulcraciunas.game.logic.plies.strategies

import com.paulcraciunas.game.logic.E_4_NEIGHBOURS
import com.paulcraciunas.game.logic.allLocationsExcept
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File.e
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank.`4`
import com.paulcraciunas.game.logic.assertMoves
import com.paulcraciunas.game.logic.assertNoMoves
import com.paulcraciunas.game.logic.impl.MutableGameInfo
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.strategies.QueenPlyStrategy
import com.paulcraciunas.game.logic.surroundQueen
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QueenPlyStrategyTest {
    private val home = Locus(e, `4`)
    private val on = Board().apply {
        add(piece = Piece.Queen, side = Side.BLACK, at = home)
    }
    private val with = MutableGameInfo(turn = Side.BLACK)

    private val underTest = QueenPlyStrategy()

    @Test
    fun `GIVEN non-queen WHEN getting plies THEN throw`() {
        on.remove(at = home)
        on.add(piece = Piece.King, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @Test
    fun `GIVEN queen on e4 WHEN getting plies THEN return e file, fourth rank and diagonals`() {
        underTest.plies(from = home, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Queen,
                home = home,
                locations = validLocations
            )
    }

    @Test
    fun `GIVEN queen surrounded by allies WHEN getting plies THEN return nothing`() {
        on.surroundQueen(at = home, side = Side.BLACK)

        underTest.plies(from = home, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN queen surrounded by enemies WHEN getting plies THEN return captures`() {
        on.surroundQueen(at = home, side = Side.WHITE)

        underTest.plies(from = home, on = on, with = with).assertMoves(
            turn = Side.BLACK,
            piece = Piece.Queen,
            home = home,
            E_4_NEIGHBOURS,
        )
    }

    @Test
    fun `WHEN queen is on e4 THEN it can attack all e file, fourth rank and diagonals`() {
        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN queen is surrounded by allies THEN it cannot attack`() {
        on.surroundQueen(at = home, side = Side.BLACK)

        allLocationsExcept(home).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN queen is surrounded by enemies THEN it can attack them`() {
        on.surroundQueen(at = home, side = Side.WHITE)

        E_4_NEIGHBOURS
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, E_4_NEIGHBOURS).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    private companion object {
        private val validLocations =
            RookPlyStrategyTest.validLocations + BishopPlyStrategyTest.validLocations
    }
}
