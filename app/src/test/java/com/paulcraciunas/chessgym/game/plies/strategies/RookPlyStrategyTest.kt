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
            *neighbours,
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

        neighbours.map { Locus(it.first, it.second) }
            .forEach {
                assertTrue(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
            }
        allLocationsExcept(home, *neighbours).forEach {
            assertFalse(underTest.canAttack(from = home, to = it, on = on, turn = with.turn))
        }
    }

    companion object {
        val validLocations = listOf(
            // e file
            Locus(e, `1`),
            Locus(e, `2`),
            Locus(e, `3`),
            Locus(e, `5`),
            Locus(e, `6`),
            Locus(e, `7`),
            Locus(e, `8`),
            // 4-th rank
            Locus(a, `4`),
            Locus(b, `4`),
            Locus(c, `4`),
            Locus(d, `4`),
            Locus(f, `4`),
            Locus(g, `4`),
            Locus(h, `4`),
        )
        val neighbours = arrayOf(e to `3`, e to `5`, d to `4`, f to `4`)
    }
}
