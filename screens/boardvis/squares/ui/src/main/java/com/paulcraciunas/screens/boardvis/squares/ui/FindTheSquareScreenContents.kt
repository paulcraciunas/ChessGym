package com.paulcraciunas.screens.boardvis.squares.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareScreenInteractor
import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareUiState
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
internal fun FindTheSquareScreenContents(
    state: FindTheSquareUiState,
    showBorders: Boolean,
    enableAnimations: Boolean,
    interactions: FindTheSquareScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val orientation = BoardOrientation.fromSide(state.orientation)
            ChessBoard(
                board = state.boardData,
                orientation = orientation,
                onClick = interactions::onSquareClicked,
                showBorders = showBorders,
                enableAnimations = enableAnimations,
                modifier = Modifier.fillMaxWidth()
            )

            if (state is FindTheSquareUiState.Playing) {
                SquareNameOverlay(
                    currentSquare = state.currentSquare,
                    showError = state.showError
                )
            }
        }
        ChessGymSpacer(size = SpacerSize.XXLARGE)
        when (state) {
            is FindTheSquareUiState.Setup -> {
                FindTheSquareControls(
                    selectedSide = state.selectedSide,
                    isPlaying = false,
                    onSideSelected = interactions::onSideSelected,
                    onPlayClicked = interactions::onPlayClicked
                )
            }
            is FindTheSquareUiState.Playing -> PlayingControls(score = state.score)
            is FindTheSquareUiState.GameOver -> {
                GameSummary(
                    score = state.score,
                    isNewHighScore = state.isNewHighScore,
                    previousHighScore = state.previousHighScore,
                    onPlayAgain = interactions::onPlayAgain,
                    modifier = Modifier.padding(Design.dimensions.spacing.xxl)
                )
            }
        }
    }
}

@Composable
internal fun PlayingControls(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Design.dimensions.spacing.xgut),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg)
    ) {
        // Score display during game
        Text(
            text = stringResource(R.string.boardvis_find_square_score, score),
            style = Design.typography.titleMedium,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            color = Design.colors.primary,
        )
    }
}
