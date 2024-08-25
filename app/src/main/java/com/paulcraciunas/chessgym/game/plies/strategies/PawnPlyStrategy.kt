package com.paulcraciunas.chessgym.game.plies.strategies

import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.board.Rank
import com.paulcraciunas.chessgym.game.board.pawnStart
import com.paulcraciunas.chessgym.game.board.promotion
import com.paulcraciunas.chessgym.game.plies.EnPassentPly
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.PromotionPly
import com.paulcraciunas.chessgym.game.plies.StandardPly

class PawnPlyStrategy : PlyStrategy() {
    override val piece: Piece = Piece.Pawn

    /**
     * All pawn movements are complicated
     * First issue, is that white pawns and black pawns move in opposite directions.
     * Next issue, is pawns can "jump" a square, but only if it's their first move
     * Pawns capture in a different direction to their normal movement.
     * Finally, there's en-passent capturing, which has it's own extra conditions.
     */
    override fun MutableList<Ply>.addComplexPlies(from: Locus, on: Board, with: GameState) {
        assert(from.rank != Rank.`1` && from.rank != Rank.`8`)

        addSimplePlies(from, on, with)
        addCaptures(from, on, with)
        addEnPassent(from, on, with)
    }

    override fun canAttack(from: Locus, to: Locus, on: Board, turn: Side): Boolean {
        assert(on.has(Piece.Pawn, turn, from))

        return turn.captureLeft(from) == to || turn.captureRight(from) == to
    }

    private fun MutableList<Ply>.addSimplePlies(from: Locus, on: Board, with: GameState) {
        var loc = with.turn.next(from)
        if (loc != null && on.isEmpty(loc)) {
            if (loc.rank == promotion(with.turn)) {
                add(PromotionPly(turn = with.turn, from = from, to = loc))
            } else {
                add(StandardPly(turn = with.turn, piece = piece, from = from, to = loc))
                loc = with.turn.next(loc)
                if (loc != null && from.rank == pawnStart(with.turn) && on.isEmpty(loc)) {
                    add(StandardPly(turn = with.turn, piece = piece, from = from, to = loc))
                }
            }
        }
    }

    private fun MutableList<Ply>.addCaptures(from: Locus, on: Board, with: GameState) {
        with.turn.captureLeft(from)?.let { addCapture(it, on, promotion(with.turn), with, from) }
        with.turn.captureRight(from)?.let { addCapture(it, on, promotion(with.turn), with, from) }
    }

    private fun MutableList<Ply>.addCapture(
        loc: Locus, on: Board, promotion: Rank, with: GameState, from: Locus,
    ) {
        if (on.has(with.turn.other(), loc)) {
            if (loc.rank == promotion) {
                add(PromotionPly(with.turn, from = from, to = loc, captured = on.at(loc)))
            } else {
                add(StandardPly(with.turn, piece, from = from, to = loc, captured = on.at(loc)))
            }
        }
    }

    private fun MutableList<Ply>.addEnPassent(from: Locus, on: Board, with: GameState) {
        if (from.rank == with.turn.enPassent() &&
            with.lastPly?.piece == Piece.Pawn &&
            with.lastPly.to.rank == with.turn.enPassent() &&
            with.lastPly.from.rank == pawnStart(with.turn.other())
        ) {
            var loc: Locus?
            if (with.lastPly.to.file == from.left()?.file) {
                loc = with.turn.captureLeft(from)
                if (loc != null && on.isEmpty(loc)) {
                    add(EnPassentPly(with.turn, from = from, to = loc, passedLoc = with.lastPly.to))
                }
            }
            if (with.lastPly.to.file == from.right()?.file) {
                loc = with.turn.captureRight(from)
                if (loc != null && on.isEmpty(loc)) {
                    add(EnPassentPly(with.turn, from = from, to = loc, passedLoc = with.lastPly.to))
                }
            }
        }
    }
}

private fun Side.enPassent(): Rank = if (this == Side.WHITE) Rank.`5` else Rank.`4`
private fun Side.next(at: Locus): Locus? = if (this == Side.WHITE) at.top() else at.down()
private fun Side.captureLeft(at: Locus): Locus? = next(at)?.left()
private fun Side.captureRight(at: Locus): Locus? = next(at)?.right()
