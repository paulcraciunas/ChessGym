package com.paulcraciunas.game.logic.impl.plies

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

open class StandardPly(
    override val turn: Side,
    override val piece: Piece,
    override val from: Locus,
    override val to: Locus,
    private val captured: Piece? = null,
    private var disambiguate: Ply.Disambiguate = Ply.Disambiguate.None,
) : Playable {

    override fun resolve(disambiguate: Ply.Disambiguate) {
        this.disambiguate = disambiguate
    }

    override fun exec(on: IBoard) {
        on.move(from = from, to = to, turn = turn)?.let { assert(it == captured) }
    }

    override fun undo(on: IBoard) {
        on.move(from = to, to = from, turn = turn)
        // If we captured something, add it back to the other set (i.e. we capture "other" pieces)
        captured?.let { on.add(piece = it, side = turn.other(), at = to) }
    }

    override fun captured(): Piece? = captured
    override fun algebraic(): String {
        val captured = if (captured != null) "x" else ""
        val amb = if (piece == Piece.Pawn && captured.isNotBlank()) Ply.Disambiguate.File
        else disambiguate
        val from = when (amb) {
            Ply.Disambiguate.File -> "${from.file}"
            Ply.Disambiguate.Rank -> "${from.rank}"
            Ply.Disambiguate.Both -> "$from"
            Ply.Disambiguate.None -> ""
        }
        return "${piece.alg()}$from$captured$to"
    }
}