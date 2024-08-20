package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

class RookPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Rook
    override fun directions(): Collection<Next> = directions

    companion object {
        val directions = listOf(Locus::left, Locus::right, Locus::top, Locus::down)
    }
}
