package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

/**
 * We use this to get around [StandardPly] not having a deep equals implemented
 * That is due to it being an open class, which is inherited by
 * [PromotionPly] and [EnPassentPly]
 */
internal data class ExpectedPly(val side: Side, val piece: Piece, val from: Locus, val to: Locus) {
    constructor(ply: Ply) : this(ply.turn, ply.piece, ply.from, ply.to)
}
