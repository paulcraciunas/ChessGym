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
interface Ply {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus

    fun algebraic(): String

    fun captured(): Piece? = null
    fun isPawnMoveOrCapture(): Boolean

    fun isPromotion(): Boolean
    fun promote(piece: Piece)

    fun resolve(disambiguate: Disambiguate)

    enum class Disambiguate {
        File,
        Rank,
        Both,
        None
    }
}

fun List<Ply>.algebraic(): String {
    val result = StringBuilder()
    forEachIndexed { index, ply ->
        if (index % 2 == 0) {
            if (index > 0) result.append(" ")
            result.append("${index / 2 + 1}.")
        }
        result.append(" ${ply.algebraic()}")
    }
    return result.toString()
}
