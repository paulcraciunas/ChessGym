package com.paulcraciunas.screens.blindmode.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.blindmode.vm.BlindModeUiState
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControls
import com.paulcraciunas.screens.common.controls.InfiniteProgressIndicator
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationType
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlindModeScreen(
    uiState: BlindModeUiState,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit,
    onTrainingModeToggled: (enabled: Boolean) -> Unit = {},
    onSideSelected: (side: SideSelection) -> Unit = {},
    onPlayClicked: () -> Unit = {},
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onResign: () -> Unit = {},
    onReveal: () -> Unit = {},
    onPlayAgain: () -> Unit = {},
    onBackPressed: () -> Boolean = { false },
    onAbandonConfirmed: () -> Unit = {},
    onAbandonDismissed: () -> Unit = {},
) {
    BackHandler(enabled = uiState is BlindModeUiState.Playing) {
        onBackPressed()
    }

    Scaffold(
        topBar = { ChildAppBar(onBack = onDrawerToggle, title = stringResource(R.string.blind_mode_title)) },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { BlindModeScreenTags.SCREEN },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (uiState) {
                is BlindModeUiState.Setup -> SetupContent(
                    state = uiState,
                    onTrainingModeToggled = onTrainingModeToggled,
                    onSideSelected = onSideSelected,
                    onPlayClicked = onPlayClicked,
                )
                is BlindModeUiState.Playing -> PlayingContent(
                    state = uiState,
                    onSquareClicked = onSquareClicked,
                    onPromote = onPromote,
                    onResign = onResign,
                    onReveal = onReveal,
                    onAbandonConfirmed = onAbandonConfirmed,
                    onAbandonDismissed = onAbandonDismissed,
                )
                is BlindModeUiState.GameOver -> GameOverContent(
                    state = uiState,
                    onPlayAgain = onPlayAgain,
                )
            }
        }
    }
}

@Composable
private fun PlayingContent(
    state: BlindModeUiState.Playing,
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onResign: () -> Unit = {},
    onReveal: () -> Unit = {},
    onAbandonConfirmed: () -> Unit = {},
    onAbandonDismissed: () -> Unit = {},
) {
    val enableAnimations = LocalUiSettings.current.enableAnimations
    val piecesAlpha by animateFloatAsState(
        targetValue = if (state.isRevealing) 1f else 0f,
        animationSpec = if (enableAnimations) {
            tween(durationMillis = if (state.isRevealing) 300 else 500)
        } else {
            tween(durationMillis = 0)
        },
        label = "revealAlpha",
    )
    val isThinking = !state.data.interactive
    val controlsAlpha by animateFloatAsState(
        targetValue = if (isThinking) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "controlsAlpha"
    )
    ChessBoard(
        board = state.data.boardData,
        orientation = BoardOrientation.fromSide(state.data.player),
        onClick = onSquareClicked,
        piecesAlpha = piecesAlpha,
        modifier = Modifier.fillMaxWidth(),
    )
    ChessGymSpacer()
    DefaultPuzzleControls(
        hintEnabled = !isThinking && !state.isRevealing && state.isRevealAvailable,
        toMove = state.data.player,
        onHintRequested = onReveal,
        onAbandonRequested = onResign,
        abandonEnabled = !isThinking && !state.isRevealing,
        moveIndicatorTextRes = R.string.blind_mode_your_turn,
        modifier = Modifier.alpha(controlsAlpha),
    )

    if (state.moveHistory.isNotEmpty()) {
        MoveHistoryDisplay(moveHistory = state.moveHistory)
    }
    if (isThinking) {
        ThinkingIndicator()
    }
    if (state.data.promotion != null) {
        PromotionDialog(
            side = state.data.player,
            onPieceChosen = onPromote,
        )
    }
    if (state.isAbandonDialogShown) {
        AbandonConfirmationDialog(
            onConfirm = onAbandonConfirmed,
            onDismiss = onAbandonDismissed,
            type = AbandonConfirmationType.Game
        )
    }
}

@Composable
private fun ThinkingIndicator() {
    Column(
        modifier = Modifier.padding(Design.dimensions.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InfiniteProgressIndicator(
            modifier = Modifier.size(Design.dimensions.spacing.section),
        )
        ChessGymSpacer()
        Text(
            text = stringResource(R.string.blind_mode_thinking),
            style = Design.typography.bodyMedium,
            color = Design.colors.inkSoft,
        )
    }
}

@Preview("BlindMode - Setup")
@Preview("BlindMode - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BlindModeSetupPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            BlindModeScreen(
                uiState = BlindModeUiState.Setup(),
                onDrawerToggle = {},
            )
        }
    }
}

@Preview("BlindMode - Playing")
@Composable
private fun BlindModePlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            BlindModeScreen(
                uiState = BlindModeUiState.Playing(
                    isTrainingMode = true,
                    data = PreviewData().whiteGameData(),
                    moveHistory = "1. e4 e5 2. Nf3 Nc6",
                    isRevealAvailable = true,
                ),
                onDrawerToggle = {},
            )
        }
    }
}

@Preview("BlindMode - Thinking")
@Composable
private fun BlindModeThinkingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            BlindModeScreen(
                uiState = BlindModeUiState.Playing(
                    isTrainingMode = true,
                    data = PreviewData().whiteGameData().copy(interactive = false),
                    moveHistory = "1. e4 e5 2. Nf3 Nc6",
                    isRevealAvailable = true,
                ),
                onDrawerToggle = {},
            )
        }
    }
}

@Preview("BlindMode - Revealing")
@Composable
private fun BlindModeRevealingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            BlindModeScreen(
                uiState = BlindModeUiState.Playing(
                    isTrainingMode = true,
                    data = PreviewData().whiteGameData(),
                    moveHistory = "1. e4 e5 2. Nf3 Nc6",
                    isRevealAvailable = true,
                    isRevealing = true,
                ),
                onDrawerToggle = {},
            )
        }
    }
}

@Preview("BlindMode - GameOver")
@Composable
private fun BlindModeGameOverPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            BlindModeScreen(
                uiState = BlindModeUiState.GameOver(
                    isTrainingMode = true,
                    data = PreviewData().whiteGameData(),
                    moveHistory = "1. e4 e5 2. Nf3 Nc6 3. Bb5",
                ),
                onDrawerToggle = {},
            )
        }
    }
}
