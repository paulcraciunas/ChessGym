package com.paulcraciunas.game.logic.impl.plies.strategies

import com.paulcraciunas.game.logic.api.board.Piece

internal class KnightPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Knight
    override fun simpleMoves(): Collection<Next> = standardMoves

    companion object {
        val standardMoves = knightMoves
    }
}

// Exposing this as we are reusing it in KnightsPath game
val knightMoves = listOf<Next>(
    { loc -> loc.top()?.topLeft() },
    { loc -> loc.top()?.topRight() },
    { loc -> loc.left()?.topLeft() },
    { loc -> loc.left()?.downLeft() },
    { loc -> loc.down()?.downLeft() },
    { loc -> loc.down()?.downRight() },
    { loc -> loc.right()?.topRight() },
    { loc -> loc.right()?.downRight() },
)