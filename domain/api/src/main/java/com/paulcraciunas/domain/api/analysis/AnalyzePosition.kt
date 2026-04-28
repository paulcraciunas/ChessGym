package com.paulcraciunas.domain.api.analysis

import com.paulcraciunas.game.engine.api.AnalysisResult
import kotlinx.coroutines.flow.Flow

interface AnalyzePosition {
    suspend fun prepare()
    operator fun invoke(fen: String): Flow<AnalysisResult>
    suspend fun stopAnalysis()
    suspend fun shutdown()
}
