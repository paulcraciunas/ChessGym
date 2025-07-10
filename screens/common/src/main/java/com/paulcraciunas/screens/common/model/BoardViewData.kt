package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank

data class BoardViewData(
    val squares: Array<Array<SquareViewData>>,
) {
    fun at(rank: Rank, file: File): SquareViewData = squares[rank.dec()][file.dec()]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardViewData

        return squares.contentDeepEquals(other.squares)
    }

    override fun hashCode(): Int = squares.contentDeepHashCode()
}

data class SquareViewData(
    val piece: PieceViewData?,
    val canMoveTo: Boolean = false,
    val lastMove: Boolean = false,
) {
    companion object {
        fun simple(piece: Piece, side: Side) = SquareViewData(piece = PieceViewData(piece = piece, side = side))
    }
}

data class PieceViewData(
    val piece: Piece,
    val side: Side,
    val isSelected: Boolean = false
)
