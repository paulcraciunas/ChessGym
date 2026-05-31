package com.paulcraciunas.screens.puzzles.rated.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymChip
import com.paulcraciunas.screens.common.design.components.PlayButton
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

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
            .padding(
                horizontal = Design.dimensions.spacing.xxl,
                vertical = Design.dimensions.spacing.sm,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Absolute.Left,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChessGymChip(ratingChange = if (success) ratingChange else -ratingChange)
            Text(
                text = stringResource(if (success) R.string.generic_success else R.string.generic_failed),
                style = Design.typography.titleMedium,
                color = if (success) Design.colors.success else Design.colors.danger,
                modifier = Modifier
                    .padding(horizontal = Design.dimensions.spacing.md)
                    .testTag { RatedPuzzleScreenTags.Finished.RESULT_TEXT }
            )
        }

        PlayButton(
            onClick = onPlayNext,
            modifier = Modifier.testTag { RatedPuzzleScreenTags.Finished.PLAY_NEXT },
            textId = R.string.rated_puzzle_next_description,
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
