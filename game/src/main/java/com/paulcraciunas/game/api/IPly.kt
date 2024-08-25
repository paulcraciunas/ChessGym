package com.paulcraciunas.game.api

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Piece

interface IPly {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus
}
