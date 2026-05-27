package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = Design.dimensions.sizes.progressRing,
    strokeWidth: Dp = Design.dimensions.sizes.progressBar,
    color: Color = Design.colors.primary,
    trackColor: Color = Design.colors.border,
    content: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(stroke),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Composable
fun LinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Design.colors.primary,
) {
    val height = Design.dimensions.sizes.progressBar
    val trackColor = Design.colors.bgTint
    val cornerRadius = Design.radii.xs
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                val width = size.width
                val barHeight = size.height
                val radiusPx = cornerRadius.toPx()
                val cornerRadiusObj = CornerRadius(radiusPx, radiusPx)
                drawRoundRect( // background track (Full Width)
                    color = trackColor,
                    size = size,
                    cornerRadius = cornerRadiusObj
                )
                val progressWidth = width * progress.coerceIn(0f, 1f)
                drawRoundRect( // active progress bar (Fractional Width)
                    color = color,
                    size = Size(width = progressWidth, height = barHeight),
                    cornerRadius = cornerRadiusObj
                )
            }
    )
}
