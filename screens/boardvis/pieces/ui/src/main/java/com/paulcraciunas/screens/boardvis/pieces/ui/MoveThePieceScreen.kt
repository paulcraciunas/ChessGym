package com.paulcraciunas.screens.boardvis.pieces.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceUiState
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveThePieceScreen(
    uiState: MoveThePieceUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onTrainingModeToggled: (enabled: Boolean) -> Unit = {},
    onPieceSelected: (piece: Piece) -> Unit = {},
    onPlayClicked: () -> Unit = {},
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPlayAgain: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ChildAppBar(title = stringResource(R.string.boardvis_move_piece_title), onBack = onNavigateBack) {
                if (uiState is MoveThePieceUiState.Playing) {
                    TimerDisplay(
                        seconds = uiState.timeRemainingSeconds,
                        modifier = Modifier.padding(Design.dimensions.spacing.xxl)
                    )
                }
            }
        },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { MoveThePieceTags.SCREEN },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            MoveThePieceScreenContents(
                state = uiState,
                onTrainingModeToggled = onTrainingModeToggled,
                onPieceSelected = onPieceSelected,
                onPlayClicked = onPlayClicked,
                onSquareClicked = onSquareClicked,
                onPlayAgain = onPlayAgain,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview("MoveThePiece - Setup")
@Preview("MoveThePiece - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MoveThePieceScreenSetupPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            MoveThePieceScreen(
                uiState = MoveThePieceUiState.Setup(
                    isTrainingMode = true,
                    selectedPiece = Piece.Rook,
                    timeRemainingSeconds = 60
                ),
            )
        }
    }
}

@Preview("MoveThePiece - Setup - no training")
@Preview("MoveThePiece - Setup - no training (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MoveThePieceScreenSetupNoTrainingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            MoveThePieceScreen(
                uiState = MoveThePieceUiState.Setup(
                    isTrainingMode = false,
                    selectedPiece = Piece.Rook,
                    timeRemainingSeconds = 60
                ),
            )
        }
    }
}

@Preview("MoveThePiece - Playing")
@Composable
private fun MoveThePieceScreenPlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            MoveThePieceScreen(
                uiState = MoveThePieceUiState.Playing(
                    boardData = MoveThePieceUiState.Setup().boardData,
                    playerPiece = Piece.Rook,
                    playerPieceLocus = Locus.d4,
                    movesRemaining = 2,
                    currentScore = 5,
                    timeRemainingSeconds = 45,
                    visitedSquares = setOf(Locus.d4),
                    isTrainingMode = true
                ),
            )
        }
    }
}

@Preview("MoveThePiece - GameOver")
@Composable
private fun MoveThePieceScreenGameOverPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            MoveThePieceScreen(
                uiState = MoveThePieceUiState.GameOver(
                    boardData = MoveThePieceUiState.Setup().boardData,
                    finalScore = 12,
                    isNewHighScore = true,
                    wasCaptured = false
                ),
            )
        }
    }
}

