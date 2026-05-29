package com.paulcraciunas.screens.tools.analysis.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.CapturedPieces
import com.paulcraciunas.screens.data.PlayableData
import com.paulcraciunas.screens.data.Promotion

@Immutable
data class AnalysisUiState(
    val data: PlayableData = PlayableData(
        rating = null,
        player = Side.WHITE,
        id = null,
        boardData = BoardViewData.empty(),
        captured = CapturedPieces(byOpponent = "", byPlayer = ""),
        isOver = false,
        outcome = null,
    ),
    val promotion: Promotion? = null,
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
