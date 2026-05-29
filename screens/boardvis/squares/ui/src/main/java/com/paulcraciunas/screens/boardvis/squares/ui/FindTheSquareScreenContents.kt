package com.paulcraciunas.screens.boardvis.squares.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareUiState
import com.paulcraciunas.screens.common.board.v2.BoardOrientation2
import com.paulcraciunas.screens.common.board.v2.ChessBoard2
import com.paulcraciunas.screens.common.controls.SideSelection
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.components.Title
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun FindTheSquareScreenContents(
    state: FindTheSquareUiState,
    onSideSelected: (side: SideSelection) -> Unit,
    onPlayClicked: () -> Unit,
    onSquareClicked: (locus: Locus) -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        ChessBoard2(
            board = state.boardData,
            orientation = BoardOrientation2.fromSide(state.orientation),
            onClick = onSquareClicked,
        ) {
            if (state is FindTheSquareUiState.Playing) {
                SquareNameOverlay(
                    currentSquare = state.currentSquare,
                    showError = state.showError,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        ChessGymSpacer(size = SpacerSize.XXLARGE)
        when (state) {
            is FindTheSquareUiState.Setup -> {
                FindTheSquareControls(
                    selectedSide = state.selectedSide,
                    isPlaying = false,
                    onSideSelected = onSideSelected,
                    onPlayClicked = onPlayClicked
                )
            }
            is FindTheSquareUiState.Playing -> PlayingControls(score = state.score)
            is FindTheSquareUiState.GameOver -> {
                GameSummary(
                    score = state.score,
                    isNewHighScore = state.isNewHighScore,
                    previousHighScore = state.previousHighScore,
                    onPlayAgain = onPlayAgain,
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
        modifier = modifier
            .fillMaxWidth()
            .padding(Design.dimensions.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Title(
            text = stringResource(R.string.generic_current_score),
        )
        Text(
            text = "$score",
            style = Design.textStyles.displayNumericLarge,
            color = Design.colors.primary,
        )
    }
}

@Preview("PlayingControls", showBackground = true)
@Preview("GameSummary - New High Score (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PlayingControlsPreview() {
    ChessGymTheme {
        PlayingControls(
            score = 9,
            modifier = Modifier.padding(16.dp)
        )
    }
}
