package com.paulcraciunas.screens.tools.importgame.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.analysis.MoveClassification
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.controls.FlipBoardButton
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.previews.PreviewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameUiState
import kotlin.Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportGameScreen(
    uiState: ImportGameUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onImport: (pgn: String) -> Unit = {},
    onFlipBoard: () -> Unit = {},
    onMoveSelected: (Int) -> Unit = {},
    onJumpToStart: () -> Unit = {},
    onPreviousMove: () -> Unit = {},
    onNextMove: () -> Unit = {},
    onJumpToEnd: () -> Unit = {},
    onAbandonConfirmed: () -> Unit = {},
    onAbandonDismissed: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ChildAppBar(
                onBack = onNavigateBack,
                title = stringResource(R.string.import_game_title),
                actions = {
                    if (uiState is ImportGameUiState.Complete) {
                        FlipBoardButton(onClick = onFlipBoard)
                    }
                }
            )
        },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { ImportGameScreenTags.SCREEN },
    ) { innerPadding ->
        val defaultModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            is ImportGameUiState.Setup -> SetupContent(
                hasError = uiState.hasImportError,
                onImport = onImport,
                modifier = defaultModifier,
            )
            is ImportGameUiState.Loading -> LoadingContent(
                state = uiState,
                modifier = defaultModifier,
                onAbandonConfirmed = onAbandonConfirmed,
                onAbandonDismissed = onAbandonDismissed,
            )
            is ImportGameUiState.Complete -> CompleteContent(
                state = uiState,
                onMoveSelected = onMoveSelected,
                onJumpToStart = onJumpToStart,
                onPreviousMove = onPreviousMove,
                onNextMove = onNextMove,
                onJumpToEnd = onJumpToEnd,
                modifier = defaultModifier,
            )
        }
    }
}

@Preview("ImportGame - Setup")
@Preview("ImportGame - Setup (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ImportGameSetupPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ImportGameScreen(
                uiState = ImportGameUiState.Setup(),
                onNavigateBack = {},
            )
        }
    }
}

@Preview("ImportGame - Loading")
@Composable
private fun ImportGameLoadingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ImportGameScreen(
                uiState = ImportGameUiState.Loading(
                    boardState = PreviewData().whiteGameData(),
                    progressPercent = 42,
                    analysedMoves = previewMoves(),
                ),
                onNavigateBack = {},
            )
        }
    }
}

@Preview("ImportGame - Complete")
@Composable
private fun ImportGameCompletePreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ImportGameScreen(
                uiState = ImportGameUiState.Complete(
                    boardState = PreviewData().whiteGameData(),
                    analysedMoves = previewMoves(),
                    currentMoveIndex = 2,
                    canNavigateBack = true,
                    canNavigateForward = true,
                ),
                onNavigateBack = {},
            )
        }
    }
}

@Preview("ImportGame - Flipped")
@Preview("ImportGame - Flipped (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ImportGameFlippedPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ImportGameScreen(
                uiState = ImportGameUiState.Complete(
                    boardState = PreviewData().whiteGameData(),
                    orientation = Side.BLACK,
                    analysedMoves = previewMoves(),
                    currentMoveIndex = 2,
                    canNavigateBack = true,
                    canNavigateForward = true,
                ),
                onNavigateBack = {},
            )
        }
    }
}

internal fun previewMoves(): List<ImportGameUiState.AnalysedMove> = listOf(
    ImportGameUiState.AnalysedMove("e4", MoveClassification.Good, 0.55f, null),
    ImportGameUiState.AnalysedMove("e5", MoveClassification.Good, 0.50f, null),
    ImportGameUiState.AnalysedMove("Nf3", MoveClassification.Great, 0.58f, null),
    ImportGameUiState.AnalysedMove("Nc6", MoveClassification.Good, 0.52f, null),
    ImportGameUiState.AnalysedMove("Bb5", MoveClassification.Good, 0.55f, null),
    ImportGameUiState.AnalysedMove("a6", MoveClassification.Inaccuracy, 0.62f, null),
)
