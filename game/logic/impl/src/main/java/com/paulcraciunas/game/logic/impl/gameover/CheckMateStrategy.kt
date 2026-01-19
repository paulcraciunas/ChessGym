package com.paulcraciunas.game.logic.impl.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.state.CheckCount

internal class CheckMateStrategy : GameOverStrategy {
    override fun invoke(of: Game): Game.GameState =
        if (of.info.inCheckCount != CheckCount.None && of.plies().isEmpty()) Game.GameState.Finished(Result.CheckMate)
        else of.state
}
