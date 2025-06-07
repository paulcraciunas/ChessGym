package com.paulcraciunas.game.logic.api

import com.paulcraciunas.game.logic.Side
import com.paulcraciunas.game.logic.board.Locus
import com.paulcraciunas.game.logic.board.Piece

// TODO Paul: figure out if we need this "api" or indeed the whole "api" directory
// TODO Paul: have a think about the design of this gradle module. How should it be used to ensure OOP principles like encapsulation
interface IPly {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus
}
