package com.paulcraciunas.screens.common.model.v2

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.SidedPiece
import com.paulcraciunas.logic.builders.Builders

@Immutable
data class BoardViewData2(
    val squares: List<SquareViewData2> = emptySquares,
    val availableMoves: List<Locus> = emptyList(),
    val selection: Locus? = null,
    val animatingPiece: AnimatingPiece2? = null,
) {
    fun at(rank: Rank, file: File): SquareViewData2 = squares[indexFrom(rank, file)]
    fun at(loc: Locus): SquareViewData2 = squares[indexFrom(loc)]

    fun select(at: Locus, moves: List<Locus>): BoardViewData2 {
        val fromIdx = indexFrom(loc = at)
        if (squares[fromIdx].piece == null) return this

        val updatedSquares = squares.toMutableList()
        // 1. Set selected square
        updatedSquares[fromIdx] = squares[fromIdx].copy(
            piece = squares[fromIdx].piece!!.copy(isSelected = true)
        )
        // 2. Set legal move indicators
        moves.map { indexFrom(it) }.forEach {
            updatedSquares[it] = squares[it].copy(canMoveTo = true)
        }

        return copy(
            squares = updatedSquares,
            availableMoves = moves,
            selection = at,
        )
    }

    fun clearSelection(): BoardViewData2 {
        if (selection == null) return this

        val updatedSquares = squares.toMutableList()
        // 1. Clear selected square
        val index = indexFrom(selection)
        updatedSquares[index] = squares[index].copy(
            piece = squares[index].piece!!.copy(isSelected = false)
        )
        // 2. Clear legal move indicators
        availableMoves.map { indexFrom(it) }.forEach {
            updatedSquares[it] = squares[it].copy(canMoveTo = false)
        }

        return copy(
            squares = updatedSquares,
            availableMoves = emptyList(),
            selection = null,
        )
    }

    companion object {
        fun empty(): BoardViewData2 = BoardViewData2(squares = emptySquares)
        fun default(): BoardViewData2 = BoardViewData2(squares = defaultSquares)

        fun from(board: IBoard, lastMove: Pair<Locus, Locus>? = null, withAnimation: Boolean = false): BoardViewData2 {
            val updatedSquares = emptySquares.toMutableList().apply {
                load(board = board)
                lastMove?.let {
                    addLastMove(it.first, it.second)
                }
            }

            val animatingPiece = if (lastMove != null && withAnimation) {
                updatedSquares[indexFrom(loc = lastMove.second)].piece?.let {
                    AnimatingPiece2(piece = it.piece, from = lastMove.first, to = lastMove.second)
                }
            } else null

            return BoardViewData2(squares = updatedSquares, animatingPiece = animatingPiece)
        }
    }
}

@Immutable
data class AnimatingPiece2(
    val piece: SidedPiece,
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
        fun simple(piece: Piece, side: Side) = SquareViewData2(
            piece = PieceViewData2(piece = SidedPiece.of(side = side, piece = piece))
        )
    }
}

@Immutable
data class PieceViewData2(
    val piece: SidedPiece,
    val isSelected: Boolean = false,
)

private fun indexFrom(rank: Rank, file: File): Int = rank.dec() * 8 + file.dec()
private fun indexFrom(loc: Locus): Int = loc.rank.dec() * 8 + loc.file.dec()

// Created once as a fixed-size flat ArrayList
private val emptySquares: List<SquareViewData2> = List(64) { SquareViewData2(piece = null) }
private val defaultSquares: List<SquareViewData2> = emptySquares.toMutableList().apply {
    load(board = Builders.boardFactory().defaultBoard())
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
    val fromIdx = indexFrom(loc = from)
    this[fromIdx] = this[fromIdx].copy(lastMove = true)

    val toIdx = indexFrom(loc = to)
    this[toIdx] = this[toIdx].copy(lastMove = true)
}
