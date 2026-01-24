package com.paulcraciunas.screens.puzzles.rated.ui

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
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControls
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.PuzzleData
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.rated.vm.RatedPuzzleScreenInteractor
import com.paulcraciunas.screens.puzzles.rated.vm.RatedPuzzleUiState
import com.paulcraciunas.screens.puzzles.rated.vm.StubRatedPuzzleScreenInteractor

@Composable
fun RatedPuzzleScreen(
    uiState: RatedPuzzleUiState,
    showBorders: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    interactions: RatedPuzzleScreenInteractor = StubRatedPuzzleScreenInteractor(),
) {
    val title = when (uiState) {
        is RatedPuzzleUiState.BoardState -> stringResource(R.string.rated_puzzle_title, uiState.data.rating)
        else -> stringResource(R.string.puzzle_mode_rated_title)
    }
    Scaffold(
        topBar = {
            AppBar(
                title = title,
                navButton = { Back(onClick = onNavigateBack) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (uiState) {
            is RatedPuzzleUiState.Loading -> {
                LoadingContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }
            is RatedPuzzleUiState.Failed -> {
                FailedContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }
            is RatedPuzzleUiState.BoardState -> {
                RatedPuzzleContent(
                    uiState = uiState,
                    showBorders = showBorders,
                    interactions = interactions,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun RatedPuzzleContent(
    uiState: RatedPuzzleUiState.BoardState,
    showBorders: Boolean,
    interactions: RatedPuzzleScreenInteractor,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    val isShowingSolution = uiState is RatedPuzzleUiState.Playing && uiState.isShowingSolution
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CapturedPieces(
            capturedPieces = data.captured[data.player.other()]!!,
            side = data.player,
            modifier = Modifier.fillMaxWidth()
        )
        ChessBoard(
            board = data.boardData,
            orientation = BoardOrientation.fromSide(data.player),
            onClick = if (isShowingSolution) { _ -> } else interactions::onSquareClicked,
            showBorders = showBorders,
            modifier = Modifier.fillMaxWidth()
        )
        CapturedPieces(
            capturedPieces = data.captured[data.player]!!,
            side = data.player.other(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedContent(
            targetState = uiState is RatedPuzzleUiState.Finished,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn())
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
            },
            label = "ControlsAnimation"
        ) { isFinished ->
            if (isFinished && uiState is RatedPuzzleUiState.Finished) {
                FinishedPuzzleControls(
                    success = uiState.success,
                    ratingChange = uiState.ratingChange,
                    modifier = Modifier.fillMaxWidth(),
                    onPlayNext = interactions::onNextPuzzle,
                )
            } else if (uiState is RatedPuzzleUiState.Playing) {
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
        // Dialogs for playing state
        if (uiState is RatedPuzzleUiState.Playing && !isShowingSolution) {
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
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun WhitePlayingPreview() {
    ChessGymTheme {
        RatedPuzzleScreen(
            uiState = RatedPuzzleUiState.Playing(
                data = PuzzleData(
                    rating = 1450,
                    player = Side.WHITE,
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Pawn, Piece.Knight, Piece.Pawn),
                        Side.BLACK to listOf(Piece.Bishop, Piece.Pawn, Piece.Pawn, Piece.Rook)
                    ),
                ),
                hintEnabled = true,
                showAbandonDialog = false,
                promotion = null,
            ),
            showBorders = true
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BlackPlayingPreview() {
    ChessGymTheme {
        RatedPuzzleScreen(
            uiState = RatedPuzzleUiState.Playing(
                data = PuzzleData(
                    rating = 1450,
                    player = Side.BLACK,
                    boardData = SampleBoardViewData.startingBoardComposable(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Pawn, Piece.Knight, Piece.Pawn),
                        Side.BLACK to listOf(Piece.Bishop, Piece.Pawn, Piece.Pawn, Piece.Rook)
                    ),
                ),
                hintEnabled = true,
                showAbandonDialog = false,
                promotion = null,
            ),
            showBorders = true
        )
    }
}
