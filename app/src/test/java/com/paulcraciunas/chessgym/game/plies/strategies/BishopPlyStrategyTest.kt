package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.allLocationsExcept
import com.paulcraciunas.chessgym.game.assertMoves
import com.paulcraciunas.chessgym.game.assertNoMoves
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File.d
import com.paulcraciunas.chessgym.game.board.File.e
import com.paulcraciunas.chessgym.game.board.File.f
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank.`3`
import com.paulcraciunas.chessgym.game.board.Rank.`4`
import com.paulcraciunas.chessgym.game.board.Rank.`5`
import com.paulcraciunas.chessgym.game.loc
import com.paulcraciunas.chessgym.game.surroundBishop
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class BishopPlyStrategyTest {
    private val home = Locus(e, `4`)
    private val on = Board().apply {
        add(piece = Piece.Bishop, side = Side.BLACK, at = home)
    }
    private val with = GameState(turn = Side.BLACK)

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
            listOf(Locus(d, `5`), Locus(d, `3`), Locus(f, `5`), Locus(f, `3`)),
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

        validAttacks.map { it.loc() }
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, validAttacks.map { it.loc() }).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    companion object {
        val validLocations = listOf(
            // First diagonal
            "a8".loc(),
            "b7".loc(),
            "c6".loc(),
            "d5".loc(),
            "f3".loc(),
            "g2".loc(),
            "h1".loc(),
            // Second diagonal
            "b1".loc(),
            "c2".loc(),
            "d3".loc(),
            "f5".loc(),
            "g6".loc(),
            "h7".loc(),
        )
        val validAttacks = listOf("d5", "d3", "f5", "f3")
    }
}
