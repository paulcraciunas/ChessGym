package com.paulcraciunas.game.logic.impl.plies

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Piece

interface Playable: Ply {
    fun exec(on: IBoard)
    fun undo(on: IBoard)

    override fun isPromotion(): Boolean = false
    override fun promote(piece: Piece): Unit = throw AssertionError("By default, moves can't promote")
}
