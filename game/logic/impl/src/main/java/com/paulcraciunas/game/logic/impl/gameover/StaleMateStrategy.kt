package com.paulcraciunas.game.logic.impl.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.state.CheckCount

internal class StaleMateStrategy : GameOverStrategy {
    override fun invoke(of: Game): Game.GameState =
        if (of.info.inCheckCount == CheckCount.None && of.info.plies.isEmpty()) Game.GameState.Finished(Result.StaleMate)
        else of.state
}
