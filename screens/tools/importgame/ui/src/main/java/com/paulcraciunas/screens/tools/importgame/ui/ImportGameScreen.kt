package com.paulcraciunas.screens.tools.importgame.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.board.v2.BoardOrientation2
import com.paulcraciunas.screens.common.board.v2.ChessBoard2
import com.paulcraciunas.screens.common.controls.MoveNavigationControls
import com.paulcraciunas.screens.common.controls.v2.CapturedPieces2
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.PrimaryButton
import com.paulcraciunas.screens.common.design.components.PrimaryButtonStyle
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.BoardViewData2
import com.paulcraciunas.screens.common.model.GameViewModelHelper.GameData2
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameUiState
import com.paulcraciunas.screens.tools.importgame.vm.ImportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportGameScreen(
    uiState: ImportGameUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onFenClicked: () -> Unit = {},
    onPgnClicked: () -> Unit = {},
    onImport: (text: String) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onJumpToStart: () -> Unit = {},
    onPreviousMove: () -> Unit = {},
    onNextMove: () -> Unit = {},
    onJumpToEnd: () -> Unit = {},
) {
    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = stringResource(R.string.import_game_title)) },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { ImportGameScreenTags.SCREEN },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val data = uiState.data
            CapturedPieces2(capturedPieces =data.captured.byOpponent, side = uiState.data.player, modifier = Modifier.fillMaxWidth())
            ChessBoard2(
                board = data.boardData,
                orientation = BoardOrientation2.fromSide(data.player),
                onClick = if (uiState.isGameLoaded) onSquareClicked else { _ -> },
                modifier = Modifier.fillMaxWidth()
            )
            CapturedPieces2(capturedPieces = uiState.data.captured.byPlayer, side = uiState.data.player.other(), modifier = Modifier.fillMaxWidth())
            ChessGymSpacer(size = SpacerSize.XXLARGE)
            AnimatedVisibility(
                visible = uiState.isGameLoaded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                MoveNavigationControls(
                    canGoBack = uiState.canNavigateBack,
                    canGoForward = uiState.canNavigateForward,
                    onJumpToStart = onJumpToStart,
                    onPreviousMove = onPreviousMove,
                    onNextMove = onNextMove,
                    onJumpToEnd = onJumpToEnd,
                )
            }
            ChessGymSpacer()
            ImportButtons(
                onFenClicked = onFenClicked,
                onPgnClicked = onPgnClicked,
            )
        }
    }

    uiState.showImportDialog?.let {
        ImportGameDialog(
            type = it,
            error = uiState.importError,
            onImport = onImport,
            onDismiss = onDismissDialog,
        )
    }

    uiState.promotion?.let {
        PromotionDialog(side = uiState.data.player, onPieceChosen = onPromote)
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
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ImportGameScreen(
                uiState = ImportGameUiState(),
                onNavigateBack = {},
            )
        }
    }
}

@Preview("ImportGame - Loaded")
@Composable
private fun ImportGameLoadedPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ImportGameScreen(
                uiState = ImportGameUiState(
                    data = GameData2(
                        rating = 1442,
                        player = Side.WHITE,
                        boardData = BoardViewData2.default(),
                        captured = GameData2.GameCaptured(byOpponent = "", byPlayer = "")
                    ),
                    isGameLoaded = true,
                    importType = ImportType.FEN,
                ),
                onNavigateBack = {},
            )
        }
    }
}

@Preview("ImportGame - PGN Loaded with Navigation")
@Composable
private fun ImportGamePgnNavigationPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ImportGameScreen(
                uiState = ImportGameUiState(
                    data = GameData2(
                        rating = 1442,
                        player = Side.WHITE,
                        boardData = BoardViewData2.default(),
                        captured = GameData2.GameCaptured(byOpponent = "", byPlayer = "")
                    ),
                    isGameLoaded = true,
                    importType = ImportType.PGN,
                    canNavigateBack = false,
                    canNavigateForward = true,
                ),
                onNavigateBack = {},
            )
        }
    }
}
