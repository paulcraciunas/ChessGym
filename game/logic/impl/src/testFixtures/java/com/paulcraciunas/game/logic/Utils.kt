package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.Side.BLACK
import com.paulcraciunas.game.logic.api.Side.WHITE
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

fun allLocationsExcept(home: Locus, except: List<Locus> = emptyList()) = Locus.entries.filter { home != it && !except.contains(it) }

inline fun <reified T : Playable> Collection<Playable>.assertMovesOf(
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

fun Collection<Playable>.assertMoves(
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

inline fun <reified T : Playable> Collection<Playable>.assertHas(
    turn: Side,
    piece: Piece,
    home: Locus,
    location: Locus,
): Collection<Playable> = apply {
    assertNotNull(find { ply ->
        ply is T
        ply.turn == turn &&
                ply.piece == piece &&
                ply.from == home &&
                ply.to == location
    })
}

fun Collection<Playable>.assertNoMoves() {
    assertTrue(isEmpty())
}

inline fun <reified T : Playable> Collection<Playable>.assertNoMovesOf() {
    assertNull(find { it is T })
}

fun Board.surroundRook(at: Locus, side: Side) {
    add(piece = Piece.Bishop, side = side, at = at.top()!!)
    add(piece = Piece.Knight, side = side, at = at.down()!!)
    add(piece = Piece.Queen, side = side, at = at.left()!!)
    add(piece = Piece.King, side = side, at = at.right()!!)
}

fun Board.surroundBishop(at: Locus, side: Side) {
    add(piece = Piece.Rook, side = side, at = at.topLeft()!!)
    add(piece = Piece.Knight, side = side, at = at.topRight()!!)
    add(piece = Piece.Queen, side = side, at = at.downLeft()!!)
    add(piece = Piece.King, side = side, at = at.downRight()!!)
}

fun Board.surroundQueen(at: Locus, side: Side) {
    surroundRook(at, side)
    surroundBishop(at, side)
}

val E_4_NEIGHBOURS = listOf(
    Locus.e3, Locus.e5, Locus.d4, Locus.f4,
    Locus.d3, Locus.d5, Locus.f3, Locus.f5
)

fun assertDefaultBoard(board: IBoard) {
    assertTrue(board.has(Piece.Rook, WHITE, Locus.a1))
    assertTrue(board.has(Piece.Knight, WHITE, Locus.b1))
    assertTrue(board.has(Piece.Bishop, WHITE, Locus.c1))
    assertTrue(board.has(Piece.Queen, WHITE, Locus.d1))
    assertTrue(board.has(Piece.King, WHITE, Locus.e1))
    assertTrue(board.has(Piece.Bishop, WHITE, Locus.f1))
    assertTrue(board.has(Piece.Knight, WHITE, Locus.g1))
    assertTrue(board.has(Piece.Rook, WHITE, Locus.h1))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.a2))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.b2))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.c2))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.d2))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.e2))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.f2))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.g2))
    assertTrue(board.has(Piece.Pawn, WHITE, Locus.h2))
    assertTrue(board.has(Piece.Rook, BLACK, Locus.a8))
    assertTrue(board.has(Piece.Knight, BLACK, Locus.b8))
    assertTrue(board.has(Piece.Bishop, BLACK, Locus.c8))
    assertTrue(board.has(Piece.Queen, BLACK, Locus.d8))
    assertTrue(board.has(Piece.King, BLACK, Locus.e8))
    assertTrue(board.has(Piece.Bishop, BLACK, Locus.f8))
    assertTrue(board.has(Piece.Knight, BLACK, Locus.g8))
    assertTrue(board.has(Piece.Rook, BLACK, Locus.h8))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.a7))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.b7))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.c7))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.d7))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.e7))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.f7))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.g7))
    assertTrue(board.has(Piece.Pawn, BLACK, Locus.h7))
}