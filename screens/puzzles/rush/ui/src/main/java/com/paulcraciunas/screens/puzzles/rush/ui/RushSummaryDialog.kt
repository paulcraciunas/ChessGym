package com.paulcraciunas.screens.puzzles.rush.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog

@Composable
internal fun RushSummaryDialog(
    puzzlesSolved: Int,
    isNewHighScore: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = {
            EyebrowTitle(
                eyebrow = stringResource(R.string.puzzle_mode_rush_title),
                title = stringResource(R.string.puzzle_rush_summary_title),
            )
        },
        buttons = {
            Primary(
                text = stringResource(R.string.generic_continue),
                onClick = onDismiss,
            )
        },
        modifier = modifier,
    ) {
        Summary(
            value = puzzlesSolved.toString(),
            subtitle = stringResource(R.string.puzzle_rush_summary_puzzles_solved),
        )
        if (isNewHighScore) {
            HighScoreBadge(text = stringResource(R.string.generic_new_high_score))
        }
    }
}
