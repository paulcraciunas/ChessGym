package com.paulcraciunas.game.logic.impl.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result

internal class DrawByMoveRuleStrategy : GameOverStrategy {

    override fun invoke(of: Game): Game.GameState =
        if (of.info.plieClock == DRAW_BY_MOVE_RULE_COUNT * 2) Game.GameState.Finished(Result.DrawByMoveRule)
        else of.state

    private companion object {
        const val DRAW_BY_MOVE_RULE_COUNT = 50
    }
}
