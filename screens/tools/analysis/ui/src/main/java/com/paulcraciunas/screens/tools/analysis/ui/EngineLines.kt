package com.paulcraciunas.screens.tools.analysis.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.tools.analysis.vm.AnalysisUiState

private const val EVAL_WIDTH_DP = 52

@Composable
internal fun EngineLines(
    lines: List<AnalysisUiState.EngineData.SuggestedLine>,
    modifier: Modifier = Modifier,
) {
    if (lines.isEmpty()) return
    Column(
        modifier = modifier.padding(horizontal = Design.dimensions.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxs),
    ) {
        lines.forEach { line ->
            // Use rank as a key for efficient list updates
            key(line.rank) {
                EngineLineRow(
                    line = line,
                    isTopLine = line.rank == 1,
                )
            }
        }
    }
}

@Composable
private fun EngineLineRow(
    line: AnalysisUiState.EngineData.SuggestedLine,
    isTopLine: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        EvaluationText(
            evaluation = line.evaluation,
            isTopLine = isTopLine,
            modifier = Modifier.width(EVAL_WIDTH_DP.dp)
        )
        Text(
            text = line.moves,
            fontSize = 13.sp,
            fontWeight = if (isTopLine) FontWeight.Medium else FontWeight.Normal,
            color = Design.colors.ink.copy(
                alpha = if (isTopLine) 1f else 0.7f
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EvaluationText(
    evaluation: AnalysisUiState.EngineData.CurrentEvaluation,
    isTopLine: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = evaluation.display,
        fontSize = 13.sp,
        fontWeight = if (isTopLine) FontWeight.Bold else FontWeight.Normal,
        color = if (isTopLine) Design.colors.primary else Design.colors.inkSoft,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EngineLinesPreview() {
    ChessGymTheme {
        EngineLines(
            lines = listOf(
                AnalysisUiState.EngineData.SuggestedLine(
                    rank = 1,
                    evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.6f, display = "+0.3"),
                    moves = "e2e4 e7e5 Nf3",
                ),
                AnalysisUiState.EngineData.SuggestedLine(
                    rank = 2,
                    evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.55f, display = "+0.2"),
                    moves = "d2d4 d7d5",
                ),
                AnalysisUiState.EngineData.SuggestedLine(
                    rank = 3,
                    evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.51f, display = "+0.1"),
                    moves = "g1f3 d7d5",
                ),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EngineLinesWithMatePreview() {
    ChessGymTheme {
        EngineLines(
            lines = listOf(
                AnalysisUiState.EngineData.SuggestedLine(
                    rank = 1,
                    evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.97f, display = "M3"),
                    moves = "e2e4 e7e5 Nf3",
                ),
                AnalysisUiState.EngineData.SuggestedLine(
                    rank = 2,
                    evaluation = AnalysisUiState.EngineData.CurrentEvaluation(normalised = 0.90f, display = "+8.2"),
                    moves = "d2d4 d7d5",
                ),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EngineLinesEmptyPreview() {
    ChessGymTheme {
        EngineLines(
            lines = emptyList(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
