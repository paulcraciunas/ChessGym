package com.paulcraciunas.screens.boardvis.pieces.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceScreenInteractor
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard

@Composable
internal fun MoveThePieceScreenContents(
    state: MoveThePieceUiState,
    showBorders: Boolean,
    interactions: MoveThePieceScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // Always show white's perspective since player is always white
            ChessBoard(
                board = state.boardData,
                orientation = BoardOrientation.fromSide(Side.WHITE),
                onClick = interactions::onSquareClicked,
                showBorders = showBorders,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is MoveThePieceUiState.Setup -> {
                MoveThePieceSetupControls(
                    isTrainingMode = state.isTrainingMode,
                    selectedPiece = state.selectedPiece,
                    onTrainingModeToggled = interactions::onTrainingModeToggled,
                    onPieceSelected = interactions::onPieceSelected,
                    onPlayClicked = interactions::onPlayClicked,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            is MoveThePieceUiState.Playing -> {
                MoveThePiecePlayingControls(
                    movesRemaining = state.movesRemaining,
                    currentScore = state.currentScore,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            is MoveThePieceUiState.GameOver -> {
                MoveThePieceGameOverControls(
                    finalScore = state.finalScore,
                    isNewHighScore = state.isNewHighScore,
                    wasCaptured = state.wasCaptured,
                    onPlayAgain = interactions::onPlayAgain,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
