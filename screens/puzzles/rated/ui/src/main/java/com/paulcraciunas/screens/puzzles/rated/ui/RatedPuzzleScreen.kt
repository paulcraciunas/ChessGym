package com.paulcraciunas.screens.puzzles.rated.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.board.BoardOrientation
import com.paulcraciunas.screens.common.board.ChessBoard
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.SquareViewData
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.puzzles.rated.vm.RatedPuzzleScreenInteractor
import com.paulcraciunas.screens.puzzles.rated.vm.RatedPuzzleUiState
import com.paulcraciunas.screens.puzzles.rated.vm.StubRatedPuzzleScreenInteractor

@Composable
fun RatedPuzzleScreen(
    uiState: RatedPuzzleUiState,
    showBorders: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    interactions: RatedPuzzleScreenInteractor = StubRatedPuzzleScreenInteractor(),
) {
    if (uiState is RatedPuzzleUiState.Loading) {
        LoadingContent(modifier = Modifier.fillMaxSize())
        return
    }
    if (uiState is RatedPuzzleUiState.Failed) {
        FailedContent(modifier = Modifier.fillMaxSize())
        return
    }
    val data = (uiState as RatedPuzzleUiState.BoardState).data
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.rated_puzzle_title, data.rating),
                navButton = { Back(onClick = onNavigateBack) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CapturedPieces(
                capturedPieces = data.captured[data.player.other()]!!,
                side = data.player,
                modifier = Modifier.fillMaxWidth()
            )
            ChessBoard(
                board = data.boardData,
                orientation = BoardOrientation.fromSide(data.player),
                onClick = interactions::onSquareClicked,
                showBorders = showBorders,
                modifier = Modifier.fillMaxWidth()
            )
            CapturedPieces(
                capturedPieces = data.captured[data.player]!!,
                side = data.player.other(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            when (uiState) {
                is RatedPuzzleUiState.Playing -> {
                    PuzzleControls(
                        hintEnabled = uiState.hintEnabled,
                        toMove = data.player,
                        onHintRequested = interactions::onHintRequested,
                        onAbandonRequested = interactions::onAbandon,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Abandon confirmation dialog
                    if (uiState.showAbandonDialog) {
                        AbandonConfirmationDialog(
                            onConfirm = interactions::onAbandonConfirmed,
                            onDismiss = interactions::onAbandonDismissed
                        )
                    }
                    if (uiState.promotion != null) {
                        PromotionDialog(
                            side = data.player,
                            onPieceChosen = interactions::onPromote
                        )
                    }
                }

                is RatedPuzzleUiState.Finished -> FinishedPuzzleControls(
                    success = uiState.success,
                    ratingChange = uiState.ratingChange,
                    modifier = Modifier.fillMaxWidth(),
                    onPlayNext = interactions::onNextPuzzle,
                )

                else -> {}
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun WhitePlayingPreview() {
    ChessGymTheme {
        RatedPuzzleScreen(
            uiState = RatedPuzzleUiState.Playing(
                data = RatedPuzzleUiState.PuzzleData(
                    rating = 1450,
                    player = Side.WHITE,
                    boardData = sampleBoard(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Pawn, Piece.Knight, Piece.Pawn),
                        Side.BLACK to listOf(Piece.Bishop, Piece.Pawn, Piece.Pawn, Piece.Rook)
                    ),
                ),
                hintEnabled = true,
                showAbandonDialog = false,
                promotion = null,
            ),
            showBorders = true
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BlackPlayingPreview() {
    ChessGymTheme {
        RatedPuzzleScreen(
            uiState = RatedPuzzleUiState.Playing(
                data = RatedPuzzleUiState.PuzzleData(
                    rating = 1450,
                    player = Side.BLACK,
                    boardData = sampleBoard(),
                    captured = hashMapOf(
                        Side.WHITE to listOf(Piece.Pawn, Piece.Knight, Piece.Pawn),
                        Side.BLACK to listOf(Piece.Bishop, Piece.Pawn, Piece.Pawn, Piece.Rook)
                    ),
                ),
                hintEnabled = true,
                showAbandonDialog = false,
                promotion = null,
            ),
            showBorders = true
        )
    }
}

private fun sampleBoard(): BoardViewData {
    val squares: Array<Array<SquareViewData>> = Array(Rank.entries.size) {
        Array(File.entries.size) { SquareViewData(piece = null) }
    }
    squares.addWhitePieces()
    squares.addBlackPieces()

    return BoardViewData(squares)
}


private fun Array<Array<SquareViewData>>.addWhitePieces() = apply {
    File.entries.forEach { file ->
        this[Rank.`2`.dec()][file.dec()] = SquareViewData.simple(piece = Piece.Pawn, side = Side.WHITE)
    }
    addStartingPieces(Side.WHITE, Rank.`1`)
}

private fun Array<Array<SquareViewData>>.addBlackPieces() = apply {
    File.entries.forEach { file ->
        this[Rank.`7`.dec()][file.dec()] = SquareViewData.simple(piece = Piece.Pawn, side = Side.BLACK)
    }
    addStartingPieces(Side.BLACK, Rank.`8`)
}

private fun Array<Array<SquareViewData>>.addStartingPieces(side: Side, rank: Rank) {
    this[rank.dec()][File.a.dec()] = SquareViewData.simple(piece = Piece.Rook, side = side)
    this[rank.dec()][File.b.dec()] = SquareViewData.simple(piece = Piece.Knight, side = side)
    this[rank.dec()][File.c.dec()] = SquareViewData.simple(piece = Piece.Bishop, side = side)
    this[rank.dec()][File.d.dec()] = SquareViewData.simple(piece = Piece.Queen, side = side)
    this[rank.dec()][File.e.dec()] = SquareViewData.simple(piece = Piece.King, side = side)
    this[rank.dec()][File.f.dec()] = SquareViewData.simple(piece = Piece.Bishop, side = side)
    this[rank.dec()][File.g.dec()] = SquareViewData.simple(piece = Piece.Knight, side = side)
    this[rank.dec()][File.h.dec()] = SquareViewData.simple(piece = Piece.Rook, side = side)
}
