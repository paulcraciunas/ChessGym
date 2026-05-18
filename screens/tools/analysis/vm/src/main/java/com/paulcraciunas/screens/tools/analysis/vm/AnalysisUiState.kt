package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.previews.SampleBoardViewData

data class AnalysisUiState(
    val boardData: BoardViewData = SampleBoardViewData.startingBoard(),
    val pendingPromotion: PendingPromotion? = null,
    val playerSide: Side = Side.WHITE,
    val evaluation: Evaluation? = null,
    val engineLines: List<EngineLine> = emptyList(),
    val topMoveArrow: MoveArrow? = null,
    val isAnalyzing: Boolean = false,
    val analysisDepth: Int = 0,
    val captured: Map<Side, List<Piece>> = HashMap<Side, List<Piece>>().apply {
        this[Side.WHITE] = emptyList()
        this[Side.BLACK] = emptyList()
    },
    val currentMoveIndex: Int = 0,
    val totalMoves: Int = 0,
)

data class PendingPromotion(val from: Locus, val to: Locus)

data class MoveArrow(val from: Locus, val to: Locus)
