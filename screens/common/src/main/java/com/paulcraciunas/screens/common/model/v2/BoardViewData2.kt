package com.paulcraciunas.screens.common.model.v2

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.logic.builders.Builders
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Immutable
data class BoardViewData2(
    val squares: PersistentList<SquareViewData2> = emptySquares,
    val availableMoves: PersistentList<Locus> = persistentListOf(),
    val selection: Locus? = null,
    val animatingPiece: AnimatingPiece2? = null,
) {
    fun at(rank: Rank, file: File): SquareViewData2 = squares[indexFrom(rank, file)]
    fun at(loc: Locus): SquareViewData2 = squares[indexFrom(loc)]

    fun select(at: Locus, moves: List<Locus>): BoardViewData2 {
        val fromIdx = indexFrom(loc = at)
        if (squares[fromIdx].piece == null) return this

        val updatedSquares = squares.mutate { updatedSquares ->
            // 1. Set selected square
            updatedSquares[fromIdx] = squares[fromIdx].copy(
                piece = squares[fromIdx].piece!!.copy(isSelected = true)
            )
            // 2. Set legal move indicators
            moves.map { loc -> indexFrom(loc) }.forEach {
                updatedSquares[it] = squares[it].copy(
                    canMoveTo = true
                )
            }
        }
        return copy(
            squares = updatedSquares,
            availableMoves = moves.toPersistentList(),
            selection = at,
        )
    }

    fun clearSelection(): BoardViewData2 {
        if (selection == null) return this

        val updatedSquares = squares.mutate { updatedSquares ->
            // 1. Set selected square
            val index = indexFrom(selection)
            updatedSquares[index] = squares[index].copy(
                piece = squares[index].piece!!.copy(isSelected = false)
            )
            // 2. Set legal move indicators
            availableMoves.map { loc -> indexFrom(loc) }.forEach {
                updatedSquares[it] = squares[it].copy(
                    canMoveTo = false
                )
            }
        }
        return copy(
            squares = updatedSquares,
            availableMoves = availableMoves.clear(),
            selection = null,
        )
    }

    companion object {
        fun empty(): BoardViewData2 = BoardViewData2(squares = emptySquares)
        fun default(): BoardViewData2 = BoardViewData2(squares = defaultSquares)
        fun from(board: IBoard, lastMove: Pair<Locus, Locus>? = null, withAnimation: Boolean = false): BoardViewData2 {
            val squares = emptySquares.mutate { newSquares ->
                newSquares.load(board = board)
                lastMove?.let {
                    newSquares.addLastMove(it.first, it.second)
                }
            }
            val animatingPiece = if (lastMove != null && withAnimation) {
                squares[indexFrom(loc = lastMove.second)].piece?.let {
                    AnimatingPiece2(piece = it.piece, side = it.side, from = lastMove.first, to = lastMove.second)
                }
            } else null
            return BoardViewData2(squares = squares, animatingPiece = animatingPiece)
        }
    }
}

/**
 * Represents a piece that should be animated from one square to another.
 * The piece at [to] should not be rendered in the static board during animation.
 */
@Immutable
data class AnimatingPiece2(
    val piece: Piece,
    val side: Side,
    val from: Locus,
    val to: Locus,
)

@Immutable
data class SquareViewData2(
    val piece: PieceViewData2?,
    val canMoveTo: Boolean = false,
    val lastMove: Boolean = false,
) {
    companion object {
        fun simple(piece: Piece, side: Side) = SquareViewData2(piece = PieceViewData2(piece = piece, side = side))
    }
}

@Immutable
data class PieceViewData2(
    val piece: Piece,
    val side: Side,
    val isSelected: Boolean = false,
)

private fun indexFrom(rank: Rank, file: File): Int = rank.dec() * 8 + file.dec()
private fun indexFrom(loc: Locus): Int = loc.rank.dec() * 8 + loc.file.dec()

private val emptySquares: PersistentList<SquareViewData2> = persistentListOf<SquareViewData2>()
    .builder().apply { repeat(64) { add(SquareViewData2(piece = null)) } }.build()
private val defaultSquares = emptySquares.mutate { updatedSquares ->
    updatedSquares.load(board = Builders.boardFactory().defaultBoard())
}

private fun MutableList<SquareViewData2>.load(board: IBoard) {
    board.forEachPiece(turn = Side.BLACK) { piece, locus ->
        this[indexFrom(locus)] = SquareViewData2.simple(piece = piece, side = Side.BLACK)
    }
    board.forEachPiece(turn = Side.WHITE) { piece, locus ->
        this[indexFrom(locus)] = SquareViewData2.simple(piece = piece, side = Side.WHITE)
    }
}

private fun MutableList<SquareViewData2>.addLastMove(from: Locus, to: Locus) {
    var index = indexFrom(loc = from)
    this[index] = this[index].copy(
        lastMove = true
    )
    index = indexFrom(loc = to)
    this[index] = this[index].copy(
        lastMove = true
    )
}
