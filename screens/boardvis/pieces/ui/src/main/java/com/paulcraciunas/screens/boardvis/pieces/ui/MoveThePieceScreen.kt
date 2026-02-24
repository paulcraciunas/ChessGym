package com.paulcraciunas.screens.boardvis.pieces.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceScreenInteractor
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceUiState
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun MoveThePieceScreen(
    uiState: MoveThePieceUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    onNavigateBack: () -> Unit,
    interactions: MoveThePieceScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.boardvis_move_piece_title),
                navButton = { Back(onClick = onNavigateBack) },
            ) {
                if (uiState is MoveThePieceUiState.Playing) {
                    TimerDisplay(
                        seconds = uiState.timeRemainingSeconds,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            MoveThePieceScreenContents(
                state = uiState,
                showBorders = showBorders,
                highlightLegalMoves = highlightLegalMoves,
                enableAnimations = enableAnimations,
                interactions = interactions,
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
        MoveThePieceScreen(
            uiState = MoveThePieceUiState.Setup(
                isTrainingMode = true,
                selectedPiece = Piece.Rook,
                timeRemainingSeconds = 60
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            onNavigateBack = {},
            interactions = PreviewInteractions
        )
    }
}

@Preview("MoveThePiece - Setup - no training")
@Preview("MoveThePiece - Setup - no training (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MoveThePieceScreenSetupNoTrainingPreview() {
    ChessGymTheme {
        MoveThePieceScreen(
            uiState = MoveThePieceUiState.Setup(
                isTrainingMode = false,
                selectedPiece = Piece.Rook,
                timeRemainingSeconds = 60
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            onNavigateBack = {},
            interactions = PreviewInteractions
        )
    }
}

@Preview("MoveThePiece - Playing")
@Composable
private fun MoveThePieceScreenPlayingPreview() {
    ChessGymTheme {
        MoveThePieceScreen(
            uiState = MoveThePieceUiState.Playing(
                boardData = MoveThePieceUiState.Setup().boardData,
                playerPiece = Piece.Rook,
                playerPieceLocus = "d4".loc(),
                movesRemaining = 2,
                currentScore = 5,
                timeRemainingSeconds = 45,
                visitedSquares = setOf("d4".loc()),
                isTrainingMode = true
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            onNavigateBack = {},
            interactions = PreviewInteractions
        )
    }
}

@Preview("MoveThePiece - GameOver")
@Composable
private fun MoveThePieceScreenGameOverPreview() {
    ChessGymTheme {
        MoveThePieceScreen(
            uiState = MoveThePieceUiState.GameOver(
                boardData = MoveThePieceUiState.Setup().boardData,
                finalScore = 12,
                isNewHighScore = true,
                wasCaptured = false
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            onNavigateBack = {},
            interactions = PreviewInteractions
        )
    }
}

private object PreviewInteractions : MoveThePieceScreenInteractor {
    override fun onTrainingModeToggled(enabled: Boolean) {}
    override fun onPieceSelected(piece: Piece) {}
    override fun onPlayClicked() {}
    override fun onSquareClicked(locus: Locus) {}
    override fun onPlayAgain() {}
}
