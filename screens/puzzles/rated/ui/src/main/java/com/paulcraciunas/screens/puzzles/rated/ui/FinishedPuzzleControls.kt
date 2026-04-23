package com.paulcraciunas.screens.puzzles.rated.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.controls.PlayButton
import com.paulcraciunas.screens.common.controls.RatingChangeChip
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.common.theme.LoadingTheme

@Composable
internal fun FinishedPuzzleControls(
    success: Boolean,
    ratingChange: Int,
    modifier: Modifier = Modifier,
    onPlayNext: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Absolute.Left,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Finished state: rating change + navigation controls + play next
            RatingChangeChip(
                ratingChange = if (success) ratingChange else -ratingChange,
                iconSize = 24.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag { RatedPuzzleScreenTags.Finished.RATING_CHANGE }
            )
            Text(
                text = stringResource(if (success) R.string.generic_success else R.string.generic_failed),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (success) {
                    LoadingTheme.colors.success
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .testTag { RatedPuzzleScreenTags.Finished.RESULT_TEXT }
            )
        }

        PlayButton(
            onClick = onPlayNext,
            text = R.string.rated_puzzle_next_description,
            modifier = Modifier.testTag { RatedPuzzleScreenTags.Finished.PLAY_NEXT }
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleControlsSolvedPreview() {
    ChessGymTheme {
        FinishedPuzzleControls(
            success = true,
            ratingChange = 15,
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PuzzleControlsFailedPreview() {
    ChessGymTheme {
        FinishedPuzzleControls(
            success = false,
            ratingChange = 12,
        )
    }
}
