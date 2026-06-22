package com.paulcraciunas.game.engine.api

import kotlinx.coroutines.flow.Flow

/**
 * Facade for a chess engine that can calculate best moves for a given position.
 * Implementations communicate with the underlying engine and abstract its protocol details.
 */
interface ChessEngine {
    suspend fun initialize()
    suspend fun startNewGame(elo: Int = DEFAULT_ELO)
    suspend fun calculateBestMove(fen: String): EngineMove
    suspend fun prepareForAnalysis()
    fun analyzePosition(fen: String, multiPvCount: Int = DEFAULT_MULTI_PV): Flow<AnalysisResult>
    suspend fun evaluatePosition(fen: String, depth: Int = DEFAULT_ANALYSIS_DEPTH): PositionEvaluation
    suspend fun stopAnalysis()
    suspend fun stop()
    suspend fun shutdown()

    companion object {
        const val DEFAULT_ELO: Int = 1350
        const val DEFAULT_MULTI_PV: Int = 3
        const val DEFAULT_ANALYSIS_DEPTH: Int = 18
    }
}
