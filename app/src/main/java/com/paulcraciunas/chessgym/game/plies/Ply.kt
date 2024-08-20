package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

interface Ply {
    val turn: Side
    val piece: Piece
    val from: Locus
    val to: Locus

    fun exec(on: Board)
    fun undo(on: Board)
}
