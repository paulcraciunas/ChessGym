package com.paulcraciunas.screens.blindmode.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.screens.common.model.BoardViewData

sealed class BlindModeUiState {

    data class Setup(
        val isTrainingMode: Boolean = true,
        val selectedSide: SideSelection = SideSelection.WHITE,
    ) : BlindModeUiState()

    data class Playing(
        val moveHistory: String = "",
        val playerSide: Side = Side.WHITE,
        val selectedSquare: Locus? = null,
        val legalMoves: List<Locus> = emptyList(),
        val isRevealAvailable: Boolean = true,
        val isThinking: Boolean = false,
        val promotionPending: Boolean = false,
        val isAbandonDialogShown: Boolean = false,
    ) : BlindModeUiState()

    data class Revealing(
        val boardData: BoardViewData,
        val moveHistory: String = "",
    ) : BlindModeUiState()

    data class GameOver(
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
