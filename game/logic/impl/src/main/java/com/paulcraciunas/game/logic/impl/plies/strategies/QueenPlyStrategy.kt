package com.paulcraciunas.game.logic.impl.plies.strategies

import com.paulcraciunas.game.logic.api.board.Piece

internal class QueenPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Queen
    override fun directions(): Collection<Next> = directions

    companion object {
        val directions = RookPlyStrategy.directions + BishopPlyStrategy.directions
    }
}
