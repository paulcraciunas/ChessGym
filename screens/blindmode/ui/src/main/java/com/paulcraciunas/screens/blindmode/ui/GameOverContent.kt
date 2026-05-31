package com.paulcraciunas.screens.blindmode.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.RefreshButton
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.data.Outcome

@Composable
internal fun GameOverContent(
    state: BlindModeUiState.GameOver,
    onPlayAgain: () -> Unit = {},
) {
    ChessBoard(
        board = state.data.boardData,
        orientation = BoardOrientation.fromSide(state.data.player),
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    )
    ChessGymSpacer(size = SpacerSize.LARGE)
    Text(
        text = stringResource(state.data.outcome.stringRes()),
        style = Design.typography.headlineLarge,
        color = Design.colors.ink,
        textAlign = TextAlign.Center,
    )
    ChessGymSpacer(size = SpacerSize.LARGE)
    RefreshButton(onClick = onPlayAgain)

    if (state.moveHistory.isNotEmpty()) {
        MoveHistoryDisplay(moveHistory = state.moveHistory)
    }
}

@StringRes
private fun Outcome?.stringRes(): Int = when (this) {
    Outcome.Won -> R.string.result_checkmate
    Outcome.Drew -> R.string.result_draw
    Outcome.Lost -> R.string.result_loss
    null -> throw IllegalArgumentException("Can't show a result if the game has no outcome")
}
