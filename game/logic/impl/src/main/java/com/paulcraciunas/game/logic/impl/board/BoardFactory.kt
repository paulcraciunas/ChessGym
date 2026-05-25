package com.paulcraciunas.game.logic.impl.board

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.IBoardFactory
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank

object BoardFactory: IBoardFactory {
    override fun defaultBoard(): Board = Board().from(defaultBoard)

    private val defaultBoard: IBoard = Board()
        .addWhitePieces()
        .addBlackPieces()
}

private fun Board.addWhitePieces() = apply {
    File.entries.forEach { file ->
        add(Piece.Pawn, Side.WHITE, Locus.from(file, Rank.`2`))
    }
    addStartingPieces(Side.WHITE, Rank.`1`)
}

private fun Board.addBlackPieces() = apply {
    File.entries.forEach { file ->
        add(Piece.Pawn, Side.BLACK, Locus.from(file, Rank.`7`))
    }
    addStartingPieces(Side.BLACK, Rank.`8`)
}

private fun Board.addStartingPieces(side: Side, rank: Rank) {
    add(Piece.Rook, side, Locus.from(File.a, rank))
    add(Piece.Knight, side, Locus.from(File.b, rank))
    add(Piece.Bishop, side, Locus.from(File.c, rank))
    add(Piece.Queen, side, Locus.from(File.d, rank))
    add(Piece.King, side, Locus.from(File.e, rank))
    add(Piece.Bishop, side, Locus.from(File.f, rank))
    add(Piece.Knight, side, Locus.from(File.g, rank))
    add(Piece.Rook, side, Locus.from(File.h, rank))
}
