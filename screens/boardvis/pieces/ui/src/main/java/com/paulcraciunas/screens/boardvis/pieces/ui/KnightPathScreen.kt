package com.paulcraciunas.screens.boardvis.pieces.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
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
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.pieces.vm.KnightPathUiState
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.controls.TimerDisplay
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.RemainingTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnightPathScreen(
    uiState: KnightPathUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onPlayClicked: () -> Unit = {},
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPlayAgain: () -> Unit = {},
    onAbandonConfirmed: () -> Unit = {},
    onAbandonDismissed: () -> Unit = {},
) {
    BackHandler(enabled = uiState is KnightPathUiState.Playing) {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            ChildAppBar(
                title = stringResource(R.string.boardvis_knight_path_title),
                onBack = onNavigateBack,
            ) { TimerDisplay(remainingTime = uiState.timeRemaining) }
        },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { KnightPathTags.SCREEN },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            KnightPathScreenContents(
                state = uiState,
                modifier = Modifier.fillMaxSize(),
                onPlayClicked = onPlayClicked,
                onSquareClicked = onSquareClicked,
                onPlayAgain = onPlayAgain,
                onAbandonConfirmed = onAbandonConfirmed,
                onAbandonDismissed = onAbandonDismissed,
            )
        }
    }
}

@Preview("KnightPath - Setup")
@Preview("KnightPath - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun KnightPathScreenSetupPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            KnightPathScreen(uiState = KnightPathUiState.Setup)
        }
    }
}

@Preview("KnightPath - Playing")
@Composable
private fun KnightPathScreenPlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            KnightPathScreen(
                uiState = KnightPathUiState.Playing(
                    boardState = BoardState.empty.copy(boardData = BoardViewData.singleKnight()),
                    timeRemaining = RemainingTime(value = "22.4", danger = false),
                    showAbandonDialog = false,
                    score = 3,
                ),
            )
        }
    }
}

@Preview("KnightPath - GameOver")
@Composable
private fun KnightPathScreenGameOverPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            KnightPathScreen(
                uiState = KnightPathUiState.GameOver(
                    boardState = BoardState.empty.copy(boardData = BoardViewData.singleKnight()),
                    timeRemaining = RemainingTime(value = "4.6", danger = true),
                    score = 8,
                    isNewHighScore = true,
                    previousHighScore = 7,
                    wasWrongMove = false,
                ),
            )
        }
    }
}
