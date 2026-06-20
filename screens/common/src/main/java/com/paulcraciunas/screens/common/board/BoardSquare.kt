package com.paulcraciunas.screens.common.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.SidedPiece
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun BoardSquare(
    side: Side,
    piece: SidedPiece?,
    isLastMoveFrom: Boolean,
    isLastMoveTo: Boolean,
    isSelected: Boolean,
    colors: BoardColors,
    piecesAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .drawBehind {
                val selectionStrokeWidth = 4.dp.toPx()
                val halfStroke = selectionStrokeWidth / 2
                val selectedRectSize = Size(size.width - selectionStrokeWidth, size.width - selectionStrokeWidth)

                val hasPiece = piece != null

                val bgColor = if (side == Side.WHITE) colors.light else colors.dark
                drawRect(color = bgColor)
                if (isLastMoveFrom) {
                    drawRect(color = colors.movePreviousFrom)
                }
                if (isLastMoveTo) {
                    drawRect(color = colors.movePreviousTo)
                }

                if (isSelected) {
                    if (hasPiece) {
                        // Option A: Selected piece OR capturable piece (selected rect underneath)
                        drawRect(
                            color = colors.squareSelected,
                            topLeft = Offset(halfStroke, halfStroke),
                            size = selectedRectSize,
                            style = Stroke(width = selectionStrokeWidth)
                        )
                    } else {
                        // Option B: Available move space with no piece (small circle)
                        drawCircle(
                            color = colors.moveAvailable,
                            radius = size.width * 0.15f,
                            center = center
                        )
                    }
                }
            }
    ) {
        piece?.let {
            ChessPiece(
                piece = it,
                alpha = piecesAlpha,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlainSquarePreview() {
    ChessGymTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Side.entries.forEach { side ->
                listOf(true, false).forEach { selection ->
                    BoardSquare(
                        side = side,
                        piece = null,
                        isLastMoveFrom = false,
                        isLastMoveTo = false,
                        isSelected = selection,
                        colors = boardColors(),
                        piecesAlpha = 1f,
                        modifier = Modifier.size(60.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PieceSquarePreview() {
    ChessGymTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Side.entries.forEach { side ->
                listOf(false, true).forEach { selection ->
                    listOf(SidedPiece.WhiteQueen, SidedPiece.BlackKing).forEach { piece ->
                        BoardSquare(
                            side = side,
                            piece = piece,
                            isLastMoveFrom = false,
                            isLastMoveTo = false,
                            isSelected = selection,
                            colors = boardColors(),
                            piecesAlpha = 1f,
                            modifier = Modifier.size(60.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveAvailablePreview() {
    ChessGymTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Side.entries.forEach { side ->
                listOf(false, true).forEach { selected ->
                    listOf(false, true).forEach { lastMoveFrom ->
                        BoardSquare(
                            side = side,
                            piece = null,
                            isLastMoveFrom = lastMoveFrom,
                            isLastMoveTo = !lastMoveFrom,
                            isSelected = selected,
                            colors = boardColors(),
                            piecesAlpha = 1f,
                            modifier = Modifier.size(60.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun boardColors(): BoardColors = BoardColors(
    edge = Design.colors.boardEdge,
    light = Design.colors.boardLight,
    dark = Design.colors.boardDark,
    movePreviousFrom = Design.colors.boardMovePreviousFrom,
    movePreviousTo = Design.colors.boardMovePreviousTo,
    moveAvailable = Design.colors.boardMoveAvailable,
    squareSelected = Design.colors.boardSquareSelected,
)
