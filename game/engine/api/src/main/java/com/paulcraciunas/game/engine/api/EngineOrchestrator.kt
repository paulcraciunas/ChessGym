package com.paulcraciunas.game.engine.api

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply

/**
 * Orchestrates engine interactions for a chess game, abstracting the underlying
 * engine protocol details and providing resolved [Ply] instances ready to play.
 */
interface EngineOrchestrator {
    suspend fun startNewGame(elo: Int = ChessEngine.DEFAULT_ELO)
    suspend fun requestEngineMove(game: Game): Ply
    suspend fun stop()
}
