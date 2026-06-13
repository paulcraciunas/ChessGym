package com.paulcraciunas.screens.tools.analysis.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.analysis.vm.AnalysisUiState

@Composable
internal fun EvaluationBar(
    evaluation: AnalysisUiState.EngineData.CurrentEvaluation?,
    depth: Int,
    modifier: Modifier = Modifier,
) {
    val targetFraction = remember(evaluation) {
        evaluation?.normalised ?: 0.5f
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "eval_bar_animation",
    )
    val isWhiteLeading = animatedFraction >= 0.5f
    Row(
        modifier = modifier
            .height(Design.dimensions.sizes.evaluationBar)
            .clip(Design.shapes.soft)
            .background(Color.DarkGray) // Black/Dark side background
            .drawBehind {
                // Draw the White side over the background
                drawRect(
                    color = Color.White,
                    size = size.copy(width = size.width * animatedFraction)
                )
            }
            .padding(horizontal = Design.dimensions.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = evaluation?.display ?: "0.0",
            style = Design.textStyles.eyebrowLarge,
            // Text color flips based on the animated position of the bar
            color = if (isWhiteLeading) Color.DarkGray else Color.White,
            modifier = Modifier.weight(1f),
        )
        if (depth > 0) {
            Text(
                text = "d$depth",
                style = Design.textStyles.label,
                color = if (isWhiteLeading) Color.Gray else Color.LightGray,
            )
        }
    }
}

private const val ANIMATION_DURATION_MS = 300

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EvaluationBarEqualPreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(0.5f, "+0.0"),
            depth = 15,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EvaluationBarWhiteAdvantagePreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(0.75f, "+2.5"),
            depth = 20,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EvaluationBarBlackAdvantagePreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(0.25f, "-2.5"),
            depth = 12,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EvaluationBarMatePreview() {
    ChessGymTheme {
        EvaluationBar(
            evaluation = AnalysisUiState.EngineData.CurrentEvaluation(1.0f, "M3"),
            depth = 25,
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
        )
    }
}
