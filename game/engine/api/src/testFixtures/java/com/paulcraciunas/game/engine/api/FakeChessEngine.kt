package com.paulcraciunas.game.engine.api

import java.util.LinkedList

/**
 * A test double for [ChessEngine] that returns pre-programmed moves from a queue.
 */
// TODO Paul: this should be a proper fake. Perhaps load it by default with some moves (e.g. Spanish game)
class FakeChessEngine : ChessEngine {
    private val moveQueue: LinkedList<EngineMove> = LinkedList()
    var isInitialized: Boolean = false
        private set
    var isShutdown: Boolean = false
        private set
    var currentElo: Int = ChessEngine.DEFAULT_ELO
        private set
    var lastReceivedFen: String? = null
        private set

    fun enqueueMoves(vararg moves: EngineMove) {
        moveQueue.addAll(moves)
    }

    override suspend fun initialize() {
        isInitialized = true
    }

    override suspend fun startNewGame(elo: Int) {
        currentElo = elo
    }

    override suspend fun calculateBestMove(fen: String): EngineMove {
        lastReceivedFen = fen
        return moveQueue.poll()
            ?: throw IllegalStateException("FakeChessEngine: no moves enqueued")
    }

    override suspend fun stop() {
        // No-op in fake
    }

    override suspend fun shutdown() {
        isShutdown = true
    }
}
