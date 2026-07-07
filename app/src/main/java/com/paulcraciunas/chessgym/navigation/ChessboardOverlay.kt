package com.paulcraciunas.chessgym.navigation

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
internal fun ChessboardOverlay(
    onFullCoverageReached: () -> Unit,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cols = 8 // Fixed 8 columns across the width of the phone

    // Master timeline runner:
    // 0.0 -> 1.0 : Staggered Fade In
    // 1.0 -> 2.0 : Solid Hold state
    // 2.0 -> 3.0 : Staggered Fade Out
    val masterTimeline = remember { Animatable(0f) }

    // Remember structured randomized index lists so matrices stay static during redraw cycles
    var matrixTotalSquares by remember { mutableIntStateOf(0) }
    var randomizedOrder by remember { mutableStateOf(emptyList<Int>()) }

    LaunchedEffect(matrixTotalSquares) {
        if (matrixTotalSquares == 0) return@LaunchedEffect

        // Progressive staggered fade in
        masterTimeline.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = FADE_DURATION)
        )
        onFullCoverageReached()

        masterTimeline.snapTo(1.5f)
        delay(HOLD_DURATION)
        masterTimeline.snapTo(2.0f)

        // Progressive staggered fade out
        masterTimeline.animateTo(
            targetValue = 3f,
            animationSpec = tween(durationMillis = FADE_DURATION)
        )

        // Cleanup: Trigger deletion from the UI Tree
        onAnimationComplete()
    }

    val darkSquare = Design.colors.boardDark
    val lightSquare = Design.colors.boardLight
    Canvas(modifier = modifier.fillMaxSize()) {
        val squareSize = size.width / cols
        val renderSize = squareSize + 1.5f // Adding +1.5f anti-aliasing pixel padding ensures subpixel screen gaps never render
        val rows = ceil(size.height / squareSize).toInt()
        val totalSquares = rows * cols

        // Lazily initialize random sequence arrays on initial layout calculation frame
        if (matrixTotalSquares != totalSquares) {
            matrixTotalSquares = totalSquares
            randomizedOrder = (0 until totalSquares).shuffled(Random(seed = 42))
        }

        val timelineValue = masterTimeline.value

        for (index in 0 until totalSquares) {
            val row = index / cols
            val col = index % cols

            val baseColor = if ((row + col) % 2 == 0) darkSquare else lightSquare

            val orderIndex = randomizedOrder.getOrNull(index) ?: index
            val staggerFactor = orderIndex.toFloat() / totalSquares

            var squareAlpha = 0f

            when {
                timelineValue <= 1.0f -> { // Staggered Fade In
                    // Each block gets a localized 20% window to animate up smoothly
                    squareAlpha = ((timelineValue - (staggerFactor * 0.8f)) / 0.20f).coerceIn(0f, 1f)
                }
                timelineValue > 1.0f && timelineValue <= 2.0f -> { // Holding solid
                    squareAlpha = 1f
                }
                timelineValue > 2.0f -> { // Staggered Fade Out
                    val fadeOutProgress = timelineValue - 2.0f // Ranges 0.0f to 1.0f
                    val individualFadeOut = ((fadeOutProgress - (staggerFactor * 0.8f)) / 0.20f).coerceIn(0f, 1f)
                    squareAlpha = 1f - individualFadeOut
                }
            }

            if (squareAlpha > 0f) {
                drawRect(
                    color = baseColor,
                    topLeft = Offset(col * squareSize, row * squareSize),
                    size = Size(renderSize, renderSize),
                    alpha = squareAlpha,
                )
            }
        }
    }
}

private const val FADE_DURATION = 600
private const val HOLD_DURATION = 200L


@Preview("FadeSplashOverlay - Normal")
@Preview("FadeSplashOverlay - Normal (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun NormalPreview() {
    ChessGymTheme {
        ChessboardOverlay(
            onFullCoverageReached = {},
            onAnimationComplete = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
