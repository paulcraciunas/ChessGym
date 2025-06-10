package com.paulcraciunas.chessgym.ui.model

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank

// TODO Paul: rethink this class when cleaning up the repo structure
class BoardViewDataBuilder {
    private val squares: Array<Array<SquareViewData>> = Array(Rank.entries.size) {
        Array(File.entries.size) { SquareViewData(piece = null) }
    }
    private var selection: Locus? = null
    private var lastMove: Pair<Locus, Locus>? = null
    private var moves: List<Locus> = emptyList()

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
        selection = from
        moves = availableMoves
    }

    fun withLastMove(from: Locus, to: Locus): BoardViewDataBuilder = apply {
        lastMove = Pair(from, to)
    }

    fun build(): BoardViewData {
        selection?.let { at ->
            // Mark the selected square piece as selected
            squares.update(at) { it.copy(piece = it.piece!!.copy(isSelected = true)) }
            setAvailableMoves()
        }
        lastMove?.let { pair ->
            squares.update(pair.first) { it.copy(lastMove = true) }
            squares.update(pair.second) { it.copy(lastMove = true) }
        }
        return BoardViewData(squares)
    }

    private fun setAvailableMoves() {
        moves.forEach { to ->
            squares.update(to) { square ->
                square.piece?.let { // if we have a piece, we can attack it; mark selected
                    square.copy(piece = square.piece.copy(isSelected = true))
                } ?: square.copy(canMoveTo = true) // otherwise mark that we can move there
            }
        }
    }
}

private fun Array<Array<SquareViewData>>.update(
    at: Locus,
    block: (SquareViewData) -> SquareViewData
) {
    val current = get(at.rank.dec())[at.file.dec()]
    get(at.rank.dec())[at.file.dec()] = block(current)
}
