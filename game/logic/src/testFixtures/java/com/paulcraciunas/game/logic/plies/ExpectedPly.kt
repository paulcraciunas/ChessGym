package com.paulcraciunas.game.logic.plies

import com.paulcraciunas.game.logic.Side
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece

/**
 * We use this to get around [com.paulcraciunas.game.logic.plies.StandardPly] not having a deep equals implemented
 * That is due to it being an open class, which is inherited by
 * [com.paulcraciunas.game.logic.plies.PromotionPly] and [com.paulcraciunas.game.logic.plies.EnPassentPly]
 */
data class ExpectedPly(val side: Side, val piece: Piece, val from: Locus, val to: Locus) {
    constructor(ply: Ply) : this(ply.turn, ply.piece, ply.from, ply.to)
}
