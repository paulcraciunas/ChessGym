package com.paulcraciunas.chessgym.game.api

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

interface IPly {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus
}
