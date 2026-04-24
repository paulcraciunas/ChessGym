package com.paulcraciunas.screens.puzzles.streak.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControls
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakScreenInteractor
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakUiState
import com.paulcraciunas.screens.puzzles.streak.vm.StubPuzzleStreakScreenInteractor

@Composable
fun PuzzleStreakScreen(
    uiState: PuzzleStreakUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    interactions: PuzzleStreakScreenInteractor = StubPuzzleStreakScreenInteractor(),
) {
    val streakCount = when (uiState) {
        is PuzzleStreakUiState.Playing -> uiState.streakCount
        is PuzzleStreakUiState.StreakEnded -> 0
        else -> 0
    }
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.puzzle_mode_streak_title),
                navButton = { Back(onClick = onNavigateBack) },
                actions = { if (streakCount > 0) StreakCounter(count = streakCount) }
            )
        },
        modifier = modifier.testTag { PuzzleStreakScreenTags.SCREEN }
    ) { innerPadding ->
        when (uiState) {
            is PuzzleStreakUiState.Loading -> {
                LoadingContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }
            is PuzzleStreakUiState.Failed -> {
                FailedContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }
            is PuzzleStreakUiState.BoardState -> {
                PuzzleStreakContent(
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

@Composable
private fun PuzzleStreakContent(
    uiState: PuzzleStreakUiState.BoardState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    interactions: PuzzleStreakScreenInteractor,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    val isShowingSolution = uiState is PuzzleStreakUiState.Playing && uiState.isShowingSolution
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CapturedPieces(
            capturedPieces = data.captured[data.player.other()] ?: emptyList(),
            side = data.player,
            modifier = Modifier.fillMaxWidth()
        )
        ChessBoard(
            board = data.boardData,
            orientation = BoardOrientation.fromSide(data.player),
            onClick = if (isShowingSolution) { _ -> } else interactions::onSquareClicked,
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
                DefaultPuzzleControls(
                    hintEnabled = uiState.hintEnabled && !isShowingSolution,
                    toMove = data.player,
                    onHintRequested = interactions::onHintRequested,
                    onAbandonRequested = interactions::onAbandon,
                    modifier = Modifier.fillMaxWidth(),
                    abandonEnabled = !isShowingSolution,
                )
            }
        }

        // Dialogs
        if (uiState is PuzzleStreakUiState.Playing && !isShowingSolution) {
            if (uiState.showAbandonDialog) {
                AbandonConfirmationDialog(
                    onConfirm = interactions::onAbandonConfirmed,
                    onDismiss = interactions::onAbandonDismissed
                )
            }
            if (uiState.promotion != null) {
                PromotionDialog(
                    side = data.player,
                    onPieceChosen = interactions::onPromote
                )
            }
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

@Composable
private fun StreakCounter(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.puzzle_rush_icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
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
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Pawn, Piece.Knight),
                        Side.BLACK to listOf(Piece.Bishop, Piece.Pawn)
                    ),
                ),
                streakCount = 12,
                hintEnabled = true,
                showAbandonDialog = false,
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
private fun StreakEndedPreview() {
    ChessGymTheme {
        PuzzleStreakScreen(
            uiState = PuzzleStreakUiState.StreakEnded(
                data = PuzzleData(
                    rating = 850,
                    player = Side.BLACK,
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Queen),
                        Side.BLACK to listOf(Piece.Rook, Piece.Pawn, Piece.Pawn)
                    ),
                ),
                finalStreakCount = 15,
                isNewHighScore = false,
                showSummary = false,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true
        )
    }
}
