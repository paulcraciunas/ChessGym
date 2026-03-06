package com.paulcraciunas.screens.common.model

import androidx.compose.runtime.Stable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank

@Stable
data class BoardViewData(
    val squares: Array<Array<SquareViewData>>,
    val animatingPiece: AnimatingPiece? = null,
) {
    fun at(rank: Rank, file: File): SquareViewData = squares[rank.dec()][file.dec()]
    fun at(loc: Locus): SquareViewData = at(loc.rank, loc.file)

    fun withMoveIndicators(
        selectedSquare: Locus?,
        legalMoves: List<Locus>,
    ): BoardViewData {
        if (selectedSquare == null && legalMoves.isEmpty()) return this

        val newSquares = squares.map { row -> row.copyOf() }.toTypedArray()
        selectedSquare?.let { loc ->
            newSquares[loc.rank.dec()][loc.file.dec()] =
                newSquares[loc.rank.dec()][loc.file.dec()].copy(lastMove = true)
        }
        legalMoves.forEach { loc ->
            newSquares[loc.rank.dec()][loc.file.dec()] =
                newSquares[loc.rank.dec()][loc.file.dec()].copy(canMoveTo = true)
        }
        return BoardViewData(newSquares)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardViewData

        if (!squares.contentDeepEquals(other.squares)) return false
        if (animatingPiece != other.animatingPiece) return false

        return true
    }

    override fun hashCode(): Int {
        var result = squares.contentDeepHashCode()
        result = 31 * result + (animatingPiece?.hashCode() ?: 0)
        return result
    }
}

/**
 * Represents a piece that should be animated from one square to another.
 * The piece at [to] should not be rendered in the static board during animation.
 */
@Stable
data class AnimatingPiece(
    val piece: Piece,
    val side: Side,
    val from: Locus,
    val to: Locus,
)

@Stable
data class SquareViewData(
    val piece: PieceViewData?,
    val canMoveTo: Boolean = false,
    val lastMove: Boolean = false,
) {
    companion object {
        fun simple(piece: Piece, side: Side) = SquareViewData(piece = PieceViewData(piece = piece, side = side))
    }
}

@Stable
data class PieceViewData(
    val piece: Piece,
    val side: Side,
    val isSelected: Boolean = false
)
