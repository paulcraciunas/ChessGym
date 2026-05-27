package com.paulcraciunas.screens.puzzles.failed.ui

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.controls.PuzzleResultsGrid
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesScreenInteractor
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesUiState
import com.paulcraciunas.screens.puzzles.failed.vm.StubFailedPuzzlesScreenInteractor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailedPuzzlesScreen(
    uiState: FailedPuzzlesUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    interactions: FailedPuzzlesScreenInteractor = StubFailedPuzzlesScreenInteractor(),
) {
    // Handle Empty state separately as it has its own Scaffold
    if (uiState is FailedPuzzlesUiState.Empty) {
        EmptyFailedPuzzlesContent(
            onNavigateBack = onNavigateBack,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val progress = when (uiState) {
        is FailedPuzzlesUiState.BoardState -> uiState.progress
        else -> null
    }

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.puzzle_mode_failed_title),
                navButton = { Back(onClick = onNavigateBack) },
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
        modifier = modifier.testTag { FailedPuzzlesScreenTags.SCREEN },
    ) { innerPadding ->
        when (uiState) {
            is FailedPuzzlesUiState.Loading -> {
                LoadingContent(modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding))
            }
            is FailedPuzzlesUiState.Failed -> {
                FailedContent(modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding))
            }
            is FailedPuzzlesUiState.BoardState -> {
                FailedPuzzlesContent(
                    uiState = uiState,
                    showBorders = showBorders,
                    highlightLegalMoves = highlightLegalMoves,
                    enableAnimations = enableAnimations,
                    interactions = interactions,
                    modifier = Modifier.background(Design.colors.primarySoft)
                        .padding(innerPadding)
                )
            }
            else -> {}
        }
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
private fun FailedPuzzlesContent(
    uiState: FailedPuzzlesUiState.BoardState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    interactions: FailedPuzzlesScreenInteractor,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Animate board transition when puzzle count changes
        AnimatedContent(
            targetState = uiState.results.size,
            transitionSpec = {
                if (enableAnimations) {
                    (slideInHorizontally { width -> width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "BoardTransition"
        ) { _ ->
            Column {
                CapturedPieces(
                    capturedPieces = data.captured[data.player.other()] ?: emptyList(),
                    side = data.player,
                    modifier = Modifier.fillMaxWidth()
                )
                ChessBoard(
                    board = data.boardData,
                    orientation = BoardOrientation.fromSide(data.player),
                    onClick = interactions::onSquareClicked,
                    showBorders = showBorders,
                    highlightLegalMoves = highlightLegalMoves,
                    enableAnimations = enableAnimations,
                    modifier = Modifier.fillMaxWidth()
                )
                CapturedPieces(
                    capturedPieces = data.captured[data.player] ?: emptyList(),
                    side = data.player.other(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ChessGymSpacer(size = SpacerSize.XXLARGE)

        // Results grid
        if (uiState.results.isNotEmpty()) {
            PuzzleResultsGrid(
                results = uiState.results,
                onFailedPuzzleClicked = interactions::onAnalyzeFailedPuzzle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Design.dimensions.spacing.xxl)
            )
        }

        // Promotion dialog
        if (uiState is FailedPuzzlesUiState.Playing && uiState.promotion != null) {
            PromotionDialog(
                side = data.player,
                onPieceChosen = interactions::onPromote
            )
        }

        // Completion dialog when finished
        if (uiState is FailedPuzzlesUiState.Finished && uiState.showCompletionDialog) {
            FailedPuzzlesCompletionDialog(
                puzzlesSolved = uiState.progress.solved,
                onDismiss = interactions::onDismissCompletion,
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
        FailedPuzzlesScreen(
            uiState = FailedPuzzlesUiState.Playing(
                data = PuzzleData(
                    rating = 1350,
                    player = Side.BLACK,
                    boardData = SampleBoardViewData.startingBoard(),
                    captured = emptyMap(),
                ),
                progress = FailedPuzzlesUiState.Progress(solved = 3, total = 10),
                results = listOf(
                    PuzzleResult(id = 1, rating = 1200, success = true),
                    PuzzleResult(id = 2, rating = 1250, success = true),
                    PuzzleResult(id = 3, rating = 1300, success = false),
                ),
                promotion = null,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FinishedPreview() {
    ChessGymTheme {
        FailedPuzzlesScreen(
            uiState = FailedPuzzlesUiState.Finished(
                data = PuzzleData(
                    rating = 1400,
                    player = Side.WHITE,
                    boardData = SampleBoardViewData.startingBoard(),
                    captured = emptyMap(),
                ),
                progress = FailedPuzzlesUiState.Progress(solved = 4, total = 5),
                results = listOf(
                    PuzzleResult(id = 1, rating = 1200, success = true),
                    PuzzleResult(id = 2, rating = 1250, success = true),
                    PuzzleResult(id = 3, rating = 1300, success = true),
                    PuzzleResult(id = 4, rating = 1320, success = true),
                    PuzzleResult(id = 5, rating = 1350, success = false),
                ),
                showCompletionDialog = false,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true
        )
    }
}
