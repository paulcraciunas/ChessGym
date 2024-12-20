package com.paulcraciunas.game.io

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece
import com.paulcraciunas.game.board.Rank
import com.paulcraciunas.game.plies.Ply
import com.paulcraciunas.game.plies.StandardPly

internal const val MISSING = "-"

internal fun String.loadEnPassent(): Ply? {
    if (this == MISSING) return null
    // This tells us the location a pawn moved OVER (e.g. e6)
    // To load the correct information as the "previous move", we have to add the from - to
    val loc = Locus.from(this) ?: throw SerializeException("Invalid en-passent location: $this")
    return when (loc.rank) {
        Rank.`3` -> StandardPly(
            turn = Side.WHITE,
            piece = Piece.Pawn,
            from = Locus(loc.file, Rank.`2`),
            to = Locus(loc.file, Rank.`4`)
        )

        Rank.`6` -> StandardPly(
            turn = Side.BLACK,
            piece = Piece.Pawn,
            from = Locus(loc.file, Rank.`7`),
            to = Locus(loc.file, Rank.`5`)
        )

        else -> throw SerializeException("Invalid en-passent rank: ${loc.rank}")
    }
}
