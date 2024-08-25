package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece

class EnPassentPly(
    turn: Side,
    from: Locus,
    to: Locus,
    private val passedLoc: Locus, // Needed as "to" location != "passed" location
) : StandardPly(turn = turn, piece = Piece.Pawn, from = from, to = to, captured = Piece.Pawn) {
    override fun exec(on: Board) {
        on.move(from = from, to = to, turn = turn).also { assert(it == null) }
        // Capture the pawn
        assert(on.has(piece = Piece.Pawn, side = turn.other(), at = passedLoc))
        on.remove(at = passedLoc).also { assert(it == Piece.Pawn) }
    }

    override fun undo(on: Board) {
        on.move(from = to, to = from, turn = turn).also { assert(it == null) }
        // Add back the captured pawn
        on.add(piece = Piece.Pawn, side = turn.other(), at = passedLoc)
    }

    override fun captured(): Piece = Piece.Pawn
}
