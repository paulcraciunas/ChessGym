package com.paulcraciunas.game.logic.impl.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import java.util.EnumMap

internal class DrawByInsufficientMaterialStrategy : GameOverStrategy {

    override fun invoke(of: Game): Game.GameState =
        if (isDraw(of)) Game.GameState.Finished(Result.DrawByInsufficientMaterial)
        else of.state

    private fun isDraw(of: Game): Boolean {
        // If we don't have 2 kings (e.g. some custom puzzle) then we can't have a normal draw
        if (of.board.king(Side.WHITE) == null || of.board.king(Side.BLACK) == null) {
            return false
        }
        val pieces = EnumMap<Piece, MutableSet<Locus>>(Piece::class.java)
        of.board.forEach { piece, locus ->
            if (!pieces.containsKey(piece)) {
                pieces[piece] = mutableSetOf()
            }
            pieces[piece]!!.add(locus)
        }
        if (pieces.size == 1) { // only kings
            return true
        } else if (pieces.size == 2) { // one of them is the king
            if (pieces[Piece.Knight]?.size == 1 ||
                pieces[Piece.Bishop]?.size == 1
            ) {
                return true
            }
            if (pieces[Piece.Bishop]?.size == 2) {
                // Check if the bishops are of the same colour
                // It doesn't matter on whose side they are
                return pieces[Piece.Bishop]?.map { it.side }?.toMutableSet()?.size == 1
            }
        }
        return false
    }
}
