package com.paulcraciunas.screens.common.board.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.board.ChessPiece
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
fun BoardSquare2(
    side: Side,
    highlight: Boolean,
    modifier: Modifier = Modifier,
    content: (@Composable SquareScope2.() -> Unit) = {},
) {
    val backgroundColor = if (highlight) Design.colors.boardMovePrevious else side.backgroundColor()
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        content(SquareScope2)
    }
}

@Immutable
object SquareScope2 {
    @Composable
    fun Piece(
        piece: Piece,
        side: Side,
        selected: Boolean,
        modifier: Modifier = Modifier,
        alpha: Float = 1f,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (selected) Modifier.drawSelectionIndicator(Design.colors.boardSquareSelected)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            ChessPiece(
                piece = piece,
                side = side,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
        }
    }

    @Composable
    fun MoveIndicator() {
        Image(
            painter = painterResource(id = R.drawable.move_available),
            colorFilter = ColorFilter.tint(Design.colors.boardMoveAvailable),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.4f)
                .aspectRatio(1f)
        )
    }
}

private fun Modifier.drawSelectionIndicator(color: Color): Modifier = this.drawBehind {
    val path = Path().apply {
        addRect(Rect(Offset.Zero, size))
        addOval(Rect(center = center, radius = size.width / 2f))
        fillType = PathFillType.EvenOdd
    }
    drawPath(path = path, color = color)
}

@Composable
private fun Side.backgroundColor(): Color = when (this) {
    Side.WHITE -> Design.colors.boardLight
    Side.BLACK -> Design.colors.boardDark
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
                listOf(false, true).forEach { highlight ->
                    BoardSquare2(
                        side = side,
                        highlight = highlight,
                        modifier = Modifier.size(60.dp)
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
                listOf(false, true).forEach { highlight ->
                    BoardSquare2(
                        side = side,
                        highlight = highlight,
                        content = { Piece(piece = Piece.Queen, side = side, selected = false) },
                        modifier = Modifier.size(60.dp)
                    )
                    BoardSquare2(
                        side = side,
                        highlight = highlight,
                        content = { Piece(piece = Piece.King, side = side, selected = true) },
                        modifier = Modifier.size(60.dp)
                    )
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
                listOf(false, true).forEach { highlight ->
                    BoardSquare2(
                        side = side,
                        highlight = highlight,
                        content = { MoveIndicator() },
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
    }
}
