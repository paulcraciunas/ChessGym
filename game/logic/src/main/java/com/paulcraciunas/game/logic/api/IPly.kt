package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.Side
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece

interface IPly {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus
}
