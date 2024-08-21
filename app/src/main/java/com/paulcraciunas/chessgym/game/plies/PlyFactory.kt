package com.paulcraciunas.chessgym.game.plies

import com.paulcraciunas.chessgym.game.CheckCount
import com.paulcraciunas.chessgym.game.GameState
import com.paulcraciunas.chessgym.game.Side
import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.plies.strategies.BishopPlyStrategy
import com.paulcraciunas.chessgym.game.plies.strategies.KingPlyStrategy
import com.paulcraciunas.chessgym.game.plies.strategies.KnightPlyStrategy
import com.paulcraciunas.chessgym.game.plies.strategies.PawnPlyStrategy
import com.paulcraciunas.chessgym.game.plies.strategies.PlyStrategy
import com.paulcraciunas.chessgym.game.plies.strategies.QueenPlyStrategy
import com.paulcraciunas.chessgym.game.plies.strategies.RookPlyStrategy
import java.util.EnumMap

/**
 * This class is responsible for generating all legal moves available.
 *
 * While each [PlyStrategy] makes its own validations,
 * certain validations cannot be performed while generating the moves.
 * This is because attempting to do so will lead to an infinite loop.
 *
 * E.g.
 * Q: Can I move this bishop? A: Only if I don't end up in check.
 * Q: How do I know that? A: try to move it, and compute all the checks from the other side.
 * Q: How do I compute all the checks? A: Look at all available moves from the other side.
 * Q: OK, can I move this Rook? A: Only if I don't end up in check.
 * - repeat ad infinitum
 */
class PlyFactory {
    private val strategies = EnumMap<Piece, PlyStrategy>(Piece::class.java).apply {
        put(Piece.Pawn, PawnPlyStrategy())
        put(Piece.Bishop, BishopPlyStrategy())
        put(Piece.Knight, KnightPlyStrategy())
        put(Piece.Rook, RookPlyStrategy())
        put(Piece.Queen, QueenPlyStrategy())
        put(Piece.King, KingPlyStrategy())
    }

    fun allLegalPlies(on: Board, with: GameState): Collection<Ply> =
        allPlies(on, with).filter { it.isValid(on) }

    fun canCheck(at: Locus, on: Board, turn: Side): CheckCount {
        var checkCount = CheckCount.None
        on.forEachPiece(turn) { piece, pieceLoc ->
            if (strategies[piece]!!.canAttack(from = pieceLoc, to = at, on = on, turn = turn)) {
                checkCount++
            }
        }
        return checkCount
    }

    private fun Ply.isValid(on: Board): Boolean {
        exec(on) // try the move
        // verify for checks
        val inCheck = on.king(turn)?.let { kingLoc ->
            if (this is CastlePly) {
                canAttack(type.pass(turn), on, turn) ||
                canAttack(type.extraPass(turn), on, turn) ||
                canAttack(type.end(turn), on, turn)
            } else {
                canAttack(kingLoc, on, turn)
            }
        } ?: false
        undo(on) // revert move
        return !inCheck
    }

    private fun allPlies(on: Board, with: GameState): MutableList<Ply> {
        val allMoves = mutableListOf<Ply>()
        on.forEachPiece(with.turn) { piece, loc ->
            allMoves.addAll(strategies[piece]!!.plies(from = loc, on = on, with = with))
        }

        return allMoves
    }

    private fun canAttack(destination: Locus?, on: Board, turn: Side): Boolean =
        destination?.let { to ->
            on.has(side = turn.other()) { piece, at ->
                strategies[piece]!!.canAttack(from = at, to = to, on = on, turn = turn.other())
            }
        } ?: false
}
