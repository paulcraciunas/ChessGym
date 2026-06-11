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
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.boardvis.pieces.vm.KnightPathUiState
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.design.components.ChessGymChip
import com.paulcraciunas.screens.common.design.components.ChipStyle
import com.paulcraciunas.screens.common.design.components.ChipTone
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.data.BoardViewData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnightPathScreen(
    uiState: KnightPathUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onPlayClicked: () -> Unit = {},
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPlayAgain: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ChildAppBar(
                title = stringResource(R.string.boardvis_knight_path_title),
                onBack = onNavigateBack,
            ) {
                ChessGymChip(
                    text = uiState.timeRemaining,
                    tone = ChipTone.Accent,
                    style = ChipStyle.Default,
                    modifier = Modifier.padding(Design.dimensions.spacing.xxl)
                )
            }
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
                onPlayClicked = onPlayClicked,
                onSquareClicked = onSquareClicked,
                onPlayAgain = onPlayAgain,
                modifier = Modifier.fillMaxSize()
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
                    boardData = BoardViewData.default(),
                    timeRemaining = "22.4",
                    destination = Locus.e6,
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
                    boardData = BoardViewData.default(),
                    timeRemaining = "7.6",
                    score = 8,
                    isNewHighScore = true,
                    wasWrongMove = false,
                ),
            )
        }
    }
}
