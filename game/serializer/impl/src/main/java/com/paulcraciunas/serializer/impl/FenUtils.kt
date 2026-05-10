package com.paulcraciunas.serializer.impl

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.Builder
import com.paulcraciunas.serializer.api.SerializeException

internal const val MISSING = "-"

internal fun Builder.withEnPassent(enPassent: String?): Builder = apply {
    if (enPassent == null || enPassent == MISSING) return this
    // This tells us the location a pawn moved OVER (e.g. e6)
    // To load the correct information as the "previous move", we have to add the from - to
    val loc = Locus.from(enPassent) ?: throw SerializeException("Invalid en-passent location: $this")
    return when (loc.rank) {
        Rank.`3` -> withLastPly(
            turn = Side.WHITE,
            piece = Piece.Pawn,
            from = Locus(loc.file, Rank.`2`),
            to = Locus(loc.file, Rank.`4`)
        )

        Rank.`6` -> withLastPly(
            turn = Side.BLACK,
            piece = Piece.Pawn,
            from = Locus(loc.file, Rank.`7`),
            to = Locus(loc.file, Rank.`5`)
        )

        else -> throw SerializeException("Invalid en-passent rank: ${loc.rank}")
    }
}
