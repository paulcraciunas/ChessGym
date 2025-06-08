package com.paulcraciunas.game.logic.impl.plies.strategies

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.impl.GameState
import com.paulcraciunas.game.logic.impl.plies.CastlePly
import com.paulcraciunas.game.logic.impl.plies.Ply

internal class KingPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.King
    override fun canMoveInCheck(count: CheckCount): Boolean = true // King can always try and move
    override fun simpleMoves(): Collection<Next> = QueenPlyStrategy.directions

    override fun canAttack(from: Locus, to: Locus, on: IBoard, turn: Side): Boolean {
        assert(on.has(Piece.King, turn, from))

        return mutableListOf<Ply>().apply {
            addSimplePlies(turn, from, on, simpleMoves()) { it == to }
        }.any { it.to == to }
    }

    override fun MutableList<Ply>.addComplexPlies(
        from: Locus,
        on: IBoard,
        with: GameState,
    ) {
        if (with.inCheckCount == CheckCount.None) {
            with.castling(with.turn).forEach { castle ->
                // Validating if these squares are not in check is done later. It can't be done now
                if (on.has(piece, with.turn, castle.from(with.turn)) &&
                    on.has(Piece.Rook, with.turn, castle.rook(with.turn)) &&
                    on.isEmpty(castle.pass(with.turn)) &&
                    on.isEmpty(castle.end(with.turn)) &&
                    castle.extraPass(with.turn)?.let { on.isEmpty(it) } != false
                ) {
                    add(CastlePly(turn = with.turn, castle))
                }
            }
        }
    }
}
