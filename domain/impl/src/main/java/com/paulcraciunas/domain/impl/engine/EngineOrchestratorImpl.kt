package com.paulcraciunas.domain.impl.engine

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.serializer.api.Serializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EngineOrchestratorImpl(
    private val chessEngine: ChessEngine,
    private val serializer: Serializer,
    private val dispatcher: CoroutineDispatcher,
) : EngineOrchestrator {
    override suspend fun startNewGame(elo: Int): Unit = withContext(dispatcher) {
        chessEngine.startNewGame(elo)
    }

    override suspend fun requestEngineMove(game: Game): Ply = withContext(dispatcher) {
        val fen = serializer.of(game)
        val engineMove = chessEngine.calculateBestMove(fen)

        val ply = game.plies(engineMove.from)
            .first { it.to == engineMove.to }

        engineMove.promotion?.let { ply.promote(it) }

        return@withContext ply
    }

    override suspend fun stop(): Unit = withContext(dispatcher) {
        chessEngine.stop()
    }
}
