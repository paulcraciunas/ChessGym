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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.backgroundColor
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.DefaultButton
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
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
        topBar = {
            AppBar(
                title = stringResource(R.string.import_game_title),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        modifier = modifier
            .testTag { ImportGameScreenTags.SCREEN }
            .semantics { this.backgroundColor = bgColor },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val displayBoard = remember(uiState.boardData, uiState.selectedSquare, uiState.legalMoves) {
                uiState.boardData.withMoveIndicators(uiState.selectedSquare, uiState.legalMoves)
            }

            ChessBoard(
                board = displayBoard,
                orientation = BoardOrientation.fromSide(uiState.playerSide),
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
private fun MoveNavigationControls(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onJumpToStart: () -> Unit,
    onPreviousMove: () -> Unit,
    onNextMove: () -> Unit,
    onJumpToEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xgut),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onJumpToStart,
            enabled = canGoBack,
            modifier = Modifier.testTag { ImportGameScreenTags.NAV_JUMP_TO_START },
        ) {
            Icon(
                painter = painterResource(R.drawable.keyboard_double_arrow_left),
                contentDescription = stringResource(R.string.import_game_jump_to_start),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
        IconButton(
            onClick = onPreviousMove,
            enabled = canGoBack,
            modifier = Modifier.testTag { ImportGameScreenTags.NAV_PREVIOUS },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.import_game_previous_move),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
        IconButton(
            onClick = onNextMove,
            enabled = canGoForward,
            modifier = Modifier.testTag { ImportGameScreenTags.NAV_NEXT },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.import_game_next_move),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
        IconButton(
            onClick = onJumpToEnd,
            enabled = canGoForward,
            modifier = Modifier.testTag { ImportGameScreenTags.NAV_JUMP_TO_END },
        ) {
            Icon(
                painter = painterResource(R.drawable.keyboard_double_arrow_right),
                contentDescription = stringResource(R.string.import_game_jump_to_end),
                modifier = Modifier.size(Design.dimensions.spacing.section),
            )
        }
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
        DefaultButton(
            onClick = onFenClicked,
            modifier = Modifier
                .weight(1f)
                .testTag { ImportGameScreenTags.FEN_BUTTON },
            text = R.string.import_fen
        )
        DefaultButton(
            onClick = onPgnClicked,
            modifier = Modifier
                .weight(1f)
                .testTag { ImportGameScreenTags.PGN_BUTTON },
            text = R.string.import_pgn
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
