package com.paulcraciunas.screens.loading.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design

private const val BOARD_SQUARES = 5
private const val BOARD_ALPHA = 0.08f
private const val KNIGHT_ALPHA = 0.06f
private val KNIGHT_SIZE = 180.dp

@Composable
internal fun ChessboardPattern(
    modifier: Modifier = Modifier,
) {
    val darkSquare = Design.colors.boardDark.copy(alpha = BOARD_ALPHA)
    Canvas(
        modifier = modifier.fillMaxSize(),
    ) {
        val squareSize = size.width / (BOARD_SQUARES * 2f)
        for (row in 0 until BOARD_SQUARES) {
            for (col in 0 until BOARD_SQUARES) {
                if ((row + col) % 2 == 1) {
                    drawRect(
                        color = darkSquare,
                        topLeft = Offset(col * squareSize, row * squareSize),
                        size = Size(squareSize, squareSize),
                    )
                }
            }
        }

    }
}

@Composable
internal fun KnightDecoration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.knight_white),
        contentDescription = null,
        colorFilter = ColorFilter.tint(Design.colors.primary.copy(alpha = KNIGHT_ALPHA)),
        modifier = modifier.size(KNIGHT_SIZE),
    )
}
