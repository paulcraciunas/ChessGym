package com.paulcraciunas.domain.api.analysis

import com.paulcraciunas.game.engine.api.AnalysisResult
import kotlinx.coroutines.flow.Flow

interface AnalyzePosition {
    suspend fun prepare()
    fun analyze(fen: String): Flow<AnalysisResult>
    suspend fun stop()
    suspend fun shutdown()
}
