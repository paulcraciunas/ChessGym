package com.paulcraciunas.screens.puzzles.streak.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.controls.RefreshButton
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@Composable
internal fun StreakEndedControls(
    finalStreakCount: Int,
    onNewStreak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Design.dimensions.spacing.xxl, vertical = Design.dimensions.spacing.sm)
            .testTag { PuzzleStreakScreenTags.Ended.CONTROLS },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak summary
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.puzzle_streak_ended),
                    style = Design.typography.titleMedium,
                    color = Design.colors.danger,
                )
                ChessGymSpacer(size = SpacerSize.LARGE)
                Icon(
                    painter = painterResource(R.drawable.puzzle_rush_icon),
                    contentDescription = null,
                    tint = Design.colors.primary,
                    modifier = Modifier.size(Design.dimensions.sizes.icon)
                )
                ChessGymSpacer(size = SpacerSize.SMALL)
                Text(
                    text = finalStreakCount.toString(),
                    style = Design.typography.titleLarge,
                    color = Design.colors.primary,
                )
            }

            RefreshButton(
                onClick = onNewStreak,
                text = R.string.puzzle_streak_new,
                modifier = Modifier.testTag { PuzzleStreakScreenTags.Ended.NEW_STREAK }
            )
        }
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StreakEndedControlsPreview() {
    ChessGymTheme {
        StreakEndedControls(
            finalStreakCount = 15,
            onNewStreak = {},
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StreakEndedControlsZeroPreview() {
    ChessGymTheme {
        StreakEndedControls(
            finalStreakCount = 0,
            onNewStreak = {},
        )
    }
}
