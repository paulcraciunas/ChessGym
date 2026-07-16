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
    puzzlesTotal: Int,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
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
            Paired(
                confirmText = stringResource(R.string.generic_retry),
                onConfirm = onRetry,
                dismissText = stringResource(R.string.generic_continue),
                onDismiss = onDismiss,
            )
        },
        modifier = modifier.testTag { FailedPuzzlesScreenTags.Completion.DIALOG },
    ) {
        Column {
            Summary(
                value = stringResource(R.string.failed_puzzles_complete_summary, puzzlesSolved, puzzlesTotal),
                subtitle = stringResource(R.string.user_stat_puzzles_solved),
            )
            CenteredMessage(
                text = stringResource(
                    if (puzzlesSolved > 0) R.string.failed_puzzles_complete_message
                    else R.string.failed_puzzles_complete_message_none_solved
                )
            )
        }
    }
}
