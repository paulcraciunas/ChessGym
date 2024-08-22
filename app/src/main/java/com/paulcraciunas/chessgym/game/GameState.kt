package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.plies.CastlePly
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.StandardPly

/**
 * Non-computable information about the current state of the game.
 *
 * Most of the information from here is needed in various parts.
 * For example, there's no way to figure out if a player can castle just by looking at the board.
 * It's necessary to keep track of moves (e.g. king moves) which will affect future castling.
 * Pawn moves are also affected. The only way en-passent is possible is if the previous move was
 * a Pawn move 2 squares forward.
 *
 * Finally, when loading a game in-media res (e.g. from a FEN position), it's important we know
 * how many moves there have been (for computing if the game should end in a draw due to the
 * 50 move rule). In that case, since we don't have access to the move history, we need to know
 * the number of non-pawn and non-capture plies.
 */
data class GameState(
    val turn: Side = Side.WHITE,
    val lastPly: Ply? = null,
    val inCheckCount: CheckCount = CheckCount.None,
    val whiteCastling: Set<CastlePly.Type> = CastlePly.Type.entries.toSet(),
    val blackCastling: Set<CastlePly.Type> = CastlePly.Type.entries.toSet(),
    val plieClock: Int = 0, // Since last pawn move or capture
    val moveIndex: Int = 1,
) {
    fun castling(turn: Side): Set<CastlePly.Type> =
        if (turn == Side.WHITE) whiteCastling else blackCastling

    fun next(ply: Ply, checkCount: CheckCount): GameState = GameState(
        turn = turn.other(),
        lastPly = ply,
        inCheckCount = checkCount,
        whiteCastling = updateCastling(Side.WHITE, ply),
        blackCastling = updateCastling(Side.BLACK, ply),
        plieClock = if (!ply.isPawnMoveOrCapture()) plieClock + 1 else 0,
        moveIndex = moveIndex + turn.moveIncrement()
    )

    private fun updateCastling(side: Side, ply: Ply): Set<CastlePly.Type> =
        if (side == ply.turn) currentCastling(ply, castling(side))
        else otherCastling(ply, castling(side))

    private fun currentCastling(ply: Ply, castling: Set<CastlePly.Type>): Set<CastlePly.Type> {
        if (castling.isEmpty() || ply.piece == Piece.King) return emptySet()
        val result = mutableSetOf<CastlePly.Type>().apply { addAll(castling) }
        when (ply) {
            is CastlePly -> result.remove(ply.type)
            is StandardPly -> {
                castling.forEach {
                    // If the corresponding Rook was moved
                    if (ply.piece == Piece.Rook && ply.from == it.rook(ply.turn)) {
                        result.remove(it)
                    }
                }
            }
        }
        return result
    }

    private fun otherCastling(ply: Ply, castling: Set<CastlePly.Type>): Set<CastlePly.Type> {
        val result = mutableSetOf<CastlePly.Type>().apply { addAll(castling) }
        if (ply is StandardPly && ply.captured == Piece.Rook) {
            castling.forEach {
                // If the corresponding Rook was captured
                if (ply.to == it.rook(ply.turn.other())) {
                    result.remove(it)
                }
            }
        }
        return result
    }
}

private fun Side.moveIncrement(): Int = if (this == Side.BLACK) 1 else 0
