package com.paulcraciunas.screens.common.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun MoveArrowOverlay(
    from: Locus,
    to: Locus,
    orientation: BoardOrientation,
    modifier: Modifier = Modifier,
    color: Color = Design.colors.ink,
) {
    val tipPainter = rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.line_end_tip))
    val bodyPainter = rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.line_body))

    val indices = remember(from, to,orientation) {
        object {
            val fromFile = fileIndex(from.file.ordinal, orientation)
            val fromRank = rankIndex(from.rank.ordinal, orientation)
            val toFile = fileIndex(to.file.ordinal, orientation)
            val toRank = rankIndex(to.rank.ordinal, orientation)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val boardSize = minOf(size.width, size.height)
        val squareSize = boardSize / 8f

        val fromCenter = Offset(
            x = (indices.fromFile + 0.5f) * squareSize,
            y = (indices.fromRank + 0.5f) * squareSize,
        )
        val toCenter = Offset(
            x = (indices.toFile + 0.5f) * squareSize,
            y = (indices.toRank + 0.5f) * squareSize,
        )

        drawMoveArrow(
            from = fromCenter,
            to = toCenter,
            squareSize = squareSize,
            color = color.copy(alpha = ARROW_ALPHA),
            tipPainter = tipPainter,
            bodyPainter = bodyPainter,
        )
    }
}

private fun fileIndex(fileOrdinal: Int, orientation: BoardOrientation): Int =
    when (orientation) {
        BoardOrientation.White -> fileOrdinal
        BoardOrientation.Black -> 7 - fileOrdinal
    }

private fun rankIndex(rankOrdinal: Int, orientation: BoardOrientation): Int =
    when (orientation) {
        BoardOrientation.White -> 7 - rankOrdinal
        BoardOrientation.Black -> rankOrdinal
    }

private fun DrawScope.drawMoveArrow(
    from: Offset,
    to: Offset,
    squareSize: Float,
    color: Color,
    tipPainter: Painter,
    bodyPainter: Painter,
) {
    val arrowheadSize = squareSize * ARROWHEAD_SCALE_FACTOR
    val tipScale = arrowheadSize / 960f

    // Coordinates from the Material vector viewport (960x960)
    val viewportTipX = 783f
    val viewportNotchX = 480f
    val viewportCenterX = 480f

    val dx = to.x - from.x
    val dy = to.y - from.y
    val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
    val angleRad = atan2(dy.toDouble(), dx.toDouble()).toFloat()
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

    // 1. Calculate tail length to meet the arrowhead notch exactly
    // Distance from the tip of the arrow to where the notch is
    val tipToNotchDist = (viewportTipX - viewportNotchX) * tipScale
    val tailLength = distance - tipToNotchDist

    // 2. Draw the body (tail)
    rotate(degrees = angleDeg, pivot = from) {
        translate(left = from.x, top = from.y - arrowheadSize / 2) {
            with(bodyPainter) {
                draw(
                    size = Size(tailLength, arrowheadSize),
                    colorFilter = ColorFilter.tint(color)
                )
            }
        }
    }

    // 3. Draw the tip (head)
    // We must shift the drawing so the vector's tip (at 783) lands exactly on 'to'
    val tipToCenterDist = (viewportTipX - viewportCenterX) * tipScale
    val shiftedTo = Offset(
        x = to.x - tipToCenterDist * cos(angleRad),
        y = to.y - tipToCenterDist * sin(angleRad)
    )

    translate(left = shiftedTo.x - arrowheadSize / 2, top = shiftedTo.y - arrowheadSize / 2) {
        rotate(degrees = angleDeg, pivot = Offset(arrowheadSize / 2, arrowheadSize / 2)) {
            with(tipPainter) {
                draw(
                    size = Size(arrowheadSize, arrowheadSize),
                    colorFilter = ColorFilter.tint(color)
                )
            }
        }
    }
}

private const val ARROW_ALPHA = 0.55f
private const val ARROWHEAD_SCALE_FACTOR = 0.9f

@Preview(showBackground = true)
@Composable
private fun MoveArrowWhitePreview() {
    ChessGymTheme {
        Box(modifier = Modifier.size(300.dp)) {
            MoveArrowOverlay(
                from = Locus.e2,
                to = Locus.e4,
                orientation = BoardOrientation.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveArrowKnightPreview() {
    ChessGymTheme {
        Box(modifier = Modifier.size(300.dp)) {
            MoveArrowOverlay(
                from = Locus.g1,
                to = Locus.f3,
                orientation = BoardOrientation.White,
                color = Color.Green
            )
        }
    }
}
