package com.paulcraciunas.screens.tools.analysis.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.screens.common.theme.ChessGymTheme

private const val ANIMATION_DURATION_MS = 300
private const val BAR_HEIGHT_DP = 28
private const val CORNER_RADIUS_DP = 4

@Composable
internal fun EvaluationBar(
    evaluation: Evaluation?,
    depth: Int,
    modifier: Modifier = Modifier,
) {
    val targetFraction = remember(evaluation) {
        evaluation?.toFraction() ?: 0.5f
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "eval_bar_animation",
    )

    // Ensure the fraction stays within bounds for visibility
    val displayFraction = animatedFraction.coerceIn(Evaluation.MIN_FRACTION, Evaluation.MAX_FRACTION)
    val isWhiteLeading = displayFraction >= 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BAR_HEIGHT_DP.dp)
            .clip(RoundedCornerShape(CORNER_RADIUS_DP.dp))
            .background(Color.DarkGray) // Black/Dark side background
            .drawBehind {
                // Draw the White side over the background
                drawRect(
                    color = Color.White,
                    size = size.copy(width = size.width * displayFraction)
                )
            },
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = evaluation?.format() ?: "0.0",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                // Text color flips based on the animated position of the bar
                color = if (isWhiteLeading) Color.DarkGray else Color.White,
                modifier = Modifier.weight(1f),
            )
            if (depth > 0) {
                Text(
                    text = "d$depth",
                    fontSize = 10.sp,
                    color = if (isWhiteLeading) Color.Gray else Color.LightGray,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EvaluationBarEqualPreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = Evaluation.Centipawns(0),
            depth = 15,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EvaluationBarWhiteAdvantagePreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = Evaluation.Centipawns(250),
            depth = 20,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EvaluationBarBlackAdvantagePreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = Evaluation.Centipawns(-150),
            depth = 12,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EvaluationBarMatePreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = Evaluation.Mate(3),
            depth = 25,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EvaluationBarNullPreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = null,
            depth = 0,
            modifier = Modifier.padding(16.dp),
        )
    }
}
