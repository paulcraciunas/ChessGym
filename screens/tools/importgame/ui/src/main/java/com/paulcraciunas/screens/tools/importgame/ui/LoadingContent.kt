package com.paulcraciunas.screens.tools.importgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.InfiniteProgressIndicator
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.LinearProgress
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationType
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameUiState

@Composable
internal fun LoadingContent(
    state: ImportGameUiState.Loading,
    modifier: Modifier = Modifier,
    onAbandonConfirmed: () -> Unit = {},
    onAbandonDismissed: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.playerInfo.topName(state.orientation),
            textAlign = TextAlign.Start,
            style = Design.typography.titleSmall,
            color = Design.colors.ink,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Design.dimensions.spacing.lg),
        )
        ChessBoard(
            board = state.boardState.boardData,
            orientation = BoardOrientation.fromSide(state.orientation),
            onClick = {}, // no interaction allowed
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = state.playerInfo.bottomName(state.orientation),
            textAlign = TextAlign.Start,
            style = Design.typography.titleSmall,
            color = Design.colors.ink,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Design.dimensions.spacing.lg),
        )
        ChessGymSpacer(size = SpacerSize.SECTION)
        Text(
            text = stringResource(R.string.import_game_analyzing, state.progressPercent),
            style = Design.typography.bodySmall,
            color = Design.colors.inkSoft,
        )
        ChessGymSpacer(size = SpacerSize.LARGE)
        LinearProgress(
            progress = state.progressPercent / 100f,
            color = Design.colors.accent,
            modifier = Modifier
                .padding(horizontal = Design.dimensions.spacing.lg)
                .fillMaxWidth(),
        )
        ChessGymSpacer(size = SpacerSize.SECTION)
        InfiniteProgressIndicator(
            modifier = Modifier.size(Design.dimensions.spacing.xsection),
        )

        if (state.showAbandonDialog) {
            AbandonConfirmationDialog(
                onConfirm = onAbandonConfirmed,
                onDismiss = onAbandonDismissed,
                type = AbandonConfirmationType.Game,
            )
        }
    }
}

@Preview
@Composable
private fun LoadingContentPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            LoadingContent(
                state = ImportGameUiState.Loading(
                    boardState = PreviewData().whiteGameData(),
                    progressPercent = 42,
                    analysedMoves = emptyList(),
                ),
                modifier = Modifier.background(Design.colors.primarySoft)
            )
        }
    }
}

@Preview
@Composable
private fun FlippedContentPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            LoadingContent(
                state = ImportGameUiState.Loading(
                    boardState = PreviewData().whiteGameData(),
                    orientation = Side.BLACK,
                    progressPercent = 42,
                    analysedMoves = emptyList(),
                ),
                modifier = Modifier.background(Design.colors.primarySoft)
            )
        }
    }
}
