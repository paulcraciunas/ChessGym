package com.paulcraciunas.screens.boardvis.squares.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareScreenInteractor
import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareUiState
import com.paulcraciunas.screens.boardvis.squares.vm.SideSelection
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun FindTheSquareScreen(
    uiState: FindTheSquareUiState,
    showBorders: Boolean,
    enableVibrations: Boolean,
    enableAnimations: Boolean,
    onNavigateBack: () -> Unit,
    interactions: FindTheSquareScreenInteractor,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    // Handle error feedback
    if (uiState is FindTheSquareUiState.Playing && uiState.showError) {
        LaunchedEffect(true) {
            if (enableVibrations) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            interactions.onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.boardvis_find_square_title),
                navButton = { Back(onClick = onNavigateBack) },
            ) {
                if (uiState is FindTheSquareUiState.Playing) {
                    TimerDisplay(
                        seconds = uiState.timeRemainingSeconds,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        FindTheSquareScreenContents(
            state = uiState,
            showBorders = showBorders,
            enableAnimations = enableAnimations,
            interactions = interactions,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        )
    }
}

@Preview("FindTheSquare - Setup")
@Preview("FindTheSquare - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FindTheSquareScreenSetupPreview() {
    ChessGymTheme {
        FindTheSquareScreen(
            uiState = FindTheSquareUiState.Setup(
                selectedSide = SideSelection.WHITE,
                timeRemainingSeconds = 30
            ),
            showBorders = true,
            enableVibrations = true,
            enableAnimations = true,
            onNavigateBack = {},
            interactions = PreviewInteractions
        )
    }
}

@Preview("FindTheSquare - Playing")
@Composable
private fun FindTheSquareScreenPlayingPreview() {
    ChessGymTheme {
        FindTheSquareScreen(
            uiState = FindTheSquareUiState.Playing(
                orientation = Side.WHITE,
                currentSquare = Locus(File.e, Rank.`4`),
                score = 5,
                timeRemainingSeconds = 22,
                showError = false
            ),
            showBorders = true,
            enableVibrations = true,
            enableAnimations = true,
            onNavigateBack = {},
            interactions = PreviewInteractions
        )
    }
}

@Preview("FindTheSquare - GameOver")
@Composable
private fun FindTheSquareScreenGameOverPreview() {
    ChessGymTheme {
        FindTheSquareScreen(
            uiState = FindTheSquareUiState.GameOver(
                orientation = Side.WHITE,
                score = 25,
                isNewHighScore = true,
                previousHighScore = 20,
            ),
            showBorders = true,
            enableVibrations = true,
            enableAnimations = true,
            onNavigateBack = {},
            interactions = PreviewInteractions
        )
    }
}

private object PreviewInteractions : FindTheSquareScreenInteractor {
    override fun onSideSelected(side: SideSelection) {}
    override fun onPlayClicked() {}
    override fun onSquareClicked(locus: Locus) {}
    override fun onPlayAgain() {}
    override fun onErrorShown() {}
}
