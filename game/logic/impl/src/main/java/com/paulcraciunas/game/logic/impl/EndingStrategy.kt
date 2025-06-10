package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.CheckCount
import com.paulcraciunas.game.logic.api.state.Settings
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.plies.Playable
import java.util.EnumMap

//TODO Paul: I should probably add unit tests for this class as there's a lot of logic here
internal class EndingStrategy(
    private val availablePlies: Collection<Playable>,
    private val board: IBoard,
    private val plies: List<Playable>,
    private val settings: Settings,
) {
    private val tempBoard = Board()

    fun of(current: GameState): Result? = when {
        isCheckMate(current) -> Result.CheckMate
        isStaleMate(current) -> Result.StaleMate
        isDrawByMoveRule(current) -> Result.DrawByMoveRule
        isDrawByInsufficientMaterial() -> Result.DrawByInsufficientMaterial
        isDrawByRepetition(current) -> Result.DrawByRepetition
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
            plies[i].undo(tempBoard)
            if (plies[i].turn == current.turn) { // only check every other ply
                if (tempBoard == board) { // is it the same position?
                    if (++count == settings.drawByRepetitionCount) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isDrawByMoveRule(current: GameState): Boolean =
        // TODO Paul: add tests for these scenarios
        current.plieClock == settings.drawByMoveRuleCount * 2
}