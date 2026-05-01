package com.paulcraciunas.screens.puzzles.rush.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.PuzzleResult
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushScreenInteractor
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushUiState
import com.paulcraciunas.screens.puzzles.rush.vm.StubPuzzleRushScreenInteractor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleRushScreen(
    uiState: PuzzleRushUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    interactions: PuzzleRushScreenInteractor = StubPuzzleRushScreenInteractor(),
) {
    val timeRemainingSeconds = when (uiState) {
        is PuzzleRushUiState.BoardState -> uiState.timeRemainingSeconds
        else -> 180 // Default 3 minutes
    }
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.screen_puzzle_rush),
                navButton = { Back(onClick = onNavigateBack) },
                actions = {
                    TimerDisplay(
                        seconds = timeRemainingSeconds,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        modifier = modifier.testTag { PuzzleRushScreenTags.SCREEN }
    ) { innerPadding ->
        when (uiState) {
            is PuzzleRushUiState.Loading -> {
                LoadingContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }
            is PuzzleRushUiState.Failed -> {
                FailedContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }
            is PuzzleRushUiState.BoardState -> {
                PuzzleRushContent(
                    uiState = uiState,
                    showBorders = showBorders,
                    highlightLegalMoves = highlightLegalMoves,
                    enableAnimations = enableAnimations,
                    interactions = interactions,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
private fun PuzzleRushContent(
    uiState: PuzzleRushUiState.BoardState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    interactions: PuzzleRushScreenInteractor,
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
                highlightLegalMoves = highlightLegalMoves,
                enableAnimations = enableAnimations,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Controls section with animation
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "RushControlsAnimation"
        ) { state ->
            when (state) {
                is PuzzleRushUiState.Ready -> {
                    ReadyControls(modifier = Modifier.fillMaxWidth())
                }
                is PuzzleRushUiState.Playing -> {
                    // Empty space while playing - no controls needed
                    Spacer(modifier = Modifier.height(48.dp))
                }
                is PuzzleRushUiState.Finished -> {
                    FinishedRushControls(
                        onPlayAgain = interactions::onPlayAgain,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results grid
        if (uiState.results.isNotEmpty()) {
            PuzzleResultsGrid(
                results = uiState.results,
                onFailedPuzzleClicked = interactions::onAnalyzeFailedPuzzle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Promotion dialog
        if (uiState is PuzzleRushUiState.Playing && uiState.promotion != null) {
            PromotionDialog(
                side = data.player,
                onPieceChosen = interactions::onPromote
            )
        }

        // Summary dialog when finished
        if (uiState is PuzzleRushUiState.Finished && uiState.showSummaryDialog) {
            RushSummaryDialog(
                puzzlesSolved = uiState.results.count { it.success },
                isNewHighScore = uiState.isNewHighScore,
                onDismiss = interactions::onDismissSummary,
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ReadyPreview() {
    ChessGymTheme {
        PuzzleRushScreen(
            uiState = PuzzleRushUiState.Ready(
                data = PuzzleData(
                    rating = 1200,
                    player = Side.WHITE,
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = emptyMap(),
                ),
                timeRemainingSeconds = 180,
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
private fun PlayingPreview() {
    ChessGymTheme {
        PuzzleRushScreen(
            uiState = PuzzleRushUiState.Playing(
                data = PuzzleData(
                    rating = 1350,
                    player = Side.BLACK,
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = emptyMap(),
                ),
                timeRemainingSeconds = 142,
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
        PuzzleRushScreen(
            uiState = PuzzleRushUiState.Finished(
                data = PuzzleData(
                    rating = 1400,
                    player = Side.WHITE,
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = emptyMap(),
                ),
                timeRemainingSeconds = 0,
                results = listOf(
                    PuzzleResult(id = 1, rating = 1200, success = true),
                    PuzzleResult(id = 2, rating = 1250, success = true),
                    PuzzleResult(id = 3, rating = 1300, success = true),
                    PuzzleResult(id = 4, rating = 1320, success = true),
                    PuzzleResult(id = 5, rating = 1350, success = false),
                ),
                showSummaryDialog = false,
                isNewHighScore = false,
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
private fun ManyResultsPreview() {
    ChessGymTheme {
        PuzzleRushScreen(
            uiState = PuzzleRushUiState.Playing(
                data = PuzzleData(
                    rating = 1500,
                    player = Side.WHITE,
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = emptyMap(),
                ),
                timeRemainingSeconds = 45,
                results = (1..12).map { i ->
                    PuzzleResult(id = i, rating = 1200 + i * 20, success = i % 3 != 0)
                },
                promotion = null,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true
        )
    }
}
