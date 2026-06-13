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
import com.paulcraciunas.screens.boardvis.pieces.vm.KnightPathUiState
import com.paulcraciunas.screens.common.AnimatedControls
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard

@Composable
internal fun KnightPathScreenContents(
    state: KnightPathUiState,
    modifier: Modifier = Modifier,
    onPlayClicked: () -> Unit = {},
    onSquareClicked: (Locus) -> Unit = {},
    onPlayAgain: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        ChessBoard(
            board = state.boardData,
            orientation = BoardOrientation.fromSide(Side.WHITE),
            onClick = onSquareClicked,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedControls(targetState = state) { uiState ->
            when (uiState) {
                is KnightPathUiState.Setup -> {
                    KnightPathSetupControls(
                        onPlayClicked = onPlayClicked,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                is KnightPathUiState.Playing -> {
                    KnightPathPlayingControls(
                        currentScore = uiState.score,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                is KnightPathUiState.GameOver -> {
                    KnightPathGameOverControls(
                        finalScore = uiState.score,
                        isNewHighScore = uiState.isNewHighScore,
                        wasWrongMove = uiState.wasWrongMove,
                        onPlayAgain = onPlayAgain,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
