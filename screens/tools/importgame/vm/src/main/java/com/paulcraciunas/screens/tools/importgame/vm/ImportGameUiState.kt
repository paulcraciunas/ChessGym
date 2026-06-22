package com.paulcraciunas.screens.tools.importgame.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.domain.api.analysis.MoveClassification
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.BoardState

@Immutable
sealed interface ImportGameUiState {
    @Immutable
    data class Setup(val hasImportError: Boolean = false) : ImportGameUiState

    @Immutable
    data class Loading(
        val boardState: BoardState = BoardState.empty,
        val orientation: Side = Side.WHITE,
        val playerInfo: PlayerInfo = PlayerInfo(),
        val analysedMoves: List<AnalysedMove> = emptyList(),
        val showAbandonDialog: Boolean = false,
        val progressPercent: Int = 0,
    ) : ImportGameUiState

    @Immutable
    data class Complete(
        val boardState: BoardState = BoardState.empty,
        val orientation: Side = Side.WHITE,
        val playerInfo: PlayerInfo = PlayerInfo(),
        val analysedMoves: List<AnalysedMove> = emptyList(),
        val currentMoveIndex: Int = 0,
        val canNavigateBack: Boolean = false,
        val canNavigateForward: Boolean = false,
        val blunderOverlay: BlunderOverlay? = null,
    ) : ImportGameUiState

    @Immutable
    data class PlayerInfo(
        val whiteName: String? = null,
        val blackName: String? = null,
    )

    @Immutable
    data class AnalysedMove(
        val algebraic: String,
        val classification: MoveClassification,
        val normalised: Float,
        val bestMove: BestMove?,
    )

    @Immutable
    data class BestMove(
        val from: Locus,
        val to: Locus,
        val promotion: Piece?,
    )

    @Immutable
    data class BlunderOverlay(
        val from: Locus,
        val to: Locus,
    )
}
