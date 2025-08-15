package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

internal class MoveAdapter {
    fun from(move: String): Move {
        assert(move.length in 4..5) // We could have a promotion piece at the end, e.g. e7e8q

        val from = Locus.from(move.substring(0, 2))!!
        val to = Locus.from(move.substring(2, 4))!!
        val promotion = move.promotion()?.let { Piece.fromAlgebraic(it) }

        return Move(from, to, promotion)
    }

    data class Move(
        val from: Locus,
        val to: Locus,
        val promotion: Piece?
    )
}

private fun String.promotion() = if (length > 4) substring(4) else null
