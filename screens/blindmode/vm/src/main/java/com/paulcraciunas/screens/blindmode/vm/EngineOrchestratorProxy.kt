package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class EngineOrchestratorProxy(
    private val proxy: EngineOrchestrator,
) : EngineOrchestrator {
    val isThinking = MutableStateFlow(false)

    override suspend fun startNewGame(elo: Int) = proxy.startNewGame(elo)
    override suspend fun requestEngineMove(game: Game): Ply {
        isThinking.update { true }
        try {
            return proxy.requestEngineMove(game)
        } finally {
            isThinking.update { false }
        }
    }

    override suspend fun stop() = proxy.stop()
}
