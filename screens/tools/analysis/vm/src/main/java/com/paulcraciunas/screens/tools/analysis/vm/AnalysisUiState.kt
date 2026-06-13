package com.paulcraciunas.screens.tools.analysis.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardState

@Immutable
data class AnalysisUiState(
    val data: BoardState = BoardState.empty,
    val orientation: Side = Side.WHITE,
    val engineData: EngineData? = null,
    val canNavigateBack: Boolean = false,
    val canNavigateForward: Boolean = false,
) {
    @Immutable
    data class EngineData(
        val evaluation: CurrentEvaluation,
        val engineLines: List<SuggestedLine>,
        val topMove: SuggestedMove?,
        val analysisDepth: Int,
    ) {
        @Immutable
        data class SuggestedLine(
            val rank: Int,
            val evaluation: CurrentEvaluation,
            val moves: String,
        )

        @Immutable
        data class SuggestedMove(
            val from: Locus,
            val to: Locus,
            val promotion: Piece? = null,
        )

        @Immutable
        data class CurrentEvaluation(
            val normalised: Float,
            val display: String,
        )
    }
}
