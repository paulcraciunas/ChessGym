package com.paulcraciunas.screens.blindmode.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeScreenInteractor
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.RefreshButton
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun GameOverContent(
    state: BlindModeUiState.GameOver,
    showBorders: Boolean,
    interactions: BlindModeScreenInteractor,
) {
    ChessBoard(
        board = state.boardData,
        orientation = BoardOrientation.fromSide(state.playerSide),
        onClick = {},
        showBorders = showBorders,
        enableAnimations = false,
        modifier = Modifier.fillMaxWidth()
    )

    ChessGymSpacer(size = SpacerSize.LARGE)

    Text(
        text = stringResource(state.result.stringRes()),
        style = Design.typography.headlineSmall,
        color = if (state.result == BlindModeUiState.GameResult.Win) {
            Design.colors.primary
        } else {
            Design.colors.danger
        },
    )

    ChessGymSpacer(size = SpacerSize.LARGE)

    RefreshButton(
        onClick = interactions::onPlayAgain,
        text = R.string.blind_mode_play_again,
    )

    if (state.moveHistory.isNotEmpty()) {
        MoveHistoryDisplay(moveHistory = state.moveHistory)
    }
}

@StringRes
private fun BlindModeUiState.GameResult.stringRes(): Int = when (this) {
    BlindModeUiState.GameResult.Win -> R.string.result_checkmate
    BlindModeUiState.GameResult.Draw -> R.string.result_draw
    BlindModeUiState.GameResult.Loss -> R.string.result_loss
}
