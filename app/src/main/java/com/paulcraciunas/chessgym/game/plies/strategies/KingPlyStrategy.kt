package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.CheckCount
import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.plies.CastlePly
import com.paulcraciunas.chessgym.game.plies.Ply

class KingPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.King
    override fun canMoveInCheck(count: CheckCount): Boolean = true // King can always try and move
    override fun simpleMoves(): Collection<Next> = QueenPlyStrategy.directions

    override fun canAttack(from: Locus, to: Locus, on: Board, turn: Side): Boolean {
        assert(on.has(Piece.King, turn, from))

        return mutableListOf<Ply>().apply {
            addSimplePlies(turn, from, on, simpleMoves()) { it == to }
        }.any { it.to == to }
    }

    override fun MutableList<Ply>.addComplexPlies(
        from: Locus,
        on: Board,
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
