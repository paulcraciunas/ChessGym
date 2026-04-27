package com.paulcraciunas.domain.api.analysis

import com.paulcraciunas.game.engine.api.AnalysisResult
import kotlinx.coroutines.flow.Flow

interface AnalyzePosition {
    fun invoke(fen: String): Flow<AnalysisResult>
}
