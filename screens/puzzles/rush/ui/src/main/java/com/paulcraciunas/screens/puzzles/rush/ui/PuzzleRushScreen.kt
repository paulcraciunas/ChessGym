package com.paulcraciunas.screens.puzzles.rush.ui

import android.annotation.SuppressLint
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AnimatedBoard
import com.paulcraciunas.screens.common.AnimatedControls
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.controls.PuzzleResultsGrid
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleRushScreen(
    uiState: PuzzleRushUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSquareClicked: (selection: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onPlayAgain: () -> Unit = {},
    onDismissSummary: () -> Unit = {},
    onAnalyzeFailedPuzzle: (puzzleId: Int) -> Unit = {},
) {
    val timeRemainingSeconds = when (uiState) {
        is PuzzleRushUiState.WithBoard -> uiState.timeRemainingSeconds
        else -> 180 // Default 3 minutes
    }
    Scaffold(
        topBar = {
            ChildAppBar(
                title = stringResource(R.string.puzzle_mode_rush_title),
                onBack = onNavigateBack,
                actions = {
                    TimerDisplay(
                        seconds = timeRemainingSeconds,
                        modifier = Modifier.padding(end = Design.dimensions.spacing.xxl)
                    )
                }
            )
        },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { PuzzleRushScreenTags.SCREEN },
    ) { innerPadding ->
        val defaultModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            is PuzzleRushUiState.Loading -> LoadingContent(modifier = defaultModifier)
            is PuzzleRushUiState.Failed -> FailedContent(modifier = defaultModifier)
            is PuzzleRushUiState.WithBoard -> {
                PuzzleRushContent(
                    uiState = uiState,
                    onSquareClicked = onSquareClicked,
                    onPromote = onPromote,
                    onPlayAgain = onPlayAgain,
                    onDismissSummary = onDismissSummary,
                    onAnalyzeFailedPuzzle = onAnalyzeFailedPuzzle,
                    modifier = defaultModifier,
                )
            }
        }
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
private fun PuzzleRushContent(
    uiState: PuzzleRushUiState.WithBoard,
    onSquareClicked: (selection: Locus) -> Unit,
    onPromote: (to: Piece) -> Unit,
    onPlayAgain: () -> Unit,
    onDismissSummary: () -> Unit,
    onAnalyzeFailedPuzzle: (puzzleId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    Column(
        modifier = modifier.blur(Design.dimensions.blur.of(uiState is PuzzleRushUiState.Finished && uiState.showSummaryDialog))
    ) {
        // Animate board transition when puzzle count changes
        CapturedPieces(capturedPieces = data.captured.byOpponent, side = data.player, modifier = Modifier.fillMaxWidth())
        AnimatedBoard(
            targetState = data,
            contentKey = { it.id },
        ) { puzzleData ->
            ChessBoard(
                board = puzzleData.boardData,
                orientation = BoardOrientation.fromSide(puzzleData.player),
                onClick = onSquareClicked,
                modifier = Modifier.fillMaxWidth()
            )
        }
        CapturedPieces(capturedPieces = data.captured.byPlayer, side = data.player.other(), modifier = Modifier.fillMaxWidth())
        ChessGymSpacer()
        AnimatedControls(targetState = uiState) { state ->
            when (state) {
                is PuzzleRushUiState.Ready -> {
                    ReadyControls(modifier = Modifier.fillMaxWidth())
                }
                is PuzzleRushUiState.Playing -> {
                    // Empty space while playing - no controls needed
                    ChessGymSpacer(size = SpacerSize.SECTION)
                }
                is PuzzleRushUiState.Finished -> {
                    FinishedRushControls(
                        onPlayAgain = onPlayAgain,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
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
        if (uiState is PuzzleRushUiState.Playing && uiState.data.promotion != null) {
            PromotionDialog(
                side = data.player,
                onPieceChosen = onPromote
            )
        }
        if (uiState is PuzzleRushUiState.Finished && uiState.showSummaryDialog) {
            RushSummaryDialog(
                puzzlesSolved = uiState.results.count { it.success },
                isNewHighScore = uiState.isNewHighScore,
                onDismiss = onDismissSummary,
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ReadyPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            PuzzleRushScreen(
                uiState = PuzzleRushUiState.Ready(
                    data = PreviewData().blackPuzzleData(),
                    timeRemainingSeconds = 180,
                ),
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            PuzzleRushScreen(
                uiState = PuzzleRushUiState.Playing(
                    data = PreviewData().blackPuzzleData(),
                    timeRemainingSeconds = 142,
                    results = PreviewData().fewResults(),
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
            PuzzleRushScreen(
                uiState = PuzzleRushUiState.Finished(
                    data = PreviewData().whitePuzzleData(),
                    timeRemainingSeconds = 0,
                    results = PreviewData().manyResults(),
                    showSummaryDialog = false,
                    isNewHighScore = false,
                ),
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ManyResultsPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            PuzzleRushScreen(
                uiState = PuzzleRushUiState.Playing(
                    data = PreviewData().whitePuzzleData(),
                    timeRemainingSeconds = 45,
                    results = PreviewData().manyResults(),
                ),
            )
        }
    }
}
