package com.paulcraciunas.screens.tools.importgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.paulcraciunas.domain.api.analysis.MoveClassification
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameUiState

@Composable
internal fun MoveList(
    moves: List<ImportGameUiState.AnalysedMove>,
    currentMoveIndex: Int,
    onMoveSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        moves.chunked(2).forEachIndexed { pairIndex, pair ->
            val moveNumber = pairIndex + 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$moveNumber.",
                    style = Design.typography.bodyLarge,
                    color = Design.colors.ink,
                    modifier = Modifier.width(Design.dimensions.sizes.moveHistoryNumber),
                )

                val whitePlyIdx = pairIndex * 2 + 1
                HalfMoveCell(
                    move = pair[0],
                    isSelected = currentMoveIndex == whitePlyIdx,
                    onClick = { onMoveSelected(whitePlyIdx) },
                    modifier = Modifier.weight(1f),
                )

                if (pair.size > 1) {
                    val blackPlyIdx = pairIndex * 2 + 2
                    HalfMoveCell(
                        move = pair[1],
                        isSelected = currentMoveIndex == blackPlyIdx,
                        onClick = { onMoveSelected(blackPlyIdx) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HalfMoveCell(
    move: ImportGameUiState.AnalysedMove,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        Design.colors.accent.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Design.dimensions.spacing.sm, vertical = Design.dimensions.spacing.xs)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs),
    ) {
        MiniEvalBar(
            fraction = move.normalised,
            modifier = Modifier.fillMaxHeight(),
        )

        Text(
            text = move.algebraic.annotated(move.classification),
            style = Design.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = classificationColor(move.classification),
        )
    }
}

@Composable
private fun MiniEvalBar(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(Design.dimensions.spacing.xs)
            .clip(Design.shapes.soft)
            .background(Design.colors.inkSubtle.copy(alpha = 0.2f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction.coerceIn(0f, 1f))
                .align(Alignment.BottomCenter)
                .background(Design.colors.surface),
        )
    }
}

@Composable
private fun classificationColor(classification: MoveClassification): Color = when (classification) {
    MoveClassification.Brilliant -> Design.colors.success
    MoveClassification.Great -> Design.colors.success
    MoveClassification.Good -> Design.colors.ink
    MoveClassification.Inaccuracy -> Design.colors.accent
    MoveClassification.Mistake -> Design.colors.danger.copy(alpha = 0.7f)
    MoveClassification.Blunder -> Design.colors.danger
}

private fun String.annotated(classification: MoveClassification): String = when (classification) {
    MoveClassification.Brilliant -> "$this!!"
    MoveClassification.Great -> "$this!"
    MoveClassification.Good -> this
    MoveClassification.Inaccuracy -> "$this?!"
    MoveClassification.Mistake -> "$this?"
    MoveClassification.Blunder -> "$this??"
}
