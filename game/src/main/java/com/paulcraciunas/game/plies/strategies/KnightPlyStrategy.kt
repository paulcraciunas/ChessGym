package com.paulcraciunas.game.plies.strategies

import com.paulcraciunas.game.board.Piece

class KnightPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Knight
    override fun simpleMoves(): Collection<Next> = standardMoves

    companion object {
        val standardMoves = listOf<Next>(
            { loc -> loc.top()?.topLeft() },
            { loc -> loc.top()?.topRight() },
            { loc -> loc.left()?.topLeft() },
            { loc -> loc.left()?.downLeft() },
            { loc -> loc.down()?.downLeft() },
            { loc -> loc.down()?.downRight() },
            { loc -> loc.right()?.topRight() },
            { loc -> loc.right()?.downRight() },
        )
    }
}
