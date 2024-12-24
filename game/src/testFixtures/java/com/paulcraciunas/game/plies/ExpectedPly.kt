package com.paulcraciunas.game.plies

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece

/**
 * We use this to get around [com.paulcraciunas.game.plies.StandardPly] not having a deep equals implemented
 * That is due to it being an open class, which is inherited by
 * [com.paulcraciunas.game.plies.PromotionPly] and [com.paulcraciunas.game.plies.EnPassentPly]
 */
data class ExpectedPly(val side: Side, val piece: Piece, val from: Locus, val to: Locus) {
    constructor(ply: Ply) : this(ply.turn, ply.piece, ply.from, ply.to)
}
