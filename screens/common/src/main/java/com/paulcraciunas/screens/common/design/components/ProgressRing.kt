package com.paulcraciunas.screens.common.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design

/**
 * SVG-style circular progress ring with optional centered content.
 *
 *   ProgressRing(progress = 0.65f, size = 72.dp) {
 *       Text("65%", style = …)
 *   }
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    color: Color? = null,
    trackColor: Color? = null,
    content: @Composable (() -> Unit)? = null,
) {
    val ringColor = color ?: Design.colors.primary
    val trackFill = trackColor ?: Design.colors.bgTint
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            drawArc(
                color = trackFill,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(stroke),
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        if (content != null) content()
    }
}

/** Slim linear progress used inside achievement tiles and rows. */
@Composable
fun LinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color? = null,
    height: Dp = Design.dimensions.sizes.progressBar,
) {
    val barColor = color ?: Design.colors.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Design.colors.bgTint, RoundedCornerShape(height / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .height(height)
                .background(barColor, RoundedCornerShape(height / 2))
        )
    }
}
