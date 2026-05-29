package com.paulcraciunas.screens.puzzles.failed.ui

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AnimatedBoard
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.controls.PuzzleResultsGrid
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailedPuzzlesScreen(
    uiState: FailedPuzzlesUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSquareClicked: (selection: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onDismissCompletion: () -> Unit = {},
    onAnalyzeFailedPuzzle: (puzzleId: Int) -> Unit = {},
) {
    val progress = when (uiState) {
        is FailedPuzzlesUiState.BoardState -> uiState.progress
        else -> null
    }

    Scaffold(
        topBar = {
            ChildAppBar(
                title = stringResource(R.string.puzzle_mode_failed_title),
                onBack = onNavigateBack,
                actions = {
                    progress?.let {
                        ProgressIndicator(
                            progress = it,
                            modifier = Modifier.padding(end = Design.dimensions.spacing.xxl)
                        )
                    }
                }
            )
        },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { FailedPuzzlesScreenTags.SCREEN },
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            is FailedPuzzlesUiState.Empty -> EmptyFailedPuzzlesContent(modifier = screenModifier)
            is FailedPuzzlesUiState.Loading -> LoadingContent(modifier = screenModifier)
            is FailedPuzzlesUiState.Failed -> FailedContent(modifier = screenModifier)
            is FailedPuzzlesUiState.BoardState -> {
                FailedPuzzlesContent(
                    uiState = uiState,
                    onSquareClicked = onSquareClicked,
                    onPromote = onPromote,
                    onDismissCompletion = onDismissCompletion,
                    onAnalyzeFailedPuzzle = onAnalyzeFailedPuzzle,
                    modifier = screenModifier,
                )
            }
        }
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
private fun FailedPuzzlesContent(
    uiState: FailedPuzzlesUiState.BoardState,
    onSquareClicked: (selection: Locus) -> Unit,
    onPromote: (to: Piece) -> Unit,
    onDismissCompletion: () -> Unit,
    onAnalyzeFailedPuzzle: (puzzleId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        val isBoardInteractive = uiState !is FailedPuzzlesUiState.Playing || !uiState.isAnimating
        CapturedPieces(capturedPieces = data.captured.byOpponent, side = data.player, modifier = Modifier.fillMaxWidth())
        AnimatedBoard(
            targetState = data,
            contentKey = { it.id },
        ) { puzzleData ->
            ChessBoard(
                board = puzzleData.boardData,
                orientation = BoardOrientation.fromSide(puzzleData.player),
                onClick = if (isBoardInteractive) onSquareClicked else { _ -> },
                modifier = Modifier.fillMaxWidth()
            )
        }
        CapturedPieces(capturedPieces = data.captured.byPlayer, side = data.player.other(), modifier = Modifier.fillMaxWidth())
        ChessGymSpacer(size = SpacerSize.XXLARGE)
        if (uiState.results.isNotEmpty()) {
            PuzzleResultsGrid(
                results = uiState.results,
                onFailedPuzzleClicked = onAnalyzeFailedPuzzle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Design.dimensions.spacing.xxl)
            )
        }
        if (uiState is FailedPuzzlesUiState.Playing && uiState.promotion != null) {
            PromotionDialog(
                side = data.player,
                onPieceChosen = onPromote
            )
        }
        if (uiState is FailedPuzzlesUiState.Finished && uiState.showCompletionDialog) {
            FailedPuzzlesCompletionDialog(
                puzzlesSolved = uiState.progress.solved,
                onDismiss = onDismissCompletion,
            )
        }
    }
}

@Composable
private fun ProgressIndicator(
    progress: FailedPuzzlesUiState.Progress,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.failed_puzzles_progress, progress.solved, progress.total),
        style = Design.typography.titleMedium,
        color = Design.colors.primary,
        modifier = modifier
    )
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            FailedPuzzlesScreen(
                uiState = FailedPuzzlesUiState.Playing(
                    data = PreviewData().blackPuzzleData(),
                    progress = FailedPuzzlesUiState.Progress(solved = 3, total = 10),
                    results = PreviewData().fewResults(),
                    promotion = null,
                ),
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FinishedPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            FailedPuzzlesScreen(
                uiState = FailedPuzzlesUiState.Finished(
                    data = PreviewData().whitePuzzleData(),
                    progress = FailedPuzzlesUiState.Progress(solved = 4, total = 5),
                    results = PreviewData().manyResults(),
                    showCompletionDialog = false,
                ),
            )
        }
    }
}
