package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.Side.BLACK
import com.paulcraciunas.chessgym.game.Side.WHITE
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

internal fun Pair<File, Rank>.loc() = Locus(first, second)

internal fun String.loc(): Locus = Locus.from(this)!!

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

internal inline fun <reified T : Ply> Collection<Ply>.assertMovesOf(
    turn: Side,
    piece: Piece,
    home: Locus,
    locations: List<Locus>,
) {
    assertEquals(locations.size, size)
    locations.forEach { dest ->
        assertNotNull(find { ply ->
            ply is T &&
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

internal inline fun <reified T : Ply> Collection<Ply>.assertHas(
    turn: Side,
    piece: Piece,
    home: Locus,
    location: Locus,
): Collection<Ply> = apply {
    assertNotNull(find { ply ->
        ply is T
        ply.turn == turn &&
        ply.piece == piece &&
        ply.from == home &&
        ply.to == location
    })
}

internal fun Collection<Ply>.assertNoMoves() {
    assertTrue(isEmpty())
}

internal inline fun <reified T : Ply> Collection<Ply>.assertNoMovesOf() {
    assertNull(find { it is T })
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

internal val E_4_NEIGHBOURS = listOf(
    Locus(e, `3`), Locus(e, `5`), Locus(d, `4`), Locus(f, `4`),
    Locus(d, `3`), Locus(d, `5`), Locus(f, `3`), Locus(f, `5`)
)

internal fun assertDefaultBoard(board:Board) {
    assertTrue(board.has(Piece.Rook, WHITE, "a1".loc()))
    assertTrue(board.has(Piece.Knight, WHITE, "b1".loc()))
    assertTrue(board.has(Piece.Bishop, WHITE, "c1".loc()))
    assertTrue(board.has(Piece.Queen, WHITE, "d1".loc()))
    assertTrue(board.has(Piece.King, WHITE, "e1".loc()))
    assertTrue(board.has(Piece.Bishop, WHITE, "f1".loc()))
    assertTrue(board.has(Piece.Knight, WHITE, "g1".loc()))
    assertTrue(board.has(Piece.Rook, WHITE, "h1".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "a2".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "b2".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "c2".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "d2".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "e2".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "f2".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "g2".loc()))
    assertTrue(board.has(Piece.Pawn, WHITE, "h2".loc()))
    assertTrue(board.has(Piece.Rook, BLACK, "a8".loc()))
    assertTrue(board.has(Piece.Knight, BLACK, "b8".loc()))
    assertTrue(board.has(Piece.Bishop, BLACK, "c8".loc()))
    assertTrue(board.has(Piece.Queen, BLACK, "d8".loc()))
    assertTrue(board.has(Piece.King, BLACK, "e8".loc()))
    assertTrue(board.has(Piece.Bishop, BLACK, "f8".loc()))
    assertTrue(board.has(Piece.Knight, BLACK, "g8".loc()))
    assertTrue(board.has(Piece.Rook, BLACK, "h8".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "a7".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "b7".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "c7".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "d7".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "e7".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "f7".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "g7".loc()))
    assertTrue(board.has(Piece.Pawn, BLACK, "h7".loc()))
}