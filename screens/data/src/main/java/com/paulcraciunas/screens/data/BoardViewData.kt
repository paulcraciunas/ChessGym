package com.paulcraciunas.screens.data

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.SidedPiece
import com.paulcraciunas.logic.builders.Builders

@Immutable
data class BoardViewData(
    val squares: List<SquareViewData> = emptySquares,
    val availableMoves: List<Locus> = emptyList(),
    val selection: Locus? = null,
    val animatingPiece: AnimatingPiece? = null,
) {
    fun at(loc: Locus): SquareViewData = squares[indexFrom(loc)]

    fun select(at: Locus, moves: List<Locus>): BoardViewData {
        val fromIdx = indexFrom(loc = at)
        val targetPiece = squares[fromIdx].piece ?: return this

        val updatedSquares = squares.toMutableList()
        // 1. Set selected square
        updatedSquares[fromIdx] = updatedSquares[fromIdx].copy(
            piece = targetPiece.copy(isSelected = true)
        )
        // 2. Set legal move indicators
        for (i in moves.indices) {
            val idx = indexFrom(moves[i])
            updatedSquares[idx] = updatedSquares[idx].copy(canMoveTo = true)
        }

        return copy(
            squares = updatedSquares,
            availableMoves = moves,
            selection = at,
        )
    }

    fun clearSelection(): BoardViewData {
        if (selection == null) return this

        val updatedSquares = squares.toMutableList()
        // 1. Clear selected square
        val index = indexFrom(selection)
        updatedSquares[index] = updatedSquares[index].copy(
            piece = updatedSquares[index].piece!!.copy(isSelected = false)
        )
        // 2. Clear legal move indicators
        for (i in availableMoves.indices) {
            val idx = indexFrom(availableMoves[i])
            updatedSquares[idx] = updatedSquares[idx].copy(canMoveTo = false)
        }

        return copy(
            squares = updatedSquares,
            availableMoves = emptyList(),
            selection = null,
        )
    }

    companion object {
        fun empty(): BoardViewData = BoardViewData(squares = emptySquares)
        fun default(): BoardViewData = BoardViewData(squares = defaultSquares)
        fun singleKnight(): BoardViewData = BoardViewData(squares = singleKnight)

        fun from(board: IBoard, lastMove: Pair<Locus, Locus>? = null, withAnimation: Boolean = false): BoardViewData {
            val updatedSquares = emptySquares.toMutableList().apply {
                load(board = board)
                lastMove?.let {
                    addLastMove(it.first, it.second)
                }
            }

            val animatingPiece = if (lastMove != null && withAnimation) {
                updatedSquares[indexFrom(loc = lastMove.second)].piece?.let {
                    AnimatingPiece(piece = it.piece, from = lastMove.first, to = lastMove.second)
                }
            } else null

            return BoardViewData(squares = updatedSquares, animatingPiece = animatingPiece)
        }
    }
}

@Immutable
data class AnimatingPiece(
    val piece: SidedPiece,
    val from: Locus,
    val to: Locus,
)

@Immutable
data class SquareViewData(
    val piece: PieceViewData?,
    val canMoveTo: Boolean = false,
    val lastMove: Boolean = false,
) {
    val highlightable: Boolean = (piece?.piece != null) && (piece.isSelected || canMoveTo)

    companion object {
        fun simple(piece: Piece, side: Side) = SquareViewData(
            piece = PieceViewData(piece = SidedPiece.of(side = side, piece = piece))
        )
    }
}

@Immutable
data class PieceViewData(
    val piece: SidedPiece,
    val isSelected: Boolean = false,
)

private fun indexFrom(loc: Locus): Int = loc.rank.dec() * 8 + loc.file.dec()

// Created once as a fixed-size flat ArrayList
private val emptySquares: List<SquareViewData> = List(64) { SquareViewData(piece = null) }
private val defaultSquares: List<SquareViewData> = emptySquares.toMutableList().apply {
    load(board = Builders.boardFactory().defaultBoard())
}
private val singleKnight: List<SquareViewData> = emptySquares.toMutableList().apply {
    this[indexFrom(Locus.e4)] = SquareViewData.simple(piece = Piece.Knight, side = Side.WHITE)
}

private fun MutableList<SquareViewData>.load(board: IBoard) {
    board.forEachPiece(turn = Side.BLACK) { piece, locus ->
        this[indexFrom(locus)] = SquareViewData.simple(piece = piece, side = Side.BLACK)
    }
    board.forEachPiece(turn = Side.WHITE) { piece, locus ->
        this[indexFrom(locus)] = SquareViewData.simple(piece = piece, side = Side.WHITE)
    }
}

private fun MutableList<SquareViewData>.addLastMove(from: Locus, to: Locus) {
    val fromIdx = indexFrom(loc = from)
    this[fromIdx] = this[fromIdx].copy(lastMove = true)

    val toIdx = indexFrom(loc = to)
    this[toIdx] = this[toIdx].copy(lastMove = true)
}
