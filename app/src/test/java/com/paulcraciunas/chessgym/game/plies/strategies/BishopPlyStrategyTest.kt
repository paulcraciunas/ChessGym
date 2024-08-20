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

internal class BishopPlyStrategyTest {
    private val on = Board()
    private val with = GameState(turn = Side.BLACK)

    private val underTest = BishopPlyStrategy()

    @Test
    fun `GIVEN non-bishop WHEN getting plies THEN throw`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home)

        assertThrows<AssertionError> {
            underTest.plies(from = home, on = on, with = with)
        }
    }

    @Test
    fun `GIVEN bishop on e4 WHEN getting plies THEN return diagonals`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)

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
        val home = Locus(e, `4`)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home.topLeft()!!)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home.topRight()!!)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home.downLeft()!!)
        on.add(piece = Piece.King, side = Side.BLACK, at = home.downRight()!!)

        underTest.plies(from = home, on = on, with = with).assertNoMoves()
    }

    @Test
    fun `GIVEN bishop surrounded by enemies WHEN getting plies THEN return captures`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = home.topLeft()!!)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = home.topRight()!!)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = home.downLeft()!!)
        on.add(piece = Piece.King, side = Side.WHITE, at = home.downRight()!!)

        underTest.plies(from = home, on = on, with = with).assertMoves(
            turn = Side.BLACK,
            piece = Piece.Bishop,
            home = home,
            listOf(Locus(d, `5`), Locus(d, `3`), Locus(f, `5`), Locus(f, `3`)),
        )
    }

    @Test
    fun `WHEN bishop is on e4 THEN it can attack both diagonals`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)

        validLocations
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        invalidLocations(home, validLocations).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN bishop is surrounded by allies THEN it cannot attack`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)
        on.add(piece = Piece.Rook, side = Side.BLACK, at = home.topLeft()!!)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = home.topRight()!!)
        on.add(piece = Piece.Queen, side = Side.BLACK, at = home.downLeft()!!)
        on.add(piece = Piece.King, side = Side.BLACK, at = home.downRight()!!)

        invalidLocations(home).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    @Test
    fun `WHEN bishop is surrounded by enemies THEN it can attack them`() {
        val home = Locus(e, `4`)
        on.add(piece = Piece.Bishop, side = Side.BLACK, at = home)
        on.add(piece = Piece.Rook, side = Side.WHITE, at = home.topLeft()!!)
        on.add(piece = Piece.Knight, side = Side.WHITE, at = home.topRight()!!)
        on.add(piece = Piece.Queen, side = Side.WHITE, at = home.downLeft()!!)
        on.add(piece = Piece.King, side = Side.WHITE, at = home.downRight()!!)

        val validAttacks = listOf(Locus(d, `5`), Locus(d, `3`), Locus(f, `5`), Locus(f, `3`))
        validAttacks
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        invalidLocations(home, validAttacks).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    companion object {
        val validLocations = listOf(
            // First diagonal
            Locus(a, `8`),
            Locus(b, `7`),
            Locus(c, `6`),
            Locus(d, `5`),
            Locus(f, `3`),
            Locus(g, `2`),
            Locus(h, `1`),
            // Second diagonal
            Locus(b, `1`),
            Locus(c, `2`),
            Locus(d, `3`),
            Locus(f, `5`),
            Locus(g, `6`),
            Locus(h, `7`),
        )
    }
}
