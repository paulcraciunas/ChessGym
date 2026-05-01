package com.paulcraciunas.screens.blindmode.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.paulcraciunas.screens.common.controls.InfiniteProgressIndicator
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationType
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlindModeScreen(
    uiState: BlindModeUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
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
        modifier = modifier.testTag { BlindModeScreenTags.SCREEN },
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
                    enableAnimations = enableAnimations,
                    interactions = interactions,
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
    enableAnimations: Boolean,
    interactions: BlindModeScreenInteractor,
) {
    val displayBoard = remember(state.boardData, state.selectedSquare, state.legalMoves) {
        state.boardData.withMoveIndicators(state.selectedSquare, state.legalMoves)
    }

    val piecesAlpha by animateFloatAsState(
        targetValue = if (state.isRevealing) 1f else 0f,
        animationSpec = if (enableAnimations) {
            tween(durationMillis = if (state.isRevealing) 300 else 500)
        } else {
            tween(durationMillis = 0)
        },
        label = "revealAlpha",
    )

    ChessBoard(
        board = displayBoard,
        orientation = BoardOrientation.fromSide(state.playerSide),
        onClick = interactions::onSquareClicked,
        showBorders = showBorders,
        highlightLegalMoves = highlightLegalMoves,
        enableAnimations = enableAnimations,
        piecesAlpha = piecesAlpha,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(8.dp))

    DefaultPuzzleControls(
        hintEnabled = !state.isThinking && !state.isRevealing && state.isRevealAvailable,
        toMove = state.playerSide,
        onHintRequested = interactions::onReveal,
        onAbandonRequested = interactions::onResign,
        abandonEnabled = !state.isThinking && !state.isRevealing,
        moveIndicatorTextRes = R.string.blind_mode_your_turn,
    )

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
private fun ThinkingIndicator() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InfiniteProgressIndicator(
            modifier = Modifier.size(32.dp),
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
            enableAnimations = false,
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
            enableAnimations = false,
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
            enableAnimations = false,
            onDrawerToggle = {},
            interactions = StubBlindModeScreenInteractor(),
        )
    }
}

@Preview("BlindMode - Revealing")
@Composable
private fun BlindModeRevealingPreview() {
    ChessGymTheme {
        BlindModeScreen(
            uiState = BlindModeUiState.Playing(
                boardData = SampleBoardViewData.startingBoard(),
                moveHistory = "1. e4 e5 2. Nf3 Nc6",
                playerSide = Side.WHITE,
                isRevealAvailable = true,
                isThinking = false,
                isRevealing = true,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = false,
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
            enableAnimations = false,
            onDrawerToggle = {},
            interactions = StubBlindModeScreenInteractor(),
        )
    }
}
