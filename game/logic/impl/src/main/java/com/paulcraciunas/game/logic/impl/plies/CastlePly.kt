package com.paulcraciunas.game.logic.impl.plies

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Piece

class CastlePly(override val turn: Side, val type: CastleType) : Playable {
    override val piece = Piece.King
    override val from = type.from(turn)
    override val to = type.end(turn)

    override fun exec(on: IBoard) {
        on.move(from = from, to = to, turn = turn)
        on.move(from = type.rook(turn), to = type.pass(turn), turn = turn)
    }

    override fun undo(on: IBoard) {
        on.move(from = to, to = from, turn = turn)
        on.move(from = type.pass(turn), to = type.rook(turn), turn = turn)
    }

    override fun captured(): Piece? = null
    override fun isPawnMoveOrCapture(): Boolean = false
    override fun algebraic(): String = if (type == CastleType.KingSide) "O-O" else "O-O-O"
    override fun resolve(disambiguate: Ply.Disambiguate) {}
}