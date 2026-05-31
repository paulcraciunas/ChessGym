package com.paulcraciunas.screens.common.controls

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.data.PuzzleResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PuzzleResultsGrid(
    results: List<PuzzleResult>,
    modifier: Modifier = Modifier,
    onFailedPuzzleClicked: (Int) -> Unit = {},
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
    ) {
        results.forEach { result ->
            PuzzleResultItem(
                result = result,
                onClick = onFailedPuzzleClicked,
            )
        }
    }
}

@Composable
private fun PuzzleResultItem(
    result: PuzzleResult,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (result.success) {
        Design.colors.chipSolvedBg
    } else {
        Design.colors.danger.copy(alpha = 0.15f)
    }

    val iconColor = if (result.success) {
        Design.colors.success
    } else {
        Design.colors.danger
    }

    Box(
        modifier = modifier
            .clip(Design.shapes.cardCompact)
            .then(if (!result.success && result.id != null) Modifier.clickable { onClick(result.id!!) } else Modifier)
            .background(backgroundColor)
            .padding(Design.dimensions.spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xxs)
        ) {
            Icon(
                imageVector = if (result.success) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(Design.dimensions.sizes.icon)
            )
            Text(
                text = result.rating.toString(),
                style = Design.textStyles.monoSmall,
                color = Design.colors.ink,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleResultsGridPreview() {
    ChessGymTheme {
        PuzzleResultsGrid(
            results = listOf(
                PuzzleResult(id = 1, rating = 1200, success = true),
                PuzzleResult(id = 2, rating = 1250, success = true),
                PuzzleResult(id = 3, rating = 1300, success = false),
                PuzzleResult(id = 4, rating = 1280, success = true),
                PuzzleResult(id = 5, rating = 1320, success = true),
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleResultsGridManyPreview() {
    ChessGymTheme {
        PuzzleResultsGrid(
            results = (1..15).map { i ->
                PuzzleResult(id = i, rating = 1100 + i * 25, success = i % 4 != 0)
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PuzzleResultItemSuccessPreview() {
    ChessGymTheme {
        PuzzleResultItem(
            result = PuzzleResult(id = 1, rating = 1350, success = true),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PuzzleResultItemFailedPreview() {
    ChessGymTheme {
        PuzzleResultItem(
            result = PuzzleResult(id = 2, rating = 1400, success = false),
            onClick = {},
        )
    }
}
