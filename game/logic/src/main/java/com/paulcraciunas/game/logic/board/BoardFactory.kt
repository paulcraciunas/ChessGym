package com.paulcraciunas.game.logic.board

import com.paulcraciunas.game.logic.Side
import com.paulcraciunas.game.logic.board.File.a
import com.paulcraciunas.game.logic.board.File.b
import com.paulcraciunas.game.logic.board.File.c
import com.paulcraciunas.game.logic.board.File.d
import com.paulcraciunas.game.logic.board.File.e
import com.paulcraciunas.game.logic.board.File.f
import com.paulcraciunas.game.logic.board.File.g
import com.paulcraciunas.game.logic.board.File.h
import com.paulcraciunas.game.logic.board.Rank.`1`
import com.paulcraciunas.game.logic.board.Rank.`2`
import com.paulcraciunas.game.logic.board.Rank.`7`
import com.paulcraciunas.game.logic.board.Rank.`8`

object BoardFactory {
    fun defaultBoard(): Board = Board().from(defaultBoard)

    private val defaultBoard: Board = Board()
        .addWhitePieces()
        .addBlackPieces()
}

private fun Board.addWhitePieces() = apply {
    File.entries.forEach { file ->
        add(Piece.Pawn, Side.WHITE, Locus(file, `2`))
    }
    addStartingPieces(Side.WHITE, `1`)
}

private fun Board.addBlackPieces() = apply {
    File.entries.forEach { file ->
        add(Piece.Pawn, Side.BLACK, Locus(file, `7`))
    }
    addStartingPieces(Side.BLACK, `8`)
}

private fun Board.addStartingPieces(side: Side, rank: Rank) {
    add(Piece.Rook, side, Locus(a, rank))
    add(Piece.Knight, side, Locus(b, rank))
    add(Piece.Bishop, side, Locus(c, rank))
    add(Piece.Queen, side, Locus(d, rank))
    add(Piece.King, side, Locus(e, rank))
    add(Piece.Bishop, side, Locus(f, rank))
    add(Piece.Knight, side, Locus(g, rank))
    add(Piece.Rook, side, Locus(h, rank))
}
