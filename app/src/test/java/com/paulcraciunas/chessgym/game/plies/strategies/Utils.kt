package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.File
import com.paulcraciunas.chessgym.game.board.File.d
import com.paulcraciunas.chessgym.game.board.File.e
import com.paulcraciunas.chessgym.game.board.File.f
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import com.paulcraciunas.chessgym.game.board.Rank.`3`
import com.paulcraciunas.chessgym.game.board.Rank.`4`
import com.paulcraciunas.chessgym.game.board.Rank.`5`
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

internal fun allLocationsExcept(home: Locus, except: List<Locus> = emptyList()) =
    mutableListOf<Locus>().apply {
        File.entries.forEach { file ->
            Rank.entries.forEach { rank ->
                val it = Locus(file, rank)
                if (home != it && !except.contains(it)) {
                    add(it)
                }
            }
        }
    }

internal fun allLocationsExcept(home: Locus, vararg except: Pair<File, Rank>) =
    mutableListOf<Locus>().apply {
        File.entries.forEach { file ->
            Rank.entries.forEach { rank ->
                val it = Locus(file, rank)
                if (home != it && !except.contains(file to rank)) {
                    add(it)
                }
            }
        }
    }

internal fun Collection<Ply>.assertMoves(
    turn: Side,
    piece: Piece,
    home: Locus,
    locations: List<Locus>,
) {
    assertEquals(locations.size, size)
    locations.forEach { dest ->
        assertNotNull(find { ply ->
            ply is StandardPly &&
            ply.turn == turn &&
            ply.piece == piece &&
            ply.from == home &&
            ply.to == dest
        })
    }
}

internal fun Collection<Ply>.assertMoves(
    turn: Side,
    piece: Piece,
    home: Locus,
    vararg locations: Pair<File, Rank>,
) {
    assertEquals(locations.size, size)
    locations.forEach { dest ->
        assertNotNull(find { ply ->
            ply is StandardPly &&
            ply.turn == turn &&
            ply.piece == piece &&
            ply.from == home &&
            ply.to == Locus(dest.first, dest.second)
        })
    }
}

internal fun Collection<Ply>.assertNoMoves() {
    assertTrue(isEmpty())
}

internal fun Board.surroundRook(at: Locus, side: Side) {
    add(piece = Piece.Bishop, side = side, at = at.top()!!)
    add(piece = Piece.Knight, side = side, at = at.down()!!)
    add(piece = Piece.Queen, side = side, at = at.left()!!)
    add(piece = Piece.King, side = side, at = at.right()!!)
}

internal fun Board.surroundBishop(at: Locus, side: Side) {
    add(piece = Piece.Rook, side = side, at = at.topLeft()!!)
    add(piece = Piece.Knight, side = side, at = at.topRight()!!)
    add(piece = Piece.Queen, side = side, at = at.downLeft()!!)
    add(piece = Piece.King, side = side, at = at.downRight()!!)
}

internal fun Board.surroundQueen(at: Locus, side: Side) {
    surroundRook(at, side)
    surroundBishop(at, side)
}

val E_4_NEIGHBOURS = listOf(
    Locus(e, `3`), Locus(e, `5`), Locus(d, `4`), Locus(f, `4`),
    Locus(d, `3`), Locus(d, `5`), Locus(f, `3`), Locus(f, `5`)
)
