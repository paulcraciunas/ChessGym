package com.paulcraciunas.domain.api.analysis

import com.paulcraciunas.game.logic.api.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAnalyzeFullGame : AnalyzeFullGame {
    private var result: List<GameAnalysisProgress> = emptyList()

    fun setResult(vararg progress: GameAnalysisProgress) {
        result = progress.toList()
    }

    override fun analyze(game: Game, depth: Int): Flow<GameAnalysisProgress> = flow {
        result.forEach { emit(it) }
    }
}
