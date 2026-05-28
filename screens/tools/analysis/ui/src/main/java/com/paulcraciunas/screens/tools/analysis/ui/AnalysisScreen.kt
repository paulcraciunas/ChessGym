package com.paulcraciunas.screens.tools.analysis.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.model.BoardViewData2
import com.paulcraciunas.screens.common.model.GameViewModelHelper.GameData2
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.analysis.vm.AnalysisUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    uiState: AnalysisUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onSquareClicked: (locus: Locus) -> Unit = {},
    onPromote: (to: Piece) -> Unit = {},
    onJumpToStart: () -> Unit = {},
    onPreviousMove: () -> Unit = {},
    onNextMove: () -> Unit = {},
    onJumpToEnd: () -> Unit = {},
) {
    Scaffold(
        topBar = { ChildAppBar(onBack = onNavigateBack, title = stringResource(R.string.tools_analysis_title)) },
        containerColor = Design.colors.primarySoft,
        modifier = modifier.testTag { AnalysisScreenTags.SCREEN },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val data = uiState.data
            val orientation = BoardOrientation2.fromSide(data.player)
            CapturedPieces2(capturedPieces = data.captured.byOpponent, side = uiState.data.player, modifier = Modifier.fillMaxWidth())
            ChessBoard2(
                board = data.boardData,
                orientation = orientation,
                onClick = onSquareClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                MoveArrowOverlay(
                    move = uiState.engineData?.topMove,
                    orientation = orientation,
                    modifier = Modifier
                        .matchParentSize()
                        .let { mod ->
                            if (LocalUiSettings.current.showBorders) mod.padding(Design.dimensions.spacing.xl) else mod
                        },
                )
            }
            CapturedPieces2(
                capturedPieces = uiState.data.captured.byPlayer,
                side = uiState.data.player.other(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Design.dimensions.spacing.sm))
            MoveNavigationControls(
                canGoBack = uiState.canNavigateBack,
                canGoForward = uiState.canNavigateForward,
                onJumpToStart = onJumpToStart,
                onPreviousMove = onPreviousMove,
                onNextMove = onNextMove,
                onJumpToEnd = onJumpToEnd,
            )
            Spacer(modifier = Modifier.height(Design.dimensions.spacing.sm))
            EvaluationBar(
                evaluation = uiState.engineData?.evaluation,
                depth = uiState.engineData?.analysisDepth ?: 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Design.dimensions.spacing.xxl)
                    .testTag { AnalysisScreenTags.EVALUATION_BAR },
            )
            Spacer(modifier = Modifier.height(Design.dimensions.spacing.sm))
            if (uiState.engineData?.engineLines?.isEmpty() == false) {
                EngineLines(
                    lines = uiState.engineData?.engineLines!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag { AnalysisScreenTags.ENGINE_LINES },
                )
            }
            Spacer(modifier = Modifier.height(Design.dimensions.spacing.xxl))
        }
    }

    uiState.promotion?.let {
        PromotionDialog(
            side = uiState.data.player,
            onPieceChosen = onPromote,
        )
    }
}

@Preview("Analysis - Starting Position")
@Preview("Analysis - Starting Position (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AnalysisScreenStartingPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            AnalysisScreen(
                uiState = AnalysisUiState(
                    data = GameData2(
                        rating = 1442,
                        player = Side.WHITE,
                        boardData = BoardViewData2.default(),
                        captured = GameData2.GameCaptured(byOpponent = "", byPlayer = "")
                    ),
                    engineData = AnalysisUiState.EngineData(
                        evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.6f, display = "+0.3"),
                        engineLines = listOf(
                            AnalysisUiState.EngineData.SuggestedLine(
                                rank = 1,
                                evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.6f, display = "+0.3"),
                                moves = "e2e4 e7e5 Nf3",
                            ),
                            AnalysisUiState.EngineData.SuggestedLine(
                                rank = 2,
                                evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.55f, display = "+0.2"),
                                moves = "d2d4 d7d5",
                            ),
                            AnalysisUiState.EngineData.SuggestedLine(
                                rank = 3,
                                evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.51f, display = "+0.1"),
                                moves = "g1f3 d7d5",
                            ),
                        ),
                        topMove = AnalysisUiState.EngineData.SuggestedMove(from = Locus.e2, to = Locus.e4),
                        analysisDepth = 15,
                    ),
                    canNavigateBack = true,
                    canNavigateForward = false,
                ),
                onNavigateBack = {},
            )
        }
    }
}

@Preview("Analysis - Empty")
@Composable
private fun AnalysisScreenEmptyPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            AnalysisScreen(
                uiState = AnalysisUiState(
                    data = GameData2(
                        rating = 1442,
                        player = Side.WHITE,
                        boardData = BoardViewData2.default(),
                        captured = GameData2.GameCaptured(byOpponent = "", byPlayer = "")
                    ),
                    canNavigateBack = true,
                    canNavigateForward = false,
                ),
                onNavigateBack = {},
            )
        }
    }
}
