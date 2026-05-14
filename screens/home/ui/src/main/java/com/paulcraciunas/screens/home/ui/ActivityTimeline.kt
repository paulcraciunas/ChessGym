package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.Eyebrow
import com.paulcraciunas.screens.common.design.components.EyebrowType
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun ActivityTimeline(
    history: List<HomeUiState.HistoryGroup>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag { HomeScreenTags.Timeline.ROOT },
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
    ) {
        Text(
            text = stringResource(R.string.home_timeline_title),
            style = Design.typography.titleSmall,
            color = Design.colors.ink,
        )

        if (history.isEmpty()) {
            EmptyTimelineContent()
        } else {
            history.forEach { ActivityGroupItem(group = it) }
        }
    }
}

@Composable
private fun ActivityGroupItem(
    group: HomeUiState.HistoryGroup,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
    ) {
        Eyebrow(text = group.label, type = EyebrowType.Soft)
        group.events.forEach { TimelineEventItem(event = it) }
    }
}

@Composable
private fun EmptyTimelineContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(vertical = Design.dimensions.spacing.section)
            .testTag { HomeScreenTags.Timeline.EMPTY },
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
    ) {
        Text(
            text = stringResource(R.string.home_timeline_empty_title),
            style = Design.typography.titleMedium,
            color = Design.colors.ink,
        )
        Text(
            text = stringResource(R.string.home_timeline_empty_description),
            style = Design.typography.bodyMedium,
            color = Design.colors.inkSoft,
        )
    }
}

@Preview("ActivityTimeline", showBackground = true)
@Preview("ActivityTimeline (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ActivityTimelinePreview() {
    ChessGymTheme {
        ActivityTimeline(
            history = listOf(
                HomeUiState.HistoryGroup(
                    label = "Today",
                    events = listOf(
                        HomeUiState.HistoryEvent.PuzzleRushEvent(highScore = 18, runs = 5),
                        HomeUiState.HistoryEvent.BoardVizEvent(runs = 2)
                    )
                ),
                HomeUiState.HistoryGroup(
                    label = "Yesterday",
                    events = listOf(
                        HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = 42, count = 12),
                        HomeUiState.HistoryEvent.BlindModeEvent(ratingChange = -15, gamesPlayed = 3)
                    )
                ),
                HomeUiState.HistoryGroup(
                    label = "This Week",
                    events = listOf(
                        HomeUiState.HistoryEvent.PuzzleStreakEvent(finalStreakCount = 8),
                        HomeUiState.HistoryEvent.FailedPuzzleEvent(puzzlesSolved = 3)
                    )
                )
            ),
            modifier = Modifier.padding(Design.dimensions.spacing.xxl)
        )
    }
}

@Preview("ActivityTimeline Empty", showBackground = true)
@Composable
private fun ActivityTimelineEmptyPreview() {
    ChessGymTheme {
        ActivityTimeline(
            history = emptyList(),
            modifier = Modifier.padding(Design.dimensions.spacing.xxl)
        )
    }
}
