package com.paulcraciunas.screens.tools.importgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.board.MoveArrowOverlay
import com.paulcraciunas.screens.common.controls.MoveNavigationControls
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameUiState

@Composable
internal fun CompleteContent(
    state: ImportGameUiState.Complete,
    modifier: Modifier = Modifier,
    onMoveSelected: (Int) -> Unit = {},
    onJumpToStart: () -> Unit = {},
    onPreviousMove: () -> Unit = {},
    onNextMove: () -> Unit = {},
    onJumpToEnd: () -> Unit = {},
) {
    val orientation = BoardOrientation.fromSide(state.orientation)
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
            orientation = orientation,
            onClick = {}, // interactions not allowed
            modifier = Modifier.fillMaxWidth(),
        ) {
            state.blunderOverlay?.let {
                MoveArrowOverlay(
                    from = it.from,
                    to = it.to,
                    orientation = orientation,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Text(
            text = state.playerInfo.bottomName(state.orientation),
            textAlign = TextAlign.Start,
            style = Design.typography.titleSmall,
            color = Design.colors.ink,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Design.dimensions.spacing.lg),
        )

        MoveNavigationControls(
            canGoBack = state.canNavigateBack,
            canGoForward = state.canNavigateForward,
            onJumpToStart = onJumpToStart,
            onPreviousMove = onPreviousMove,
            onNextMove = onNextMove,
            onJumpToEnd = onJumpToEnd,
        )
        ChessGymSpacer(size = SpacerSize.LARGE)
        MoveList(
            moves = state.analysedMoves,
            currentMoveIndex = state.currentMoveIndex,
            onMoveSelected = onMoveSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Design.dimensions.spacing.xgut),
        )
        ChessGymSpacer(size = SpacerSize.XXLARGE)
    }
}

@Preview
@Composable
private fun CompleteContentPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            CompleteContent(
                state = ImportGameUiState.Complete(
                    boardState = PreviewData().whiteGameData(),
                    analysedMoves = previewMoves(),
                    currentMoveIndex = 1,
                    canNavigateBack = true,
                    canNavigateForward = true,
                ),
                modifier = Modifier.background(Design.colors.primarySoft),
            )
        }
    }
}
