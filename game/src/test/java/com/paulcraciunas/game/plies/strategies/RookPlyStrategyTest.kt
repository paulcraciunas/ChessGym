package com.paulcraciunas.game.plies.strategies

import com.paulcraciunas.game.GameState
import com.paulcraciunas.game.Side
import com.paulcraciunas.game.allLocationsExcept
import com.paulcraciunas.game.assertMoves
import com.paulcraciunas.game.assertNoMoves
import com.paulcraciunas.game.board.Board
import com.paulcraciunas.game.board.File.e
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.board.Rank.`4`
import com.paulcraciunas.game.loc
import com.paulcraciunas.game.surroundRook
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RookPlyStrategyTest {
    private val home = Locus(e, `4`)
    private val on = Board().apply {
        add(piece = Piece.Rook, side = Side.BLACK, at = home)
    }
    private val with = GameState(turn = Side.BLACK)

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
            neighbours.map { it.loc() },
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

        neighbours.map { it.loc() }
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, neighbours.map { it.loc() }).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    companion object {
        val validLocations = listOf(
            // e file
            "e1".loc(),
            "e2".loc(),
            "e3".loc(),
            "e5".loc(),
            "e6".loc(),
            "e7".loc(),
            "e8".loc(),
            // 4-th rank
            "a4".loc(),
            "b4".loc(),
            "c4".loc(),
            "d4".loc(),
            "f4".loc(),
            "g4".loc(),
            "h4".loc(),
        )
        val neighbours = listOf("e3", "e5", "d4", "f4")
    }
}
