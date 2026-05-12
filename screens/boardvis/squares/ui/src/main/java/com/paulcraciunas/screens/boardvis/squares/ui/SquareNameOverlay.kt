package com.paulcraciunas.screens.boardvis.squares.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

private const val ANIMATION_DURATION_MS = 300

@Composable
internal fun SquareNameOverlay(
    currentSquare: Locus,
    showError: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (showError) {
            Color.Red.copy(alpha = 0.7f)
        } else {
            Design.colors.ink.copy(alpha = 0.4f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "textColorAnimation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentSquare,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(ANIMATION_DURATION_MS),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(ANIMATION_DURATION_MS),
                    targetOffsetX = { fullWidth -> -fullWidth }
                )
            },
            label = "squareNameAnimation"
        ) { square ->
            Text(
                text = square.toString().uppercase(),
                fontSize = 192.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                style = Design.typography.displayLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SquareNameOverlayPreview() {
    ChessGymTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SquareNameOverlay(
                currentSquare = "e4".loc(),
                showError = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SquareNameOverlayErrorPreview() {
    ChessGymTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SquareNameOverlay(
                currentSquare = "e4".loc(),
                showError = true
            )
        }
    }
}
