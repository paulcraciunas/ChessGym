package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.game.engine.api.AnalysisResult
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.engine.api.UciMoveParser
import com.paulcraciunas.game.logic.api.Side

internal class EngineDataAdapter {
    fun from(result: AnalysisResult, sideToMove: Side): AnalysisUiState.EngineData = AnalysisUiState.EngineData(
        evaluation = normalizeEvaluation(result.evaluation, sideToMove),
        engineLines = result.lines.map {
            AnalysisUiState.EngineData.SuggestedLine(
                rank = it.rank,
                evaluation = normalizeEvaluation(it.evaluation, sideToMove),
                moves = it.moves.joinToString(" ") { move -> UciMoveParser.format(move) },
            )
        },
        topMove = extractTopMove(result.lines),
        analysisDepth = result.depth,
    )

    private fun normalizeEvaluation(evaluation: Evaluation, sideToMove: Side): AnalysisUiState.EngineData.CurrentEvaluation {
        if (sideToMove == Side.WHITE) return evaluation.adapt()

        return when (evaluation) {
            is Evaluation.Centipawns -> Evaluation.Centipawns(-evaluation.value).adapt()
            is Evaluation.Mate -> Evaluation.Mate(-evaluation.movesToMate).adapt()
        }
    }

    private fun Evaluation.adapt() = AnalysisUiState.EngineData.CurrentEvaluation(
        normalised = toFraction(),
        display = format(),
    )

    private fun extractTopMove(lines: List<EngineLine>): AnalysisUiState.EngineData.SuggestedMove? {
        val topMove = lines.firstOrNull()?.moves?.firstOrNull() ?: return null
        return AnalysisUiState.EngineData.SuggestedMove(from = topMove.from, to = topMove.to)
    }
}
