package com.paulcraciunas.screens.puzzles.failed.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun FailedPuzzlesCompletionDialog(
    puzzlesSolved: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.failed_puzzles_complete_title),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = puzzlesSolved.toString(),
                    style = Design.typography.displayLarge,
                    color = Design.colors.primary,
                )
                ChessGymSpacer(size = SpacerSize.SMALL)
                Text(
                    text = pluralStringResource(
                        R.plurals.failed_puzzles_solved_count,
                        puzzlesSolved,
                        puzzlesSolved
                    ),
                    style = Design.typography.bodyLarge,
                    color = Design.colors.inkSoft,
                )
                ChessGymSpacer()
                Text(
                    text = stringResource(R.string.failed_puzzles_complete_message),
                    style = Design.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Design.colors.inkSoft,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag { FailedPuzzlesScreenTags.Completion.DISMISS }
            ) {
                Text(stringResource(R.string.generic_continue))
            }
        },
        modifier = modifier.testTag { FailedPuzzlesScreenTags.Completion.DIALOG }
    )
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FailedPuzzlesCompletionDialogPreview() {
    ChessGymTheme {
        FailedPuzzlesCompletionDialog(
            puzzlesSolved = 8,
            onDismiss = {},
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FailedPuzzlesCompletionDialogAllSolvedPreview() {
    ChessGymTheme {
        FailedPuzzlesCompletionDialog(
            puzzlesSolved = 10,
            onDismiss = {},
        )
    }
}
