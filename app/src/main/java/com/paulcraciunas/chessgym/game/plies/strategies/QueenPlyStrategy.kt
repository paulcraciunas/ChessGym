package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.board.Piece

class QueenPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Queen
    override fun directions(): Collection<Next> = directions

    companion object {
        val directions = RookPlyStrategy.directions + BishopPlyStrategy.directions
    }
}
