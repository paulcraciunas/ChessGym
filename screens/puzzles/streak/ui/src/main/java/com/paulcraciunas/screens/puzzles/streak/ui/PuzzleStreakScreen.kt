package com.paulcraciunas.screens.puzzles.streak.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AnimatedBoard
import com.paulcraciunas.screens.common.AnimatedControls
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControls
import com.paulcraciunas.screens.common.design.components.PlayButton
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleStreakScreen(
    uiState: PuzzleStreakUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSquareClicked: (selection: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onHintRequested: () -> Unit = {},
    onAbandon: () -> Unit = {},
    onAbandonConfirmed: () -> Unit = {},
    onAbandonDismissed: () -> Unit = {},
    onNewStreak: () -> Unit = {},
    onNextPuzzle: () -> Unit = {},
    onAutoNext: (Boolean) -> Unit = {},
    onDismissSummary: () -> Unit = {},
) {
    BackHandler(enabled = uiState is PuzzleStreakUiState.Playing) {
        onNavigateBack()
    }

    val streakCount = when (uiState) {
        is PuzzleStreakUiState.Playing -> uiState.streakCount
        is PuzzleStreakUiState.StreakEnded -> 0
        else -> 0
    }
    val countAlpha by animateFloatAsState(
        targetValue = if (streakCount > 0) 1f else 0f,
        animationSpec = tween(300),
        label = "revealStreak",
    )
    Scaffold(
        topBar = {
            ChildAppBar(
                title = stringResource(R.string.puzzle_mode_streak_title),
                onBack = onNavigateBack,
                actions = {
                    StreakCounter(
                        count = streakCount,
                        modifier = Modifier
                            .alpha(countAlpha)
                            .padding(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.sm)
                    )
                })
        },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { PuzzleStreakScreenTags.SCREEN },
    ) { innerPadding ->
        val defaultModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            is PuzzleStreakUiState.Loading -> LoadingContent(modifier = defaultModifier)
            is PuzzleStreakUiState.Failed -> FailedContent(modifier = defaultModifier)
            is PuzzleStreakUiState.WithBoard -> PuzzleStreakContent(
                uiState = uiState,
                onSquareClicked = onSquareClicked,
                onPromote = onPromote,
                onHintRequested = onHintRequested,
                onAbandon = onAbandon,
                onAbandonConfirmed = onAbandonConfirmed,
                onAbandonDismissed = onAbandonDismissed,
                onNewStreak = onNewStreak,
                onNextPuzzle = onNextPuzzle,
                onAutoNext = onAutoNext,
                onDismissSummary = onDismissSummary,
                modifier = defaultModifier,
            )
        }
    }
}

@Composable
private fun PuzzleStreakContent(
    uiState: PuzzleStreakUiState.WithBoard,
    onSquareClicked: (selection: Locus) -> Unit,
    onPromote: (to: Piece) -> Unit,
    onHintRequested: () -> Unit,
    onAbandon: () -> Unit,
    onAbandonConfirmed: () -> Unit,
    onAbandonDismissed: () -> Unit,
    onNewStreak: () -> Unit,
    onNextPuzzle: () -> Unit,
    onAutoNext: (Boolean) -> Unit,
    onDismissSummary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    Column(modifier = modifier) {
        CapturedPieces(capturedPieces = data.captured.byOpponent, side = data.player, modifier = Modifier.fillMaxWidth())
        AnimatedBoard(
            targetState = data,
            contentKey = { it.id },
        ) { puzzleData ->
            ChessBoard(
                board = puzzleData.boardData,
                orientation = BoardOrientation.fromSide(puzzleData.player),
                onClick = onSquareClicked,
                modifier = Modifier.fillMaxWidth()
            )
        }
        CapturedPieces(capturedPieces = data.captured.byPlayer, side = data.player.other(), modifier = Modifier.fillMaxWidth())
        AnimatedControls(targetState = uiState is PuzzleStreakUiState.StreakEnded) { isEnded ->
            val controlsModifier = Modifier
                .fillMaxWidth()
                .padding(top = Design.dimensions.spacing.sm)
            if (isEnded && uiState is PuzzleStreakUiState.StreakEnded) {
                StreakEndedControls(
                    finalStreakCount = uiState.streakCount,
                    onNewStreak = onNewStreak,
                    modifier = controlsModifier,
                )
            } else if (uiState is PuzzleStreakUiState.Playing) {
                if (uiState.isAwaitingNextPuzzle) {
                    NextPuzzleControls(
                        onNextPuzzle = onNextPuzzle,
                        onAutoNext = onAutoNext,
                        modifier = controlsModifier,
                    )
                } else {
                    DefaultPuzzleControls(
                        hintEnabled = uiState.hintEnabled,
                        toMove = uiState.data.player,
                        onHintRequested = onHintRequested,
                        onAbandonRequested = onAbandon,
                        modifier = controlsModifier,
                        abandonEnabled = true,
                    )
                }
            }
        }

        // Dialogs
        if (uiState is PuzzleStreakUiState.Playing) {
            if (uiState.showAbandonDialog) {
                AbandonConfirmationDialog(
                    onConfirm = onAbandonConfirmed,
                    onDismiss = onAbandonDismissed
                )
            }
            if (uiState.data.promotion != null) {
                PromotionDialog(
                    side = data.player,
                    onPieceChosen = onPromote
                )
            }
        }
        if (uiState is PuzzleStreakUiState.StreakEnded && uiState.showSummary) {
            StreakSummaryDialog(
                streakCount = uiState.streakCount,
                isNewHighScore = uiState.isNewHighScore,
                onDismiss = onDismissSummary
            )
        }
    }
}

@Composable
private fun NextPuzzleControls(
    onNextPuzzle: () -> Unit,
    onAutoNext: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleNext by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = toggleNext,
            onCheckedChange = onAutoNext,
            modifier = Modifier.padding(end = Design.dimensions.spacing.lg)
        )
        Text(
            text = stringResource(R.string.settings_auto_next_puzzle),
            color = Design.colors.ink,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        PlayButton(
            textId = R.string.rated_puzzle_next_description,
            onClick = onNextPuzzle,
            modifier = Modifier
                .testTag { PuzzleStreakScreenTags.NEXT_PUZZLE },
        )
    }
}

@Composable
private fun StreakCounter(
    count: Int,
    modifier: Modifier = Modifier,
    tint: Color = Design.colors.primary,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.puzzle_streak_icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Design.dimensions.sizes.icon)
        )
        Text(
            text = count.toString(),
            style = Design.typography.titleLarge,
            color = tint,
            modifier = Modifier.testTag { PuzzleStreakScreenTags.STREAK_COUNTER }
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PlayingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            PuzzleStreakScreen(
                uiState = PuzzleStreakUiState.Playing(
                    data = PreviewData().whitePuzzleData(),
                    streakCount = 12,
                    hintEnabled = true,
                    showAbandonDialog = false,
                ),
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleEndedPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            PuzzleStreakScreen(
                uiState = PuzzleStreakUiState.Playing(
                    data = PreviewData().whitePuzzleData(),
                    streakCount = 12,
                    hintEnabled = true,
                    isAwaitingNextPuzzle = true,
                    showAbandonDialog = false,
                ),
            )
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StreakEndedPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            PuzzleStreakScreen(
                uiState = PuzzleStreakUiState.StreakEnded(
                    data = PreviewData().blackPuzzleData(),
                    streakCount = 15,
                    isNewHighScore = false,
                    showSummary = false,
                ),
            )
        }
    }
}
