package com.paulcraciunas.screens.puzzles.streak.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.testTag

@Composable
internal fun StreakSummaryDialog(
    streakCount: Int,
    isNewHighScore: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = {
            EyebrowTitle(
                eyebrow = stringResource(R.string.puzzle_mode_streak_title),
                title = stringResource(R.string.puzzle_streak_summary_title),
            )
        },
        confirmButton = {
            Primary(
                text = stringResource(R.string.generic_continue),
                onClick = onDismiss,
                modifier = Modifier.testTag { PuzzleStreakScreenTags.Summary.DISMISS },
            )
        },
        modifier = modifier.testTag { PuzzleStreakScreenTags.Summary.DIALOG },
    ) {
        Summary(
            value = streakCount.toString(),
            subtitle = stringResource(R.string.puzzle_streak_summary_puzzles_solved),
        )
        if (isNewHighScore) {
            HighScoreBadge(text = stringResource(R.string.generic_new_high_score))
        }
    }
}
