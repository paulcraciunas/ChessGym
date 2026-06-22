package com.paulcraciunas.domain.api.analysis

sealed class GameAnalysisProgress {
    data class Analyzing(
        val currentMove: Int,
        val totalMoves: Int,
        val latestMoveAnalysis: MoveAnalysis,
    ) : GameAnalysisProgress()

    data class Completed(val analysis: GameAnalysis) : GameAnalysisProgress()
}
