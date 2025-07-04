package com.paulcraciunas.chessgym.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.theme.BoardColors
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.resources.R

@Composable
fun BoardSquare(
    side: Side,
    highlight: Boolean,
    content: (@Composable SquareScope.() -> Unit),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        val squareScope = remember(side, highlight) { SquareScope(side, highlight) }
        content(squareScope)
    }
}

@Stable
class SquareScope internal constructor(
    private val side: Side,
    private val highlight: Boolean,
) {
    @Composable
    fun Plain(modifier: Modifier = Modifier) {
        val background = background()
        Canvas(modifier = modifier.fillMaxSize(1f)) {
            drawRect(color = background)
        }
    }

    @Composable
    fun Piece(
        piece: Piece,
        side: Side,
        selected: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val background = background()
        val selectedBackground = BoardColors.boardSquareSelected
        Canvas(modifier = modifier.fillMaxSize(1f)) {
            if (selected) {
                drawRect(color = selectedBackground)
                drawCircle(
                    radius = size.width / 2,
                    color = background,
                )
            } else {
                drawRect(color = background)
            }
        }

        ChessPiece(
            piece = piece,
            side = side,
            modifier = modifier
        )
    }

    @Composable
    fun MoveAvailable(modifier: Modifier = Modifier) {
        val background = background()
        Canvas(modifier = modifier.fillMaxSize(1f)) {
            drawRect(color = background)
        }
        Image(
            painter = painterResource(id = R.drawable.move_available),
            colorFilter = ColorFilter.tint(BoardColors.boardMoveAvailable),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.4f)
                .aspectRatio(1f)
        )
    }

    @Composable
    private fun background() = if (highlight) BoardColors.boardMovePrevious else side.background()

    @Composable
    private fun Side.background(): Color = when (this) {
        Side.WHITE -> BoardColors.boardSquareWhite
        Side.BLACK -> BoardColors.boardSquareBlack
    }
}

@Preview(showBackground = true)
@Composable
fun PlainSquarePreview() {
    ChessGymTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Side.entries.forEach { side ->
                listOf(false, true).forEach { highlight ->
                    BoardSquare(
                        side = side,
                        highlight = highlight,
                        content = { Plain() },
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PieceSquarePreview() {
    ChessGymTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Side.entries.forEach { side ->
                listOf(false, true).forEach { highlight ->
                    BoardSquare(
                        side = side,
                        highlight = highlight,
                        content = { Piece(piece = Piece.Queen, side = side, selected = false) },
                        modifier = Modifier.size(60.dp)
                    )
                    BoardSquare(
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
fun MoveAvailablePreview() {
    ChessGymTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Side.entries.forEach { side ->
                listOf(false, true).forEach { highlight ->
                    BoardSquare(
                        side = side,
                        highlight = highlight,
                        content = { MoveAvailable() },
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
    }
}
