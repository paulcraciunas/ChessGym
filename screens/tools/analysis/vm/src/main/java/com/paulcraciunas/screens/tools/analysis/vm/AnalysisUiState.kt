package com.paulcraciunas.screens.tools.analysis.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.GameViewModelHelper
import com.paulcraciunas.screens.common.model.GameViewModelHelper.GameData2

@Immutable
data class AnalysisUiState(
    val data: GameData2 = GameData2(
        rating = null,
        player = Side.WHITE,
        boardData = BoardViewData.empty(),
        captured = GameData2.GameCaptured(byOpponent = "", byPlayer = "")
    ),
    val promotion: GameViewModelHelper.GamePromotion? = null,
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
