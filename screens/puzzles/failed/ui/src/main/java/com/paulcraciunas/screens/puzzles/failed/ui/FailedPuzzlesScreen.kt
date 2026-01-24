package com.paulcraciunas.screens.puzzles.failed.ui

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.PuzzleResultsGrid
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesScreenInteractor
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesUiState
import com.paulcraciunas.screens.puzzles.failed.vm.StubFailedPuzzlesScreenInteractor

@Composable
fun FailedPuzzlesScreen(
    uiState: FailedPuzzlesUiState,
    showBorders: Boolean,
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
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        modifier = modifier
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
                    interactions = interactions,
                    modifier = Modifier.padding(innerPadding)
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
                (slideInHorizontally { width -> width } + fadeIn())
                    .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
            },
            label = "BoardTransition"
        ) { _ ->
            ChessBoard(
                board = data.boardData,
                orientation = BoardOrientation.fromSide(data.player),
                onClick = interactions::onSquareClicked,
                showBorders = showBorders,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results grid
        if (uiState.results.isNotEmpty()) {
            PuzzleResultsGrid(
                results = uiState.results,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
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
                    boardData = SampleBoardViewData.startingBoardComposable(),
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
            showBorders = true
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
                    boardData = SampleBoardViewData.startingBoardComposable(),
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
            showBorders = true
        )
    }
}
