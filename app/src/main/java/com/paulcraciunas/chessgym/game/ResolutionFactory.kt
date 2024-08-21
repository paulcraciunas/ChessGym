package com.paulcraciunas.chessgym.game

import com.paulcraciunas.chessgym.game.board.Board
import com.paulcraciunas.chessgym.game.board.Locus
import com.paulcraciunas.chessgym.game.board.Piece
import com.paulcraciunas.chessgym.game.plies.Ply
import com.paulcraciunas.chessgym.game.plies.StandardPly
import java.util.EnumMap

class ResolutionFactory(
    private val availablePlies: Collection<Ply>,
    private val board: Board,
    private val plies: List<PlyWithState>,
    private val settings: Settings,
) {
    private val tempBoard = Board()

    fun of(current: GameState): Resolution? = when {
        isCheckMate(current) -> Resolution.CheckMate
        isStaleMate(current) -> Resolution.StaleMate
        isDrawByMoveRule() -> Resolution.DrawByMoveRule
        isDrawByInsufficientMaterial() -> Resolution.DrawByInsufficientMaterial
        isDrawByRepetition(current) -> Resolution.DrawByRepetition
        else -> null
    }

    private fun isCheckMate(with: GameState): Boolean =
        with.inCheckCount != CheckCount.None && availablePlies.isEmpty()

    private fun isStaleMate(with: GameState): Boolean =
        with.inCheckCount == CheckCount.None && availablePlies.isEmpty()

    private fun isDrawByInsufficientMaterial(): Boolean {
        // If we don't have 2 kings (e.g. some custom puzzle) then we can't have a normal draw
        if (board.king(Side.WHITE) == null || board.king(Side.BLACK) == null) {
            return false
        }
        val pieces = EnumMap<Piece, MutableSet<Locus>>(Piece::class.java)
        board.forEach { piece, locus ->
            if (!pieces.containsKey(piece)) {
                pieces[piece] = mutableSetOf()
            }
            pieces[piece]!!.add(locus)
        }
        if (pieces.size == 1) { // only kings
            return true
        }
        if (pieces.size == 2) {
            if (pieces[Piece.Knight]?.size == 1) {
                return true
            }
            if (pieces[Piece.Bishop]?.size == 1) {
                return true
            }
            if (pieces[Piece.Bishop]?.size == 2) {
                // Check if the bishops are of the same colour
                // It doesn't matter on whose side they are
                return pieces[Piece.Bishop]?.map { it.side() }?.toMutableSet()?.size == 1
            }
        }
        return false
    }

    private fun isDrawByRepetition(current: GameState): Boolean {
        if (plies.size < settings.drawByRepetitionCount * 2) {
            return false
        }
        var count = 1
        tempBoard.from(board)
        for (i in plies.size - 1 downTo 0) {
            plies[i].ply.undo(tempBoard)
            if (plies[i].ply.turn == current.turn) { // only check every other ply
                if (tempBoard == board) { // is it the same position?
                    if (++count == settings.drawByRepetitionCount) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isDrawByMoveRule(): Boolean =
        (plies.size >= settings.drawByMoveRuleCount * 2) &&
        plies.takeLast(settings.drawByMoveRuleCount * 2)
            .map { it.ply }
            .all { it.piece != Piece.Pawn && ((it as? StandardPly)?.captured == null) }

}