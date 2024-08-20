package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

class BishopPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Bishop
    override fun directions(): Collection<Next> = directions

    companion object {
        val directions = listOf(Locus::topLeft, Locus::topRight, Locus::downLeft, Locus::downRight)
    }
}
