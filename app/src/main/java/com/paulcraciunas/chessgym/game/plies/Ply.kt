package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

/**
 * In chess terminology, a ply is also known as a half-move.
 * This is because a "move" consists of a par of half-moves: one for white and one for black.
 * That is useful to keep in mind when thinking about endgame conditions
 *
 * e.g. The 50 move rule requires there to be 50 moves (i.e. 100 plies)
 */
interface Ply {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus
    // TODO Paul: add is check field, to allow things like sounds effect on check

    fun resolve(disambiguate: Disambiguate) {}
    fun exec(on: Board)
    fun undo(on: Board)

    fun captured(): Piece? = null
    fun isPawnMoveOrCapture(): Boolean
    fun accept(piece: Piece): Unit = throw AssertionError("Default moves can't promote")
    // TODO Paul: need to add if it's a check or not for serialization purposes
    fun algebraic(): String

    enum class Disambiguate {
        File,
        Rank,
        Both,
        None
    }
}
