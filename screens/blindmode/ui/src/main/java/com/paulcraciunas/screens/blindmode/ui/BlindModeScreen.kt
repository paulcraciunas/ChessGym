package com.paulcraciunas.screens.blindmode.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeScreenInteractor
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.blindmode.vm.StubBlindModeScreenInteractor
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun BlindModeScreen(
    uiState: BlindModeUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    onDrawerToggle: () -> Unit,
    interactions: BlindModeScreenInteractor,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.blind_mode_title),
                navButton = { Home(onClick = onDrawerToggle) },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (uiState) {
                is BlindModeUiState.Setup -> SetupContent(
                    state = uiState,
                    showBorders = showBorders,
                    interactions = interactions,
                )
                is BlindModeUiState.Playing -> PlayingContent(
                    state = uiState,
                    showBorders = showBorders,
                    highlightLegalMoves = highlightLegalMoves,
                    interactions = interactions,
                )
                is BlindModeUiState.Revealing -> RevealingContent(
                    state = uiState,
                    showBorders = showBorders,
                )
                is BlindModeUiState.GameOver -> GameOverContent(
                    state = uiState,
                    showBorders = showBorders,
                    interactions = interactions,
                )
            }
        }
    }
}

@Composable
private fun PlayingContent(
    state: BlindModeUiState.Playing,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    interactions: BlindModeScreenInteractor,
) {
    ChessBoard(
        board = SampleBoardViewData.emptyBoardWithMoves(
            selectedSquare = state.selectedSquare,
            legalMoves = state.legalMoves
        ),
        orientation = BoardOrientation.White,
        onClick = interactions::onSquareClicked,
        showBorders = showBorders,
        highlightLegalMoves = highlightLegalMoves,
        enableAnimations = false,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    PlayingControls(
        isRevealAvailable = state.isRevealAvailable,
        isThinking = state.isThinking,
        onResign = interactions::onResign,
        onReveal = interactions::onReveal,
    )

    if (state.moveHistory.isNotEmpty()) {
        MoveHistoryDisplay(moveHistory = state.moveHistory)
    }

    if (state.isThinking) {
        Text(
            text = stringResource(R.string.blind_mode_thinking),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
private fun RevealingContent(
    state: BlindModeUiState.Revealing,
    showBorders: Boolean,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 500)),
    ) {
        ChessBoard(
            board = state.boardData,
            orientation = BoardOrientation.White,
            onClick = {},
            showBorders = showBorders,
            enableAnimations = false,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (state.moveHistory.isNotEmpty()) {
        MoveHistoryDisplay(moveHistory = state.moveHistory)
    }
}

@Preview("BlindMode - Setup")
@Preview("BlindMode - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BlindModeSetupPreview() {
    ChessGymTheme {
        BlindModeScreen(
            uiState = BlindModeUiState.Setup(),
            showBorders = true,
            highlightLegalMoves = true,
            onDrawerToggle = {},
            interactions = StubBlindModeScreenInteractor(),
        )
    }
}

@Preview("BlindMode - Playing")
@Composable
private fun BlindModePlayingPreview() {
    ChessGymTheme {
        BlindModeScreen(
            uiState = BlindModeUiState.Playing(
                moveHistory = "1. e4 e5 2. Nf3 Nc6",
                isRevealAvailable = true,
                isThinking = true
            ),
            showBorders = true,
            highlightLegalMoves = true,
            onDrawerToggle = {},
            interactions = StubBlindModeScreenInteractor(),
        )
    }
}

@Preview("BlindMode - GameOver")
@Composable
private fun BlindModeGameOverPreview() {
    ChessGymTheme {
        BlindModeScreen(
            uiState = BlindModeUiState.GameOver(
                boardData = SampleBoardViewData.emptyBoard(),
                moveHistory = "1. e4 e5 2. Nf3 Nc6 3. Bb5",
                result = BlindModeUiState.GameResult.Win,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            onDrawerToggle = {},
            interactions = StubBlindModeScreenInteractor(),
        )
    }
}
