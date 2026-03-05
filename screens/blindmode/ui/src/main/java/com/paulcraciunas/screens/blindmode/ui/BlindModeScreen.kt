package com.paulcraciunas.screens.blindmode.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeScreenInteractor
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.blindmode.vm.StubBlindModeScreenInteractor
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControls
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationType
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
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
    BackHandler(enabled = uiState is BlindModeUiState.Playing) {
        interactions.onBackPressed()
    }

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
        orientation = if (state.playerSide == Side.BLACK) BoardOrientation.Black
        else BoardOrientation.White,
        onClick = interactions::onSquareClicked,
        showBorders = showBorders,
        highlightLegalMoves = highlightLegalMoves,
        enableAnimations = false,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (!state.isThinking) {
        DefaultPuzzleControls(
            hintEnabled = state.isRevealAvailable,
            toMove = state.playerSide,
            onHintRequested = interactions::onReveal,
            onAbandonRequested = interactions::onResign,
            abandonEnabled = true,
            moveIndicatorTextRes = R.string.blind_mode_your_turn,
        )
    }

    if (state.moveHistory.isNotEmpty()) {
        MoveHistoryDisplay(moveHistory = state.moveHistory)
    }

    if (state.isThinking) {
        ThinkingIndicator()
    }

    if (state.pendingPromotion != null) {
        PromotionDialog(
            side = state.playerSide,
            onPieceChosen = interactions::onPromote,
        )
    }

    if (state.isAbandonDialogShown) {
        AbandonConfirmationDialog(
            onConfirm = interactions::onAbandonConfirmed,
            onDismiss = interactions::onAbandonDismissed,
            type = AbandonConfirmationType.Game
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

@Composable
private fun ThinkingIndicator() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.blind_mode_thinking),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
                playerSide = Side.WHITE,
                isRevealAvailable = true,
                isThinking = false,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            onDrawerToggle = {},
            interactions = StubBlindModeScreenInteractor(),
        )
    }
}

@Preview("BlindMode - Thinking")
@Composable
private fun BlindModeThinkingPreview() {
    ChessGymTheme {
        BlindModeScreen(
            uiState = BlindModeUiState.Playing(
                moveHistory = "1. e4 e5 2. Nf3 Nc6",
                playerSide = Side.WHITE,
                isRevealAvailable = true,
                isThinking = true,
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
