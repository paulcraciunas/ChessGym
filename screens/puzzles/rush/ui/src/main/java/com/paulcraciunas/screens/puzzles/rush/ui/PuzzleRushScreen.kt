package com.paulcraciunas.screens.puzzles.rush.ui

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
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.SquareViewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushScreenInteractor
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushUiState
import com.paulcraciunas.screens.puzzles.rush.vm.StubPuzzleRushScreenInteractor

@Composable
fun PuzzleRushScreen(
    uiState: PuzzleRushUiState,
    showBorders: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    interactions: PuzzleRushScreenInteractor = StubPuzzleRushScreenInteractor(),
) {
    if (uiState is PuzzleRushUiState.Loading) {
        LoadingContent(modifier = Modifier.fillMaxSize())
        return
    }
    if (uiState is PuzzleRushUiState.Failed) {
        FailedContent(modifier = Modifier.fillMaxSize())
        return
    }
    val boardState = uiState as PuzzleRushUiState.BoardState
    val data = boardState.data

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.screen_puzzle_rush),
                navButton = { Back(onClick = onNavigateBack) },
                actions = {
                    CountdownTimer(
                        timeRemainingSeconds = boardState.timeRemainingSeconds,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animate board transition when puzzle count changes
            AnimatedContent(
                targetState = boardState.results.size,
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
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results grid
            if (boardState.results.isNotEmpty()) {
                PuzzleResultsGrid(
                    results = boardState.results,
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
                    puzzlesSolved = boardState.results.count { it.success },
                    onDismiss = interactions::onDismissSummary
                )
            }
        }
    }
}

@Composable
private fun CountdownTimer(
    timeRemainingSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timeText = "%d:%02d".format(minutes, seconds)

    Text(
        text = timeText,
        style = MaterialTheme.typography.titleLarge,
        color = if (timeRemainingSeconds <= 30) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
        modifier = modifier
    )
}

// Preview helpers
private fun sampleBoard(): BoardViewData {
    val squares: Array<Array<SquareViewData>> = Array(Rank.entries.size) {
        Array(File.entries.size) { SquareViewData(piece = null) }
    }
    squares.addWhitePieces()
    squares.addBlackPieces()
    return BoardViewData(squares)
}

private fun Array<Array<SquareViewData>>.addWhitePieces() = apply {
    File.entries.forEach { file ->
        this[Rank.`2`.dec()][file.dec()] = SquareViewData.simple(piece = Piece.Pawn, side = Side.WHITE)
    }
    addStartingPieces(Side.WHITE, Rank.`1`)
}

private fun Array<Array<SquareViewData>>.addBlackPieces() = apply {
    File.entries.forEach { file ->
        this[Rank.`7`.dec()][file.dec()] = SquareViewData.simple(piece = Piece.Pawn, side = Side.BLACK)
    }
    addStartingPieces(Side.BLACK, Rank.`8`)
}

private fun Array<Array<SquareViewData>>.addStartingPieces(side: Side, rank: Rank) {
    this[rank.dec()][File.a.dec()] = SquareViewData.simple(piece = Piece.Rook, side = side)
    this[rank.dec()][File.b.dec()] = SquareViewData.simple(piece = Piece.Knight, side = side)
    this[rank.dec()][File.c.dec()] = SquareViewData.simple(piece = Piece.Bishop, side = side)
    this[rank.dec()][File.d.dec()] = SquareViewData.simple(piece = Piece.Queen, side = side)
    this[rank.dec()][File.e.dec()] = SquareViewData.simple(piece = Piece.King, side = side)
    this[rank.dec()][File.f.dec()] = SquareViewData.simple(piece = Piece.Bishop, side = side)
    this[rank.dec()][File.g.dec()] = SquareViewData.simple(piece = Piece.Knight, side = side)
    this[rank.dec()][File.h.dec()] = SquareViewData.simple(piece = Piece.Rook, side = side)
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ReadyPreview() {
    ChessGymTheme {
        PuzzleRushScreen(
            uiState = PuzzleRushUiState.Ready(
                data = PuzzleRushUiState.PuzzleData(
                    rating = 1200,
                    player = Side.WHITE,
                    boardData = sampleBoard(),
                ),
                timeRemainingSeconds = 180,
                results = emptyList(),
            ),
            showBorders = true
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
                data = PuzzleRushUiState.PuzzleData(
                    rating = 1350,
                    player = Side.BLACK,
                    boardData = sampleBoard(),
                ),
                timeRemainingSeconds = 142,
                results = listOf(
                    PuzzleRushUiState.PuzzleResult(1200, true),
                    PuzzleRushUiState.PuzzleResult(1250, true),
                    PuzzleRushUiState.PuzzleResult(1300, false),
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
        PuzzleRushScreen(
            uiState = PuzzleRushUiState.Finished(
                data = PuzzleRushUiState.PuzzleData(
                    rating = 1400,
                    player = Side.WHITE,
                    boardData = sampleBoard(),
                ),
                timeRemainingSeconds = 0,
                results = listOf(
                    PuzzleRushUiState.PuzzleResult(1200, true),
                    PuzzleRushUiState.PuzzleResult(1250, true),
                    PuzzleRushUiState.PuzzleResult(1300, true),
                    PuzzleRushUiState.PuzzleResult(1320, true),
                    PuzzleRushUiState.PuzzleResult(1350, false),
                ),
                showSummaryDialog = false,
            ),
            showBorders = true
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
                data = PuzzleRushUiState.PuzzleData(
                    rating = 1500,
                    player = Side.WHITE,
                    boardData = sampleBoard(),
                ),
                timeRemainingSeconds = 45,
                results = (1..12).map { i ->
                    PuzzleRushUiState.PuzzleResult(1200 + i * 20, i % 3 != 0)
                },
                promotion = null,
            ),
            showBorders = true
        )
    }
}
