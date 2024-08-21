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

internal class KnightPlyStrategyTest {
    private val home = Locus(e, `4`)
    private val on = Board().apply {
        add(piece = Piece.Knight, side = Side.BLACK, at = home)
    }
    private val with = GameState(turn = Side.BLACK)

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
        val newHome = Locus(a, `1`)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = newHome)

        underTest.plies(from = newHome, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = newHome,
                locations = arrayOf(b to `3`, c to `2`)
            )
    }

    @Test
    fun `GIVEN knight on h8 WHEN getting plies THEN both valid plies are returned`() {
        on.remove(at = home)
        val newHome = Locus(h, `8`)
        on.add(piece = Piece.Knight, side = Side.BLACK, at = newHome)

        underTest.plies(from = newHome, on = on, with = with)
            .assertMoves(
                turn = Side.BLACK,
                piece = Piece.Knight,
                home = newHome,
                locations = arrayOf(g to `6`, f to `7`)
            )
    }

    private companion object {
        private val validLocations = listOf(
            Locus(f, `6`),
            Locus(d, `6`),
            Locus(c, `5`),
            Locus(c, `3`),
            Locus(d, `2`),
            Locus(f, `2`),
            Locus(g, `3`),
            Locus(g, `5`)
        )
    }
}
