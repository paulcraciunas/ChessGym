package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

open class StandardPly(
    override val turn: Side,
    override val piece: Piece,
    override val from: Locus,
    override val to: Locus,
    val captured: Piece? = null,
) : Ply {

    override fun exec(on: Board) {
        on.move(from = from, to = to, turn = turn)?.let { assert(it == captured) }
    }

    override fun undo(on: Board) {
        on.move(from = to, to = from, turn = turn)
        // If we captured something, add it back to the other set (i.e. we capture "other" pieces)
        captured?.let { on.add(piece = it, side = turn.other(), at = to) }
    }
}
