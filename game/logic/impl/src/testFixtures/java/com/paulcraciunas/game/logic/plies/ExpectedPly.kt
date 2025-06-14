package com.paulcraciunas.game.logic.plies

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

/**
 * We use this to get around [com.paulcraciunas.game.logic.impl.plies.StandardPly] not having a deep equals implemented
 * That is due to it being an open class, which is inherited by
 * [com.paulcraciunas.game.logic.impl.plies.PromotionPly] and [com.paulcraciunas.game.logic.impl.plies.EnPassentPly]
 */
data class ExpectedPly(val side: Side, val piece: Piece, val from: Locus, val to: Locus) {
    constructor(playable: Ply) : this(playable.turn, playable.piece, playable.from, playable.to)
}
