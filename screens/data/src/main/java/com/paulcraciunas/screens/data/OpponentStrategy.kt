package com.paulcraciunas.screens.data

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle

interface OpponentStrategy {
    suspend fun init()
    fun canPlay(): Boolean
    suspend fun playNext(): Boolean
    suspend fun shutdown()
}

class ScriptedOpponent(private val puzzle: Puzzle) : OpponentStrategy {
    override suspend fun init() = Unit
    override fun canPlay(): Boolean = !puzzle.state.isOver()
    override suspend fun playNext(): Boolean = if (canPlay()) {
        puzzle.playNextMove()
        true
    } else false

    override suspend fun shutdown() = Unit
}

class EngineOpponent(
    private val orchestrator: EngineOrchestrator,
    private val game: Game,
) : OpponentStrategy {
    override suspend fun init() {
        orchestrator.startNewGame(game.rating ?: ChessEngine.DEFAULT_ELO)
    }

    override fun canPlay(): Boolean = game.state !is Game.GameState.Finished

    override suspend fun playNext(): Boolean = if (canPlay()) {
        game.play(orchestrator.requestEngineMove(game))
        true
    } else false

    override suspend fun shutdown() {
        orchestrator.stop()
    }
}

object NoOpOpponent : OpponentStrategy {
    override suspend fun init() = Unit
    override fun canPlay(): Boolean = false
    override suspend fun playNext(): Boolean = false
    override suspend fun shutdown() = Unit
}
