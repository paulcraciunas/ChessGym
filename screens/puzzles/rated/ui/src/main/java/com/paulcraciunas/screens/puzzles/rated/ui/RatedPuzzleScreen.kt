package com.paulcraciunas.screens.puzzles.rated.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AnimatedControls
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.v2.BoardOrientation2
import com.paulcraciunas.screens.common.board.v2.ChessBoard2
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControls
import com.paulcraciunas.screens.common.controls.v2.CapturedPieces2
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.rated.vm.RatedPuzzleUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatedPuzzleScreen(
    uiState: RatedPuzzleUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSquareClicked: (selection: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onHintRequested: () -> Unit = {},
    onAbandon: () -> Unit = {},
    onAbandonConfirmed: () -> Unit = {},
    onAbandonDismissed: () -> Unit = {},
    onNextPuzzle: () -> Unit = {},
) {
    val title = when (uiState) {
        is RatedPuzzleUiState.BoardState -> stringResource(R.string.rated_puzzle_title, uiState.data.rating)
        else -> stringResource(R.string.puzzle_mode_rated_title)
    }
    Scaffold(
        topBar = { ChildAppBar(title = title, onBack = onNavigateBack) },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { RatedPuzzleScreenTags.SCREEN },
    ) { innerPadding ->
        val defaultModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            is RatedPuzzleUiState.Loading -> LoadingContent(modifier = defaultModifier.testTag { RatedPuzzleScreenTags.LOADING })
            is RatedPuzzleUiState.Failed -> FailedContent(modifier = defaultModifier.testTag { RatedPuzzleScreenTags.FAILED })
            is RatedPuzzleUiState.BoardState -> {
                RatedPuzzleContent(
                    uiState = uiState,
                    onSquareClicked = onSquareClicked,
                    onPromote = onPromote,
                    onHintRequested = onHintRequested,
                    onAbandon = onAbandon,
                    onAbandonConfirmed = onAbandonConfirmed,
                    onAbandonDismissed = onAbandonDismissed,
                    onNextPuzzle = onNextPuzzle,
                    modifier = defaultModifier,
                )
            }
        }
    }
}

@Composable
private fun RatedPuzzleContent(
    uiState: RatedPuzzleUiState.BoardState,
    onSquareClicked: (selection: Locus) -> Unit,
    onPromote: (to: Piece) -> Unit,
    onHintRequested: () -> Unit,
    onAbandon: () -> Unit,
    onAbandonConfirmed: () -> Unit,
    onAbandonDismissed: () -> Unit,
    onNextPuzzle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    val isShowingSolution = uiState is RatedPuzzleUiState.Playing && uiState.isShowingSolution
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CapturedPieces2(capturedPieces = data.captured.byOpponent, side = data.player, modifier = Modifier.fillMaxWidth())
        ChessBoard2(
            board = data.boardData,
            orientation = BoardOrientation2.fromSide(data.player),
            onClick = if (!isShowingSolution) onSquareClicked else { _ -> },
            modifier = Modifier.fillMaxWidth()
        )
        CapturedPieces2(capturedPieces = data.captured.byPlayer, side = data.player.other(), modifier = Modifier.fillMaxWidth())
        AnimatedControls(targetState = uiState is RatedPuzzleUiState.Finished) { isEnded ->
            if (isEnded && uiState is RatedPuzzleUiState.Finished) {
                FinishedPuzzleControls(
                    success = uiState.success,
                    ratingChange = uiState.ratingChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag { RatedPuzzleScreenTags.Finished.CONTROLS },
                    onPlayNext = onNextPuzzle,
                )
            } else if (uiState is RatedPuzzleUiState.Playing) {
                DefaultPuzzleControls(
                    hintEnabled = uiState.hintEnabled && !isShowingSolution,
                    toMove = data.player,
                    onHintRequested = onHintRequested,
                    onAbandonRequested = onAbandon,
                    modifier = Modifier.fillMaxWidth(),
                    abandonEnabled = !isShowingSolution,
                )
            }
        }
        if (uiState is RatedPuzzleUiState.Playing && !isShowingSolution) {
            if (uiState.showAbandonDialog) {
                AbandonConfirmationDialog(
                    onConfirm = onAbandonConfirmed,
                    onDismiss = onAbandonDismissed
                )
            }
            if (uiState.promotion != null) {
                PromotionDialog(
                    side = data.player,
                    onPieceChosen = onPromote
                )
            }
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun WhitePlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            RatedPuzzleScreen(
                uiState = RatedPuzzleUiState.Playing(
                    data = PreviewData().whitePuzzleData(),
                    hintEnabled = true,
                    showAbandonDialog = false,
                    promotion = null,
                ),
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BlackPlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            RatedPuzzleScreen(
                uiState = RatedPuzzleUiState.Playing(
                    data = PreviewData().blackPuzzleData(),
                    hintEnabled = true,
                    showAbandonDialog = false,
                    promotion = null,
                ),
            )
        }
    }
}
