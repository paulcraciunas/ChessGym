package com.paulcraciunas.screens.puzzles.failed.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymDialog
import com.paulcraciunas.screens.common.testTag

@Composable
fun FailedPuzzlesCompletionDialog(
    puzzlesSolved: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChessGymDialog(
        onDismissRequest = onDismiss,
        title = {
            EyebrowTitle(
                eyebrow = stringResource(R.string.failed_puzzles_complete_title),
                title = stringResource(R.string.puzzle_mode_failed_title),
            )
        },
        buttons = {
            Primary(
                text = stringResource(R.string.generic_continue),
                onClick = onDismiss,
            )
        },
        modifier = modifier.testTag { FailedPuzzlesScreenTags.Completion.DIALOG },
    ) {
        Column {
            Summary(
                value = puzzlesSolved.toString(),
                subtitle = stringResource(R.string.user_stat_puzzles_solved),
            )
            CenteredMessage(text = stringResource(R.string.failed_puzzles_complete_message))
        }
    }
}
