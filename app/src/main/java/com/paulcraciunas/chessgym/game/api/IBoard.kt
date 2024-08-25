package com.paulcraciunas.chessgym.game.api

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

interface IBoard {
    fun forEachPiece(turn: Side, action: (Piece, Locus) -> Unit)
}
