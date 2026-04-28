package com.paulcraciunas.screens.tools.analysis.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.engine.api.UciMoveParser
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.global.resources.R
import androidx.compose.ui.semantics.semantics
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.backgroundColor
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.controls.CapturedPieces
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.previews.SampleBoardViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.analysis.vm.AnalysisScreenInteractor
import com.paulcraciunas.screens.tools.analysis.vm.AnalysisUiState
import com.paulcraciunas.screens.tools.analysis.vm.MoveArrow
import com.paulcraciunas.screens.tools.analysis.vm.StubAnalysisScreenInteractor

@Composable
fun AnalysisScreen(
    uiState: AnalysisUiState,
    showBorders: Boolean,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    onNavigateBack: () -> Unit,
    interactions: AnalysisScreenInteractor,
    modifier: Modifier = Modifier,
) {
    val bgColor = MaterialTheme.colorScheme.background
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.tools_analysis_title),
                navButton = { Back(onClick = onNavigateBack) },
            )
        },
        modifier = modifier
            .testTag { AnalysisScreenTags.SCREEN }
            .semantics { this.backgroundColor = bgColor },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val displayBoard = remember(
                uiState.boardData,
                uiState.selectedSquare,
                uiState.legalMoves,
            ) {
                uiState.boardData.withMoveIndicators(
                    uiState.selectedSquare,
                    uiState.legalMoves,
                )
            }

            CapturedPieces(
                capturedPieces = uiState.captured[uiState.playerSide.other()]!!,
                side = uiState.playerSide,
                modifier = Modifier.fillMaxWidth(),
            )
            ChessBoard(
                board = displayBoard,
                orientation = BoardOrientation.fromSide(uiState.playerSide),
                onClick = interactions::onSquareClicked,
                showBorders = showBorders,
                highlightLegalMoves = highlightLegalMoves,
                enableAnimations = enableAnimations,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MoveArrowOverlay(
                    arrow = uiState.topMoveArrow,
                    orientation = BoardOrientation.fromSide(uiState.playerSide),
                    modifier = Modifier
                        .matchParentSize()
                        .let { mod ->
                            if (showBorders) mod.padding(14.dp) else mod
                        },
                )
            }
            CapturedPieces(
                capturedPieces = uiState.captured[uiState.playerSide]!!,
                side = uiState.playerSide.other(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            EvaluationBar(
                evaluation = uiState.evaluation,
                depth = uiState.analysisDepth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag { AnalysisScreenTags.EVALUATION_BAR },
            )
            Spacer(modifier = Modifier.height(8.dp))
            EngineLines(
                lines = uiState.engineLines,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag { AnalysisScreenTags.ENGINE_LINES },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    uiState.pendingPromotion?.let {
        PromotionDialog(
            side = uiState.playerSide,
            onPieceChosen = interactions::onPromote,
        )
    }
}

@Preview("Analysis - Starting Position")
@Preview("Analysis - Starting Position (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AnalysisScreenStartingPreview() {
    ChessGymTheme {
        AnalysisScreen(
            uiState = AnalysisUiState(
                boardData = SampleBoardViewData.startingBoard(),
                evaluation = Evaluation.Centipawns(30),
                analysisDepth = 15,
                engineLines = listOf(
                    EngineLine(1, Evaluation.Centipawns(30), sampleMoves("e2e4", "e7e5", "g1f3")),
                    EngineLine(2, Evaluation.Centipawns(20), sampleMoves("d2d4", "d7d5")),
                    EngineLine(3, Evaluation.Centipawns(12), sampleMoves("g1f3", "d7d5")),
                ),
                topMoveArrow = MoveArrow(
                    from = Locus(
                        File.e,
                        Rank.`2`,
                    ),
                    to = Locus(
                        File.e,
                        Rank.`4`,
                    ),
                ),
                captured = hashMapOf(
                    Side.WHITE to listOf(Piece.Pawn, Piece.Knight, Piece.Pawn),
                    Side.BLACK to listOf(Piece.Bishop, Piece.Pawn, Piece.Pawn, Piece.Rook)
                ),
            ),
            showBorders = true,
            highlightLegalMoves = true,
            enableAnimations = false,
            onNavigateBack = {},
            interactions = StubAnalysisScreenInteractor(),
        )
    }
}

@Preview("Analysis - Empty")
@Composable
private fun AnalysisScreenEmptyPreview() {
    ChessGymTheme {
        AnalysisScreen(
            uiState = AnalysisUiState(
                captured = hashMapOf(
                    Side.WHITE to emptyList(),
                    Side.BLACK to emptyList()
                )
            ),
            showBorders = false,
            highlightLegalMoves = true,
            enableAnimations = false,
            onNavigateBack = {},
            interactions = StubAnalysisScreenInteractor(),
        )
    }
}

private fun sampleMoves(vararg uciMoves: String): List<EngineMove> =
    uciMoves.mapNotNull { UciMoveParser.parse(it) }
