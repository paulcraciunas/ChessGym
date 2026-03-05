package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.screens.common.model.BoardViewData

sealed class BlindModeUiState {
    abstract val isTrainingMode: Boolean
    abstract val selectedSide: SideSelection

    data class PendingPromotion(val from: Locus, val to: Locus)

    data class Setup(
        override val isTrainingMode: Boolean = true,
        override val selectedSide: SideSelection = SideSelection.WHITE,
    ) : BlindModeUiState()

    data class Playing(
        override val isTrainingMode: Boolean = true,
        override val selectedSide: SideSelection = SideSelection.WHITE,
        val moveHistory: String = "",
        val playerSide: Side = Side.WHITE,
        val selectedSquare: Locus? = null,
        val legalMoves: List<Locus> = emptyList(),
        val isRevealAvailable: Boolean = true,
        val isThinking: Boolean = false,
        val pendingPromotion: PendingPromotion? = null,
        val isAbandonDialogShown: Boolean = false,
    ) : BlindModeUiState()

    data class Revealing(
        override val isTrainingMode: Boolean = true,
        override val selectedSide: SideSelection = SideSelection.WHITE,
        val playerSide: Side = Side.WHITE,
        val boardData: BoardViewData,
        val moveHistory: String = "",
    ) : BlindModeUiState()

    data class GameOver(
        override val isTrainingMode: Boolean = true,
        override val selectedSide: SideSelection = SideSelection.WHITE,
        val boardData: BoardViewData,
        val moveHistory: String = "",
        val result: GameResult = GameResult.Win,
    ) : BlindModeUiState()

    enum class GameResult {
        Win,
        Draw,
        Loss
    }
}
