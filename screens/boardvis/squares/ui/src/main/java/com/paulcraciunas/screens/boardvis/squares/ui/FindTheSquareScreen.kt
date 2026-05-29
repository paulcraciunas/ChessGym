package com.paulcraciunas.screens.boardvis.squares.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareUiState
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindTheSquareScreen(
    uiState: FindTheSquareUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSideSelected: (side: SideSelection) -> Unit = {},
    onPlayClicked: () -> Unit = {},
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPlayAgain: () -> Unit = {},
    onErrorShown: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    // Handle error feedback
    if (uiState is FindTheSquareUiState.Playing && uiState.showError) {
        val enableVibrations = LocalUiSettings.current.enableVibrations
        LaunchedEffect(true) {
            if (enableVibrations) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            ChildAppBar(
                title = stringResource(R.string.boardvis_find_square_title),
                onBack = onNavigateBack,
                actions = {
                    if (uiState is FindTheSquareUiState.Playing) {
                        TimerDisplay(
                            seconds = uiState.timeRemainingSeconds,
                            modifier = Modifier.padding(Design.dimensions.spacing.xxl)
                        )
                    }
                }
            )
        },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { FindTheSquareTags.SCREEN },
    ) { innerPadding ->
        FindTheSquareScreenContents(
            state = uiState,
            onSideSelected = onSideSelected,
            onPlayClicked = onPlayClicked,
            onSquareClicked = onSquareClicked,
            onPlayAgain = onPlayAgain,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Preview("FindTheSquare - Setup")
@Preview("FindTheSquare - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FindTheSquareScreenSetupPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            FindTheSquareScreen(
                uiState = FindTheSquareUiState.Setup(
                    selectedSide = SideSelection.WHITE,
                    timeRemainingSeconds = 30
                ),
            )
        }
    }
}

@Preview("FindTheSquare - Playing")
@Composable
private fun FindTheSquareScreenPlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            FindTheSquareScreen(
                uiState = FindTheSquareUiState.Playing(
                    orientation = Side.WHITE,
                    currentSquare = Locus.e4,
                    score = 5,
                    timeRemainingSeconds = 22,
                    showError = false
                ),
            )
        }
    }
}

@Preview("FindTheSquare - GameOver")
@Composable
private fun FindTheSquareScreenGameOverPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            FindTheSquareScreen(
                uiState = FindTheSquareUiState.GameOver(
                    orientation = Side.WHITE,
                    score = 25,
                    isNewHighScore = true,
                    previousHighScore = 20,
                ),
            )
        }
    }
}
