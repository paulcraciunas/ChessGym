package com.paulcraciunas.game.logic.gameover

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.MutableGame
import com.paulcraciunas.game.logic.impl.board.Board

internal class GameTestHelper {
    fun createGameWithKingsOnly(
        whiteKingAt: Locus = Locus.e1,
        blackKingAt: Locus = Locus.e8,
        turn: Side = Side.WHITE
    ): MutableGame {
        val board = Board()
        board.add(Piece.King, Side.WHITE, whiteKingAt)
        board.add(Piece.King, Side.BLACK, blackKingAt)
        return MutableGame(board = board, turn = turn)
    }

    fun createGameWithPieces(
        pieces: List<Triple<Piece, Side, Locus>>,
        turn: Side = Side.WHITE
    ): MutableGame {
        val board = Board()
        pieces.forEach { (piece, side, locus) ->
            board.add(piece, side, locus)
        }
        return MutableGame(board = board, turn = turn)
    }
}
