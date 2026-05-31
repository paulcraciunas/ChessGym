package com.paulcraciunas.screens.tools.analysis.ui

import com.paulcraciunas.screens.tools.analysis.vm.AnalysisUiState

class Previews {
    fun engineLines() = listOf(
        AnalysisUiState.EngineData.SuggestedLine(
            rank = 1,
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.6f, display = "+0.3"),
            moves = "e2e4 e7e5 Nf3",
        ),
        AnalysisUiState.EngineData.SuggestedLine(
            rank = 2,
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.55f, display = "+0.2"),
            moves = "d2d4 d7d5",
        ),
        AnalysisUiState.EngineData.SuggestedLine(
            rank = 3,
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.51f, display = "+0.1"),
            moves = "g1f3 d7d5",
        ),
    )
}
