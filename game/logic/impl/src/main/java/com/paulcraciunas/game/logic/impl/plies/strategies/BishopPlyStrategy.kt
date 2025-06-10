package com.paulcraciunas.game.logic.impl.plies.strategies

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

internal class BishopPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Bishop
    override fun directions(): Collection<Next> = directions

    companion object {
        val directions = listOf(Locus::topLeft, Locus::topRight, Locus::downLeft, Locus::downRight)
    }
}
