package com.paulcraciunas.screens.boardvis.pieces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceUiState
import com.paulcraciunas.screens.common.board.v2.BoardOrientation2
import com.paulcraciunas.screens.common.board.v2.ChessBoard2

@Composable
internal fun MoveThePieceScreenContents(
    state: MoveThePieceUiState,
    modifier: Modifier = Modifier,
    onTrainingModeToggled: (enabled: Boolean) -> Unit = {},
    onPieceSelected: (piece: Piece) -> Unit = {},
    onPlayClicked: () -> Unit = {},
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPlayAgain: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Always show white's perspective since player is always white
        ChessBoard2(
            board = state.boardData,
            orientation = BoardOrientation2.fromSide(Side.WHITE),
            onClick = onSquareClicked,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (state) {
            is MoveThePieceUiState.Setup -> {
                MoveThePieceSetupControls(
                    isTrainingMode = state.isTrainingMode,
                    selectedPiece = state.selectedPiece,
                    onTrainingModeToggled = onTrainingModeToggled,
                    onPieceSelected = onPieceSelected,
                    onPlayClicked = onPlayClicked,
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
                    onPlayAgain = onPlayAgain,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
