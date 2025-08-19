package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank

class BoardViewDataBuilder {
    private val squares: Array<Array<SquareViewData>> = Array(Rank.entries.size) {
        Array(File.entries.size) { SquareViewData(piece = null) }
    }

    fun load(puzzle: Puzzle) {
        loadBoard(puzzle.board)
        withLastMove(puzzle.info.lastPly!!.from, puzzle.info.lastPly!!.to)
    }

    fun loadBoard(board: IBoard) {
        // Clear the squares first
        Locus.all { loc ->
            squares[loc.rank.dec()][loc.file.dec()] = SquareViewData(piece = null)
        }
        // now loaded the board
        Locus.all { loc ->
            board.at(loc)?.let { piece ->
                val side = if (board.has(piece, Side.WHITE, loc)) Side.WHITE else Side.BLACK
                squares[loc.rank.dec()][loc.file.dec()] = SquareViewData(
                    piece = PieceViewData(piece = piece, side = side)
                )
            }
        }
    }

    fun withSelection(from: Locus, availableMoves: List<Locus>): BoardViewDataBuilder = apply {
        // Mark the selected square piece as selected
        squares.update(from) { it.copy(piece = it.piece!!.copy(isSelected = true)) }
        availableMoves.forEach { to ->
            squares.update(to) { square ->
                square.piece?.let { // if we have a piece, we can attack it; mark selected
                    square.copy(piece = square.piece.copy(isSelected = true))
                } ?: square.copy(canMoveTo = true) // otherwise mark that we can move there
            }
        }
    }

    fun clearSelection(from: Locus, availableMoves: List<Locus>) {
        squares.update(from) { it.copy(piece = it.piece!!.copy(isSelected = false)) }
        availableMoves.forEach { to ->
            squares.update(to) { square ->
                square.piece?.let { // if we have a piece, we can attack it; mark selected
                    square.copy(piece = square.piece.copy(isSelected = false))
                } ?: square.copy(canMoveTo = false) // otherwise mark that we can move there
            }
        }
    }

    fun withLastMove(from: Locus, to: Locus): BoardViewDataBuilder = apply {
        squares.update(from) { it.copy(lastMove = true) }
        squares.update(to) { it.copy(lastMove = true) }
    }

    fun build(): BoardViewData = BoardViewData(squares)
}

private fun Array<Array<SquareViewData>>.update(
    at: Locus,
    block: (SquareViewData) -> SquareViewData
) {
    val current = get(at.rank.dec())[at.file.dec()]
    get(at.rank.dec())[at.file.dec()] = block(current)
}
