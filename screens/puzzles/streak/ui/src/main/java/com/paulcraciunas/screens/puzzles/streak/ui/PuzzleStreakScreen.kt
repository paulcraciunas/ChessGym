package com.paulcraciunas.screens.puzzles.streak.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.paulcraciunas.screens.common.board.v2.BoardOrientation2
import com.paulcraciunas.screens.common.board.v2.ChessBoard2
import com.paulcraciunas.screens.common.controls.DefaultPuzzleControls
import com.paulcraciunas.screens.common.controls.v2.CapturedPieces2
import com.paulcraciunas.screens.common.design.components.PrimaryButton
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
    onDismissSummary: () -> Unit = {},
) {
    val streakCount = when (uiState) {
        is PuzzleStreakUiState.Playing -> uiState.streakCount
        is PuzzleStreakUiState.StreakEnded -> 0
        else -> 0
    }
    Scaffold(
        topBar = {
            ChildAppBar(
                title = stringResource(R.string.puzzle_mode_streak_title),
                onBack = onNavigateBack,
                actions = {
                    if (streakCount > 0) StreakCounter(
                        count = streakCount,
                        modifier = Modifier.padding(horizontal = Design.dimensions.spacing.lg, vertical = Design.dimensions.spacing.sm)
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
            is PuzzleStreakUiState.BoardState -> PuzzleStreakContent(
                uiState = uiState,
                onSquareClicked = onSquareClicked,
                onPromote = onPromote,
                onHintRequested = onHintRequested,
                onAbandon = onAbandon,
                onAbandonConfirmed = onAbandonConfirmed,
                onAbandonDismissed = onAbandonDismissed,
                onNewStreak = onNewStreak,
                onNextPuzzle = onNextPuzzle,
                onDismissSummary = onDismissSummary,
                modifier = defaultModifier,
            )
        }
    }
}

@Composable
private fun PuzzleStreakContent(
    uiState: PuzzleStreakUiState.BoardState,
    onSquareClicked: (selection: Locus) -> Unit,
    onPromote: (to: Piece) -> Unit,
    onHintRequested: () -> Unit,
    onAbandon: () -> Unit,
    onAbandonConfirmed: () -> Unit,
    onAbandonDismissed: () -> Unit,
    onNewStreak: () -> Unit,
    onNextPuzzle: () -> Unit,
    onDismissSummary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    val isShowingSolution = uiState is PuzzleStreakUiState.Playing && uiState.isShowingSolution
    val isAwaitingNext = uiState is PuzzleStreakUiState.Playing && uiState.isAwaitingNextPuzzle
    val isBoardInteractive = !isShowingSolution && !isAwaitingNext
    Column(modifier = modifier) {
        CapturedPieces2(capturedPieces = data.captured.byOpponent, side = data.player, modifier = Modifier.fillMaxWidth())
        AnimatedBoard(
            targetState = data,
            contentKey = { it.id },
        ) { puzzleData ->
            ChessBoard2(
                board = puzzleData.boardData,
                orientation = BoardOrientation2.fromSide(puzzleData.player),
                onClick = if (isBoardInteractive) onSquareClicked else { _ -> },
                modifier = Modifier.fillMaxWidth()
            )
        }
        CapturedPieces2(capturedPieces = data.captured.byPlayer, side = data.player.other(), modifier = Modifier.fillMaxWidth())
        AnimatedControls(targetState = uiState is PuzzleStreakUiState.StreakEnded) { isEnded ->
            val controlsModifier = Modifier
                .fillMaxWidth()
                .padding(top = Design.dimensions.spacing.sm)
            if (isEnded && uiState is PuzzleStreakUiState.StreakEnded) {
                StreakEndedControls(
                    finalStreakCount = uiState.finalStreakCount,
                    onNewStreak = onNewStreak,
                    modifier = controlsModifier,
                )
            } else if (uiState is PuzzleStreakUiState.Playing) {
                if (isAwaitingNext) {
                    NextPuzzleControls(
                        streakCount = uiState.streakCount,
                        onNextPuzzle = onNextPuzzle,
                        modifier = controlsModifier,
                    )
                } else {
                    DefaultPuzzleControls(
                        hintEnabled = uiState.hintEnabled && !isShowingSolution,
                        toMove = data.player,
                        onHintRequested = onHintRequested,
                        onAbandonRequested = onAbandon,
                        modifier = controlsModifier,
                        abandonEnabled = !isShowingSolution,
                    )
                }
            }
        }

        // Dialogs
        if (uiState is PuzzleStreakUiState.Playing && !isShowingSolution) {
            if (uiState.showAbandonDialog) {
                AbandonConfirmationDialog(
                    onConfirm = onAbandonConfirmed,
                    onDismiss = onAbandonDismissed
                )
            }
            if (uiState.promotion != null) {
                PromotionDialog(
                    side = data.player,
                    onPieceChosen = onPromote
                )
            }
        }
        if (uiState is PuzzleStreakUiState.StreakEnded && uiState.showSummary) {
            StreakSummaryDialog(
                streakCount = uiState.finalStreakCount,
                isNewHighScore = uiState.isNewHighScore,
                onDismiss = onDismissSummary
            )
        }
    }
}

@Composable
private fun NextPuzzleControls(
    streakCount: Int,
    onNextPuzzle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StreakCounter(
            count = streakCount,
            tint = Design.colors.success
        )
        PrimaryButton(
            text = stringResource(R.string.puzzle_streak_next_puzzle),
            onClick = onNextPuzzle,
            modifier = Modifier.testTag { PuzzleStreakScreenTags.NEXT_PUZZLE },
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
                    promotion = null,
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
                    promotion = null,
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
                    finalStreakCount = 15,
                    isNewHighScore = false,
                    showSummary = false,
                ),
            )
        }
    }
}
