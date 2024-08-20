package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File.d
import com.paulcraciunas.chessgym.game.board.File.e
import com.paulcraciunas.chessgym.game.board.File.f
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank.`3`
import com.paulcraciunas.chessgym.game.board.Rank.`4`
import com.paulcraciunas.chessgym.game.board.Rank.`5`
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QueenPlyStrategyTest {
    private val on = Board()
    private val with = GameState(turn = Side.BLACK)

    private val underTest = QueenPlyStrategy()

    @Test
    fun `GIVEN non-rook WHEN getting plies THEN throw`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.King, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @Test
    fun `GIVEN queen on e4 WHEN getting plies THEN return e file, fourth rank and diagonals`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)

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
        val home = Locus(e, `4`)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home.down()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.BLACK, at = home.right()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.topLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.topRight()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.downLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.downRight()!!)

        underTest.plies(from = home, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN queen surrounded by enemies WHEN getting plies THEN return captures`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = home.down()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.WHITE, at = home.right()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.topLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.topRight()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.downLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.downRight()!!)

        underTest.plies(from = home, on = on, with = with).assertMoves(
            turn = Side.BLACK,
            piece = Piece.Queen,
            home = home,
            listOf(
                Locus(e, `3`), Locus(e, `5`), Locus(d, `4`), Locus(f, `4`),
                Locus(d, `3`), Locus(d, `5`), Locus(f, `3`), Locus(f, `5`)
            ),
        )
    }

    @Test
    fun `WHEN queen is on e4 THEN it can attack all e file, fourth rank and diagonals`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)

        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        invalidLocations(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN queen is surrounded by allies THEN it cannot attack`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home.down()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.BLACK, at = home.right()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.topLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.topRight()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.downLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.BLACK, at = home.downRight()!!)

        invalidLocations(home).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN queen is surrounded by enemies THEN it can attack them`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = home.down()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.WHITE, at = home.right()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.topLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.topRight()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.downLeft()!!)
        on.add(piece = Piece.Pawn, side = Side.WHITE, at = home.downRight()!!)

        val validAttacks = listOf(
            Locus(e, `3`), Locus(e, `5`), Locus(d, `4`), Locus(f, `4`),
            Locus(d, `3`), Locus(d, `5`), Locus(f, `3`), Locus(f, `5`)
        )
        validAttacks
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        invalidLocations(home, validAttacks).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    private companion object {
        val validLocations =
            RookPlyStrategyTest.validLocations + BishopPlyStrategyTest.validLocations
    }
}