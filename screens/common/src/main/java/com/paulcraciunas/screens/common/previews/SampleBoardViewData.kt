package com.paulcraciunas.screens.common.previews

import androidx.compose.runtime.Composable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.SquareViewData

object SampleBoardViewData {
    fun startingBoard(): BoardViewData {
        val squares: Array<Array<SquareViewData>> = Array(Rank.entries.size) {
            Array(File.entries.size) { SquareViewData(piece = null) }
        }
        squares.addWhitePieces()
        squares.addBlackPieces()
        return BoardViewData(squares)
    }

    @Composable
    fun startingBoardComposable(): BoardViewData = startingBoard()

    private fun Array<Array<SquareViewData>>.addWhitePieces() = apply {
        File.entries.forEach { file ->
            this[Rank.`2`.dec()][file.dec()] = SquareViewData.simple(piece = Piece.Pawn, side = Side.WHITE)
        }
        addStartingPieces(Side.WHITE, Rank.`1`)
    }

    private fun Array<Array<SquareViewData>>.addBlackPieces() = apply {
        File.entries.forEach { file ->
            this[Rank.`7`.dec()][file.dec()] = SquareViewData.simple(piece = Piece.Pawn, side = Side.BLACK)
        }
        addStartingPieces(Side.BLACK, Rank.`8`)
    }

    private fun Array<Array<SquareViewData>>.addStartingPieces(side: Side, rank: Rank) {
        this[rank.dec()][File.a.dec()] = SquareViewData.simple(piece = Piece.Rook, side = side)
        this[rank.dec()][File.b.dec()] = SquareViewData.simple(piece = Piece.Knight, side = side)
        this[rank.dec()][File.c.dec()] = SquareViewData.simple(piece = Piece.Bishop, side = side)
        this[rank.dec()][File.d.dec()] = SquareViewData.simple(piece = Piece.Queen, side = side)
        this[rank.dec()][File.e.dec()] = SquareViewData.simple(piece = Piece.King, side = side)
        this[rank.dec()][File.f.dec()] = SquareViewData.simple(piece = Piece.Bishop, side = side)
        this[rank.dec()][File.g.dec()] = SquareViewData.simple(piece = Piece.Knight, side = side)
        this[rank.dec()][File.h.dec()] = SquareViewData.simple(piece = Piece.Rook, side = side)
    }
}
