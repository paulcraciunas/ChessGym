package com.paulcraciunas.domain.impl.analysis

import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.game.engine.api.AnalysisResult
import com.paulcraciunas.game.engine.api.ChessEngine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnalyzePositionImpl @Inject constructor(
    private val engine: ChessEngine,
) : AnalyzePosition {

    override suspend fun prepare() {
        engine.prepareForAnalysis()
    }

    override fun invoke(fen: String): Flow<AnalysisResult> {
        return engine.analyzePosition(fen)
    }

    override suspend fun stopAnalysis() {
        engine.stopAnalysis()
    }

    override suspend fun shutdown() {
        engine.shutdown()
    }
}
