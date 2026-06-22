package com.paulcraciunas.domain.api.analysis

import com.paulcraciunas.game.logic.api.Game
import kotlinx.coroutines.flow.Flow

interface AnalyzeFullGame {
    fun analyze(positions: List<String>, depth: Int = DEFAULT_ANALYSIS_DEPTH): Flow<GameAnalysisProgress>
    fun analyze(game: Game, depth: Int = DEFAULT_ANALYSIS_DEPTH): Flow<GameAnalysisProgress>

    companion object {
        const val DEFAULT_ANALYSIS_DEPTH: Int = 18
    }
}
