package com.paulcraciunas.game.logic.plies.strategies

import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece

internal class RookPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Rook
    override fun directions(): Collection<Next> = directions

    companion object {
        val directions = listOf(Locus::left, Locus::right, Locus::top, Locus::down)
    }
}
