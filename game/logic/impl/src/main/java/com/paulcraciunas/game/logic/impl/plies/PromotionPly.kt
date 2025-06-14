package com.paulcraciunas.game.logic.impl.plies

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.pawnStart
import com.paulcraciunas.game.logic.api.board.promotion

internal class PromotionPly(
    turn: Side,
    from: Locus,
    to: Locus,
    captured: Piece? = null,
    private var resultingPiece: Piece? = Piece.Queen,
) : StandardPly(turn = turn, piece = Piece.Pawn, from = from, to = to, captured = captured) {

    override fun isPromotion(): Boolean = true
    override fun promote(piece: Piece) {
        assert(piece != Piece.Pawn && piece != Piece.King)
        resultingPiece = piece
    }

    override fun exec(on: IBoard) {
        assert(resultingPiece != null)
        assert(from.rank == pawnStart(turn.other()))
        assert(to.rank == promotion(turn))

        on.remove(at = from)?.let { assert(it == Piece.Pawn) }
        captured()?.let {
            on.remove(at = to)?.let { assert(it == captured()) }
        }
        on.add(piece = resultingPiece!!, side = turn, at = to)
    }

    override fun undo(on: IBoard) {
        assert(resultingPiece != null)

        on.remove(at = to)?.let { assert(it == resultingPiece) }
        on.add(piece = Piece.Pawn, side = turn, at = from)
        captured()?.let {
            on.add(piece = it, side = turn.other(), at = to)
        }
    }

    override fun algebraic(): String = "${super.algebraic()}=${resultingPiece?.alg()}"
}