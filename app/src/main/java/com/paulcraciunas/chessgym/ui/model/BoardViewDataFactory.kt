package com.paulcraciunas.chessgym.ui.model

import com.paulcraciunas.game.Side
import com.paulcraciunas.game.board.BoardFactory
import com.paulcraciunas.game.board.File
import com.paulcraciunas.game.board.Locus
import com.paulcraciunas.game.board.Rank

class BoardViewDataFactory {
    private val pieces: Array<Array<SquareViewData>> = Array(Rank.entries.size) {
        Array(File.entries.size) { SquareViewData(piece = null) }
    }

    fun create(): BoardViewData = BoardViewData(pieces)

    companion object {
        fun default(): BoardViewDataFactory = BoardViewDataFactory().apply {
            val board = BoardFactory.defaultBoard()
            Locus.all { loc ->
                board.at(loc)?.let { piece ->
                    val side = if (board.has(piece, Side.WHITE, loc)) Side.WHITE else Side.BLACK
                    pieces[loc.rank.dec()][loc.file.dec()] = SquareViewData(
                        piece = PieceViewData(piece = piece, side = side)
                    )
                }
            }
        }
    }
}
