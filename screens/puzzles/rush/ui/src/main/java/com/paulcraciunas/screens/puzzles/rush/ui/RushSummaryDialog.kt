package com.paulcraciunas.screens.puzzles.rush.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun RushSummaryDialog(
    puzzlesSolved: Int,
    isNewHighScore: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.puzzle_rush_summary_title),
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
                    text = stringResource(R.string.puzzle_rush_summary_puzzles_solved),
                    style = Design.typography.bodyLarge,
                    color = Design.colors.inkSoft,
                )
                if (isNewHighScore) {
                    ChessGymSpacer(size = SpacerSize.XXLARGE)
                    Text(
                        text = stringResource(R.string.generic_new_high_score),
                        style = Design.typography.titleMedium,
                        color = Design.colors.accent,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_continue))
            }
        },
        modifier = modifier
    )
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun RushSummaryDialogPreview() {
    ChessGymTheme {
        RushSummaryDialog(
            puzzlesSolved = 12,
            isNewHighScore = false,
            onDismiss = {},
        )
    }
}

@Preview
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun RushSummaryDialogHighScorePreview() {
    ChessGymTheme {
        RushSummaryDialog(
            puzzlesSolved = 18,
            isNewHighScore = true,
            onDismiss = {},
        )
    }
}
