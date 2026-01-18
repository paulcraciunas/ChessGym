package com.paulcraciunas.screens.common.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import com.paulcraciunas.screens.common.model.SquareViewData
import com.paulcraciunas.screens.common.theme.BoardColors
import com.paulcraciunas.screens.common.theme.BoardTheme
import com.paulcraciunas.screens.common.theme.ChessGymTheme

private val borderSize = 14.dp

// Most of the time, this will be the same as player's side
enum class BoardOrientation(val ranks: List<Rank>, val files: List<File>) {
    White(ranks = Rank.entries.reversed(), files = File.entries),
    Black(ranks = Rank.entries, files = File.entries.reversed());

    companion object {
        fun fromSide(player: Side): BoardOrientation = if (player == Side.WHITE) White else Black
    }
}

@Composable
fun ChessBoard(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    showBorders: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!showBorders) {
        ChessBoardContents(
            board = board,
            orientation = orientation,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(color = BoardColors.boardEdge),
            contentAlignment = Alignment.Center
        ) {
            BorderRanks(orientation = orientation, modifier = Modifier.align(Alignment.TopStart), width = borderSize)
            BorderFiles(orientation = orientation, modifier = Modifier.align(Alignment.TopCenter), height = borderSize)
            ChessBoardContents(
                board = board,
                orientation = orientation,
                onClick = onClick,
                modifier = modifier.padding(borderSize)
            )
            BorderFiles(orientation = orientation, modifier = Modifier.align(Alignment.BottomCenter), height = borderSize)
            BorderRanks(orientation = orientation, modifier = Modifier.align(Alignment.TopEnd), width = borderSize)
        }
    }
}

@Composable
private fun ChessBoardContents(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        for (rank in orientation.ranks) {
            Row {
                for (file in orientation.files) {
                    val square = board.at(rank, file)
                    val squareSide = squareSide(file, rank)
                    BoardSquare(
                        side = squareSide,
                        highlight = square.lastMove,
                        content = { SquareContent(square) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onClick(Locus(file, rank)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareScope.SquareContent(square: SquareViewData) = when {
    square.piece != null -> Piece(
        piece = square.piece.piece,
        side = square.piece.side,
        selected = square.piece.isSelected
    )

    square.canMoveTo -> MoveAvailable()
    else -> Plain()
}

private fun squareSide(file: File, rank: Rank): Side =
    if ((rank.ordinal + file.ordinal) % 2 == 0) Side.BLACK
    else Side.WHITE

@Preview(showBackground = true)
@Composable
private fun WhitePerspectivePreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.White,
            onClick = { _ -> },
            showBorders = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlackPerspectivePreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.Black,
            onClick = { _ -> },
            showBorders = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GreyThemePreview() {
    ChessGymTheme(
        boardTheme = BoardTheme.Grey
    ) {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.Black,
            onClick = { _ -> },
            showBorders = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WhitePerspectiveBordersPreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.White,
            onClick = { _ -> },
            showBorders = true
        )
    }
}
