package com.paulcraciunas.game.engine.api

/**
 * Facade for a chess engine that can calculate best moves for a given position.
 * Implementations communicate with the underlying engine and abstract its protocol details.
 */
interface ChessEngine {
    suspend fun initialize()
    suspend fun startNewGame(elo: Int = DEFAULT_ELO)
    suspend fun calculateBestMove(fen: String): EngineMove
    suspend fun stop()
    suspend fun shutdown()

    companion object {
        const val DEFAULT_ELO: Int = 1350
    }
}
