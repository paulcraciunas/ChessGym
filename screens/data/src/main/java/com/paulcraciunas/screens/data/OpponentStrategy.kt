package com.paulcraciunas.screens.data

import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle
import kotlinx.coroutines.delay

interface OpponentStrategy {
    val isParticipating: Boolean get() = true
    suspend fun prepare()
    fun canPlay(): Boolean
    suspend fun playNext(): Boolean
    suspend fun shutdown()
}

class ScriptedOpponent : OpponentStrategy {
    private lateinit var puzzle: Puzzle

    fun load(puzzle: Puzzle) {
        this.puzzle = puzzle
    }

    override suspend fun prepare() = Unit
    override fun canPlay(): Boolean = !puzzle.state.isOver()
    override suspend fun playNext(): Boolean = if (canPlay()) {
        delay(RESPONSE_DELAY_MS)
        puzzle.playNextMove()
        true
    } else false

    override suspend fun shutdown() = Unit

    companion object {
        const val RESPONSE_DELAY_MS = 250L
    }
}

class EngineOpponent(
    private val orchestrator: EngineOrchestrator,
) : OpponentStrategy {
    private lateinit var game: Game

    fun load(game: Game) {
        this.game = game
    }

    override suspend fun prepare() {
        orchestrator.startNewGame(game.rating ?: DEFAULT_ELO)
    }

    override fun canPlay(): Boolean = game.state !is Game.GameState.Finished

    override suspend fun playNext(): Boolean = if (canPlay()) {
        val ply = orchestrator.requestEngineMove(game)
        game.play(ply)
        true
    } else false

    override suspend fun shutdown() {
        orchestrator.stop()
    }

    companion object {
        private const val DEFAULT_ELO = 1350
    }
}

object NoOpOpponent : OpponentStrategy {
    override val isParticipating: Boolean = false
    override suspend fun prepare() = Unit
    override fun canPlay(): Boolean = false
    override suspend fun playNext(): Boolean = false
    override suspend fun shutdown() = Unit
}
