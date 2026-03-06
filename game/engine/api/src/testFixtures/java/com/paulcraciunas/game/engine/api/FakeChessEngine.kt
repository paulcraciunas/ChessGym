package com.paulcraciunas.game.engine.api

import com.paulcraciunas.game.logic.api.board.loc
import java.util.LinkedList

/**
 * A fake [ChessEngine] that returns pre-programmed moves.
 * By default, pre-loaded with the Italian Game opening for Black:
 *   1. ... e5  2. ... Nc6  3. ... Bc5
 *
 * Additional moves can be enqueued via [enqueueMoves].
 */
class FakeChessEngine : ChessEngine {
    private val moveQueue: LinkedList<EngineMove> = LinkedList()
    var isInitialized: Boolean = false
        private set
    var isStopped: Boolean = false
        private set
    var isShutdown: Boolean = false
        private set
    var currentElo: Int = ChessEngine.DEFAULT_ELO
        private set
    var lastReceivedFen: String? = null
        private set

    init {
        loadItalianGameDefense()
    }

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
        isStopped = true
    }

    override suspend fun shutdown() {
        isShutdown = true
    }

    private fun loadItalianGameDefense() {
        enqueueMoves(
            EngineMove(from = "e7".loc(), to = "e5".loc()),
            EngineMove(from = "b8".loc(), to = "c6".loc()),
            EngineMove(from = "f8".loc(), to = "c5".loc()),
        )
    }
}
