package com.paulcraciunas.game.logic.impl.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.impl.board.Board

fun interface GameOverStrategy {
    operator fun invoke(of: Game): Game.GameState

    companion object {
        /**
         * To be shared by children. In order to avoid creating loads of these pointlessly.
         *
         * IMPORTANT: As this is effectively global state, it shouldn't be used in a multi-threaded context.
         * I.e. if we ever - doubt it - want to have multiple games running in parallel, this needs to be refactored.
         */
        @JvmStatic
        val tempBoard = Board()
    }
}
