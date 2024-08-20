package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File.a
import com.paulcraciunas.chessgym.game.board.File.b
import com.paulcraciunas.chessgym.game.board.File.c
import com.paulcraciunas.chessgym.game.board.File.d
import com.paulcraciunas.chessgym.game.board.File.e
import com.paulcraciunas.chessgym.game.board.File.f
import com.paulcraciunas.chessgym.game.board.File.g
import com.paulcraciunas.chessgym.game.board.File.h
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank.`1`
import com.paulcraciunas.chessgym.game.board.Rank.`2`
import com.paulcraciunas.chessgym.game.board.Rank.`3`
import com.paulcraciunas.chessgym.game.board.Rank.`4`
import com.paulcraciunas.chessgym.game.board.Rank.`5`
import com.paulcraciunas.chessgym.game.board.Rank.`6`
import com.paulcraciunas.chessgym.game.board.Rank.`7`
import com.paulcraciunas.chessgym.game.board.Rank.`8`
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RookPlyStrategyTest {
    private val on = Board()
    private val with = GameState(turn = Side.BLACK)

    private val underTest = RookPlyStrategy()

    @Test
    fun `GIVEN non-rook WHEN getting plies THEN throw`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @Test
    fun `GIVEN rook on e4 WHEN getting plies THEN return e file and fourth rank`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home)

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
        val home = Locus(e, `4`)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home.down()!!)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.BLACK, at = home.right()!!)

        underTest.plies(from = home, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN rook surrounded by enemies WHEN getting plies THEN return captures`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = home.down()!!)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.WHITE, at = home.right()!!)

        underTest.plies(from = home, on = on, with = with).assertMoves(
            turn = Side.BLACK,
            piece = Piece.Rook,
            home = home,
            listOf(Locus(e, `3`), Locus(e, `5`), Locus(d, `4`), Locus(f, `4`)),
        )
    }

    @Test
    fun `WHEN rook is on e4 THEN it can attack all e file and fourth rank`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home)

        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        invalidLocations(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN rook is surrounded by allies THEN it cannot attack`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home.down()!!)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.BLACK, at = home.right()!!)

        invalidLocations(home).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN rook is surrounded by enemies THEN it can attack them`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home)
        on.add(piece = Piece.Bishop, side = Side.WHITE, at = home.top()!!)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = home.down()!!)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = home.left()!!)
        on.add(piece = Piece.King, side = Side.WHITE, at = home.right()!!)

        val validAttacks = listOf(Locus(e, `3`), Locus(e, `5`), Locus(d, `4`), Locus(f, `4`))
        validAttacks
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        invalidLocations(home, validAttacks).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    private companion object {
        val validLocations = listOf(
            Locus(e, `1`),
            Locus(e, `2`),
            Locus(e, `3`),
            Locus(e, `5`),
            Locus(e, `6`),
            Locus(e, `7`),
            Locus(e, `8`),
            Locus(a, `4`),
            Locus(b, `4`),
            Locus(c, `4`),
            Locus(d, `4`),
            Locus(f, `4`),
            Locus(g, `4`),
            Locus(h, `4`),
        )
    }
}
