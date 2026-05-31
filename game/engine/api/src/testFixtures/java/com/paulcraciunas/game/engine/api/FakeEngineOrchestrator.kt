package com.paulcraciunas.game.engine.api

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import java.util.LinkedList

class FakeEngineOrchestrator : EngineOrchestrator {
    private val moveQueue: LinkedList<Ply> = LinkedList()

    var isStopped: Boolean = false
        private set
    var currentElo: Int = ChessEngine.DEFAULT_ELO
        private set

    fun enqueueMoves(vararg moves: Ply) {
        moveQueue.addAll(moves)
    }

    override suspend fun startNewGame(elo: Int) {
        isStopped = false
        currentElo = elo
    }

    override suspend fun requestEngineMove(game: Game): Ply =
        moveQueue.poll() ?: throw IllegalStateException("FakeEngineOrchestrator: no moves enqueued")

    override suspend fun stop() {
        isStopped = true
    }
}
