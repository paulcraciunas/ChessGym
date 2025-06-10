package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

/**
 * In chess terminology, a ply is also known as a half-move.
 * This is because a "move" consists of a pair of half-moves: one for white and one for black.
 * That is useful to keep in mind when thinking about endgame conditions
 *
 * e.g. The 50 move rule requires there to be 50 moves (i.e. 100 plies)
 */
interface IPly {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus

    fun algebraic(): String
}
