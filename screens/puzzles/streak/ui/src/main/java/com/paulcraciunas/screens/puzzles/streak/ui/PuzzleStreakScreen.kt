package com.paulcraciunas.screens.puzzles.streak.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.model.SquareViewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakScreenInteractor
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakUiState
import com.paulcraciunas.screens.puzzles.streak.vm.StubPuzzleStreakScreenInteractor

@Composable
fun PuzzleStreakScreen(
    uiState: PuzzleStreakUiState,
    showBorders: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    interactions: PuzzleStreakScreenInteractor = StubPuzzleStreakScreenInteractor(),
) {
    if (uiState is PuzzleStreakUiState.Loading) {
        LoadingContent(modifier = Modifier.fillMaxSize())
        return
    }
    if (uiState is PuzzleStreakUiState.Failed) {
        FailedContent(modifier = Modifier.fillMaxSize())
        return
    }
    val data = (uiState as PuzzleStreakUiState.BoardState).data
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.puzzle_streak_title, data.rating),
                navButton = { Back(onClick = onNavigateBack) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                modifier = Modifier.fillMaxWidth()
            )
            CapturedPieces(
                capturedPieces = data.captured[data.player] ?: emptyList(),
                side = data.player.other(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = uiState is PuzzleStreakUiState.StreakEnded,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn())
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                },
                label = "ControlsAnimation"
            ) { isEnded ->
                if (isEnded && uiState is PuzzleStreakUiState.StreakEnded) {
                    StreakEndedControls(
                        finalStreakCount = uiState.finalStreakCount,
                        onNewStreak = interactions::onNewStreak,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else if (uiState is PuzzleStreakUiState.Playing) {
                    StreakControls(
                        streakCount = uiState.streakCount,
                        toMove = data.player,
                        onHintRequested = interactions::onHintRequested,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Dialogs
            if (uiState is PuzzleStreakUiState.Playing && uiState.promotion != null) {
                PromotionDialog(
                    side = data.player,
                    onPieceChosen = interactions::onPromote
                )
            }
            if (uiState is PuzzleStreakUiState.StreakEnded && uiState.showSummary) {
                StreakSummaryDialog(
                    streakCount = uiState.finalStreakCount,
                    isNewHighScore = uiState.isNewHighScore,
                    onDismiss = interactions::onDismissSummary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PlayingPreview() {
    ChessGymTheme {
        PuzzleStreakScreen(
            uiState = PuzzleStreakUiState.Playing(
                data = PuzzleData(
                    rating = 650,
                    player = Side.WHITE,
                    boardData = sampleBoard(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Pawn, Piece.Knight),
                        Side.BLACK to listOf(Piece.Bishop, Piece.Pawn)
                    ),
                ),
                streakCount = 12,
                promotion = null,
            ),
            showBorders = true
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StreakEndedPreview() {
    ChessGymTheme {
        PuzzleStreakScreen(
            uiState = PuzzleStreakUiState.StreakEnded(
                data = PuzzleData(
                    rating = 850,
                    player = Side.BLACK,
                    boardData = sampleBoard(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Queen),
                        Side.BLACK to listOf(Piece.Rook, Piece.Pawn, Piece.Pawn)
                    ),
                ),
                finalStreakCount = 15,
                isNewHighScore = false,
                showSummary = false,
            ),
            showBorders = true
        )
    }
}

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
