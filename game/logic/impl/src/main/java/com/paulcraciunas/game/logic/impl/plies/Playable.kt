package com.paulcraciunas.game.logic.impl.plies

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Piece

// TODO Paul: merge this with parent interface. No point in having 2
interface Playable: Ply {
    fun resolve(disambiguate: Disambiguate) {}
    fun exec(on: IBoard)
    fun undo(on: IBoard)

    fun captured(): Piece? = null
    fun isPawnMoveOrCapture(): Boolean
    fun accept(piece: Piece): Unit = throw AssertionError("Default moves can't promote")

    enum class Disambiguate {
        File,
        Rank,
        Both,
        None
    }
}