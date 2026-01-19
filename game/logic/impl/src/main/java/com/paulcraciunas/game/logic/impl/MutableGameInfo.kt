package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.api.state.GameInfo
import com.paulcraciunas.game.logic.impl.plies.CastlePly
import com.paulcraciunas.game.logic.impl.plies.Playable
import com.paulcraciunas.game.logic.impl.plies.StandardPly

internal data class MutableGameInfo(
    override var turn: Side = Side.WHITE,
    override var lastPly: Ply? = null,
    override var inCheckCount: CheckCount = CheckCount.None,
    override var whiteCastling: Set<CastleType> = CastleType.entries.toSet(),
    override var blackCastling: Set<CastleType> = CastleType.entries.toSet(),
    override var plieClock: Int = 0,
    override var moveIndex: Int = 1,
) : GameInfo {

    override fun castling(turn: Side): Set<CastleType> =
        if (turn == Side.WHITE) whiteCastling else blackCastling

    fun update(ply: Playable, checkCount: CheckCount) {
        moveIndex += turn.moveIncrement() // do this before we swap the turn
        turn = turn.other()
        lastPly = ply
        inCheckCount = checkCount
        whiteCastling = updateCastling(Side.WHITE, ply)
        blackCastling = updateCastling(Side.BLACK, ply)
        plieClock = if (!ply.isPawnMoveOrCapture()) plieClock + 1 else 0
    }

    private fun updateCastling(side: Side, ply: Ply): Set<CastleType> =
        if (side == ply.turn) currentCastling(ply, castling(side))
        else otherCastling(ply, castling(side))

    private fun currentCastling(ply: Ply, castling: Set<CastleType>): Set<CastleType> {
        if (castling.isEmpty() || ply.piece == Piece.King) return emptySet()
        val result = mutableSetOf<CastleType>().apply { addAll(castling) }
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

    private fun otherCastling(ply: Ply, castling: Set<CastleType>): Set<CastleType> {
        val result = mutableSetOf<CastleType>().apply { addAll(castling) }
        if (ply is StandardPly && ply.captured() == Piece.Rook) {
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