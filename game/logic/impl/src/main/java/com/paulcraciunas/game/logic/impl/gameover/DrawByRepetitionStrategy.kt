package com.paulcraciunas.game.logic.impl.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.impl.gameover.GameOverStrategy.Companion.tempBoard
import com.paulcraciunas.game.logic.impl.plies.Playable

internal class DrawByRepetitionStrategy : GameOverStrategy {

    override fun invoke(of: Game): Game.GameState {
        val history = of.history
        if (history.size < DRAW_BY_REPETITION_COUNT * 2) {
            return of.state
        }
        var count = 1
        tempBoard.from(of.board)
        for (i in of.history.size - 1 downTo 0) {
            (of.history[i] as Playable).undo(tempBoard)
            if (of.history[i].turn == of.info.turn) { // only check every other ply
                if (tempBoard == of.board) { // is it the same position?
                    if (++count == DRAW_BY_REPETITION_COUNT) {
                        return Game.GameState.Finished(Result.DrawByRepetition)
                    }
                }
            }
        }
        return of.state
    }

    private companion object {
        const val DRAW_BY_REPETITION_COUNT = 3
    }
}
