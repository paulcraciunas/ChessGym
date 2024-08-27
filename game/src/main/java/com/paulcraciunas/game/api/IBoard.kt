package com.paulcraciunas.game.api

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece

interface IBoard {
    fun forEachPiece(turn: Side, action: (Piece, Locus) -> Unit)
}
