package com.paulcraciunas.screens.tools.analysis.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulcraciunas.game.engine.api.EngineLine
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.engine.api.Evaluation
import com.paulcraciunas.game.engine.api.UciMoveParser
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

private const val EVAL_WIDTH_DP = 52

@Composable
internal fun EngineLines(
    lines: List<EngineLine>,
    modifier: Modifier = Modifier,
) {
    if (lines.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xxl),
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
    line: EngineLine,
    isTopLine: Boolean,
    modifier: Modifier = Modifier,
) {
    // Optimization: Only re-format moves when the list of moves actually changes
    val formattedMoves = remember(line.moves) {
        line.moves.joinToString(" ") { UciMoveParser.format(it) }
    }
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        EvaluationText(
            evaluation = line.evaluation,
            isTopLine = isTopLine,
            modifier = Modifier.width(EVAL_WIDTH_DP.dp)
        )
        Text(
            text = formattedMoves,
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
    evaluation: Evaluation,
    isTopLine: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = evaluationColor(evaluation, isTopLine)
    val text = remember(evaluation) { evaluation.format() }

    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = if (isTopLine) FontWeight.Bold else FontWeight.Normal,
        color = color,
        modifier = modifier,
    )
}

@Composable
private fun evaluationColor(evaluation: Evaluation, isTopLine: Boolean): Color {
    val isPositive = when (evaluation) {
        is Evaluation.Centipawns -> evaluation.value >= 0
        is Evaluation.Mate -> evaluation.movesToMate > 0
    }
    val baseColor = if (isPositive) Design.colors.primary else Design.colors.danger
    return if (isTopLine) baseColor else baseColor.copy(alpha = 0.7f)
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EngineLinesPreview() {
    ChessGymTheme {
        EngineLines(
            lines = listOf(
                EngineLine(1, Evaluation.Centipawns(45), sampleMoves("e2e4", "e7e5", "g1f3", "b8c6")),
                EngineLine(2, Evaluation.Centipawns(30), sampleMoves("d2d4", "d7d5", "c2c4")),
                EngineLine(3, Evaluation.Centipawns(12), sampleMoves("g1f3", "d7d5", "d2d4")),
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
                EngineLine(1, Evaluation.Mate(3), sampleMoves("d1h5", "f7f6", "d1f7")),
                EngineLine(2, Evaluation.Centipawns(850), sampleMoves("e2e4", "e7e5")),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

private fun sampleMoves(vararg uciMoves: String): List<EngineMove> =
    uciMoves.mapNotNull { UciMoveParser.parse(it) }

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
