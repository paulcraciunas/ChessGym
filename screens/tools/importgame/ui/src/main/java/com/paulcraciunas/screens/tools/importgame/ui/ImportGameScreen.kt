package com.paulcraciunas.screens.tools.importgame.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.MoveNavigationControls
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.PrimaryButton
import com.paulcraciunas.screens.common.design.components.PrimaryButtonStyle
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameScreenInteractor
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameUiState
import com.paulcraciunas.screens.tools.importgame.vm.ImportType
import com.paulcraciunas.screens.tools.importgame.vm.StubImportGameScreenInteractor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportGameScreen(
    uiState: ImportGameUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    onNavigateBack: () -> Unit,
    interactions: ImportGameScreenInteractor,
    modifier: Modifier = Modifier,
) {
    val bgColor = Design.colors.primarySoft
    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = stringResource(R.string.import_game_title)) },
        modifier = modifier.testTag { ImportGameScreenTags.SCREEN },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChessBoard(
                board = uiState.boardData,
                orientation = BoardOrientation.fromSide(uiState.orientation),
                onClick = if (uiState.isGameLoaded) interactions::onSquareClicked else { _ -> },
                showBorders = showBorders,
                highlightLegalMoves = highlightLegalMoves,
                enableAnimations = enableAnimations,
                modifier = Modifier.fillMaxWidth(),
            )
            ChessGymSpacer(size = SpacerSize.XXLARGE)
            AnimatedVisibility(
                visible = uiState.isGameLoaded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                MoveNavigationControls(
                    canGoBack = uiState.currentMoveIndex > 0,
                    canGoForward = uiState.currentMoveIndex < uiState.totalMoves,
                    onJumpToStart = interactions::onJumpToStart,
                    onPreviousMove = interactions::onPreviousMove,
                    onNextMove = interactions::onNextMove,
                    onJumpToEnd = interactions::onJumpToEnd,
                )
            }
            ChessGymSpacer()
            ImportButtons(
                onFenClicked = interactions::onFenClicked,
                onPgnClicked = interactions::onPgnClicked,
            )
        }
    }

    uiState.importDialogType?.let {
        ImportGameDialog(
            type = it,
            error = uiState.importError,
            onImport = interactions::onImport,
            onDismiss = interactions::onDismissDialog,
        )
    }

    uiState.pendingPromotion?.let {
        PromotionDialog(
            side = uiState.playerSide,
            onPieceChosen = interactions::onPromote,
        )
    }
}

@Composable
private fun ImportButtons(
    onFenClicked: () -> Unit,
    onPgnClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xgut),
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxl),
    ) {
        PrimaryButton(
            text = stringResource(R.string.import_fen),
            onClick = onFenClicked,
            modifier = Modifier
                .weight(1f)
                .testTag { ImportGameScreenTags.FEN_BUTTON },
            style = PrimaryButtonStyle.Clear,
        )
        PrimaryButton(
            text = stringResource(R.string.import_pgn),
            onClick = onPgnClicked,
            modifier = Modifier
                .weight(1f)
                .testTag { ImportGameScreenTags.PGN_BUTTON },
            style = PrimaryButtonStyle.Clear,
        )
    }
}

@Preview("ImportGame - Empty")
@Preview("ImportGame - Empty (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ImportGameEmptyPreview() {
    ChessGymTheme {
        ImportGameScreen(
            uiState = ImportGameUiState(),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = false,
            onNavigateBack = {},
            interactions = StubImportGameScreenInteractor(),
        )
    }
}

@Preview("ImportGame - Loaded")
@Composable
private fun ImportGameLoadedPreview() {
    ChessGymTheme {
        ImportGameScreen(
            uiState = ImportGameUiState(
                boardData = SampleBoardViewData.startingBoard(),
                isGameLoaded = true,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = false,
            onNavigateBack = {},
            interactions = StubImportGameScreenInteractor(),
        )
    }
}

@Preview("ImportGame - PGN Loaded with Navigation")
@Composable
private fun ImportGamePgnNavigationPreview() {
    ChessGymTheme {
        ImportGameScreen(
            uiState = ImportGameUiState(
                boardData = SampleBoardViewData.startingBoard(),
                isGameLoaded = true,
                importSource = ImportType.PGN,
                currentMoveIndex = 3,
                totalMoves = 8,
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = false,
            onNavigateBack = {},
            interactions = StubImportGameScreenInteractor(),
        )
    }
}
