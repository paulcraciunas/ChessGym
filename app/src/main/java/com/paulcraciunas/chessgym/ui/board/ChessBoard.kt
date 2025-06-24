package com.paulcraciunas.chessgym.ui.board

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.chessgym.ui.model.BoardViewData
import com.paulcraciunas.chessgym.ui.model.BoardViewDataBuilder
import com.paulcraciunas.chessgym.ui.model.SquareViewData
import com.paulcraciunas.screens.common.theme.BoardTheme
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.File

// Most of the time, this will be the same as player's side
enum class BoardOrientation(val ranks: List<Rank>, val files: List<File>) {
    White(ranks = Rank.entries.reversed(), files = File.entries),
    Black(ranks = Rank.entries, files = File.entries.reversed())
}

@Composable
fun ChessBoard(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Rank, File) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO Paul: add border; this should be set in the Ui settings as well
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Column {
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
                                .clickable { onClick(rank, file) },
                        )
                    }
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
fun WhitePerspectivePreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.White,
            onClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BlackPerspectivePreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.Black,
            onClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreyThemePreview() {
    ChessGymTheme(
        boardTheme = BoardTheme.Grey
    ) {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.Black,
            onClick = { _, _ -> }
        )
    }
}
