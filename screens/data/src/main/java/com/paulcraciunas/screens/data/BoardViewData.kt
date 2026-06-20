package com.paulcraciunas.screens.data

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.SidedPiece
import com.paulcraciunas.logic.builders.Builders

@Immutable
data class BoardViewData(
    val squares: List<SidedPiece?> = emptySquares,
    val availableMoves: List<Locus> = emptyList(),
    val selection: Locus? = null,
    val lastMove: Pair<Locus, Locus>? = null,
    val animatingPiece: SidedPiece? = null,
) {
    fun at(loc: Locus): SidedPiece? = squares[indexFrom(loc)]

    fun select(at: Locus, moves: List<Locus>): BoardViewData =
        if (squares[indexFrom(loc = at)] == null) return this
        else copy(
            availableMoves = moves,
            selection = at,
            animatingPiece = null,
        )

    fun clearSelection(): BoardViewData =
        if (selection == null) this
        else copy(
            availableMoves = emptyList(),
            selection = null,
            animatingPiece = null,
        )

    companion object {
        fun empty(): BoardViewData = BoardViewData(squares = emptySquares)
        fun default(): BoardViewData = BoardViewData(squares = defaultSquares)
        fun singleKnight(): BoardViewData = BoardViewData(squares = singleKnight)

        fun from(board: IBoard, lastMove: Pair<Locus, Locus>? = null, withAnimation: Boolean = false): BoardViewData {
            val updatedSquares = emptySquares.toMutableList().apply {
                load(board = board)
            }

            val animatingPiece = if (lastMove != null && withAnimation) {
                updatedSquares[indexFrom(loc = lastMove.second)]
            } else null

            return BoardViewData(
                squares = updatedSquares,
                lastMove = lastMove,
                animatingPiece = animatingPiece
            )
        }
    }
}

private fun indexFrom(loc: Locus): Int = loc.rank.dec() * 8 + loc.file.dec()

// Created once as a fixed-size flat ArrayList
private val emptySquares: List<SidedPiece?> = List(64) { null }
private val defaultSquares: List<SidedPiece?> = emptySquares.toMutableList().apply {
    load(board = Builders.boardFactory().defaultBoard())
}
private val singleKnight: List<SidedPiece?> = emptySquares.toMutableList().apply {
    this[indexFrom(Locus.e4)] = SidedPiece.WhiteKnight
}

private fun MutableList<SidedPiece?>.load(board: IBoard) {
    board.forEachPiece(turn = Side.BLACK) { piece, locus ->
        this[indexFrom(locus)] = SidedPiece.of(side = Side.BLACK, piece = piece)
    }
    board.forEachPiece(turn = Side.WHITE) { piece, locus ->
        this[indexFrom(locus)] = SidedPiece.of(side = Side.WHITE, piece = piece)
    }
}
