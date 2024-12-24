package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.Side
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece

interface IBoard {
    fun forEachPiece(turn: Side, action: (Piece, Locus) -> Unit)
}
