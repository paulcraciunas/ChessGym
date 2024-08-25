package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.pawnStart
import com.paulcraciunas.chessgym.game.board.promotion

class PromotionPly(
    turn: Side,
    from: Locus,
    to: Locus,
    captured: Piece? = null,
    private var resultingPiece: Piece? = null,
) : StandardPly(turn = turn, piece = Piece.Pawn, from = from, to = to, captured = captured) {

    override fun accept(piece: Piece) {
        assert(piece != Piece.Pawn && piece != Piece.King)
        resultingPiece = piece
    }

    fun isAccepted(): Boolean = resultingPiece != null
    fun isPending(): Boolean = resultingPiece == null

    override fun exec(on: Board) {
        assert(resultingPiece != null)
        assert(from.rank == pawnStart(turn.other()))
        assert(to.rank == promotion(turn))

        on.remove(at = from)?.let { assert(it == Piece.Pawn) }
        captured()?.let {
            on.remove(at = to)?.let { assert(it == captured()) }
        }
        on.add(piece = resultingPiece!!, side = turn, at = to)
    }

    override fun undo(on: Board) {
        assert(resultingPiece != null)

        on.remove(at = to)?.let { assert(it == resultingPiece) }
        on.add(piece = Piece.Pawn, side = turn, at = from)
        captured()?.let {
            on.add(piece = it, side = turn.other(), at = to)
        }
    }

    override fun algebraic(): String = "${super.algebraic()}=${resultingPiece?.alg()}"
}
