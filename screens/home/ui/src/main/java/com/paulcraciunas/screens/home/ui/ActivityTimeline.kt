package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun ActivityTimeline(
    activityGroups: List<HomeUiState.ActivityGroup>,
    modifier: Modifier = Modifier
) {
    val innerPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(innerPadding)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.home_timeline_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            // Timeline Content
            if (activityGroups.isEmpty()) {
                EmptyTimelineContent()
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    activityGroups.forEachIndexed { index, group ->
                        ActivityGroupItem(
                            group = group,
                            isLast = index == activityGroups.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityGroupItem(
    group: HomeUiState.ActivityGroup,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Header
        Text(
            text = formatDate(group.date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        // Activity Events
        group.activities.forEachIndexed { index, activity ->
            TimelineEventItem(
                event = activity,
                isLast = index == group.activities.lastIndex && isLast
            )
        }
    }
}

@Composable
private fun EmptyTimelineContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.home_timeline_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = stringResource(R.string.home_timeline_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@Stable
private fun formatDate(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return when (date) {
        today -> stringResource(R.string.generic_today)
        yesterday -> stringResource(R.string.generic_yesterday)
        else -> {
            date.format(formatter)
        }
    }
}

private val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")

@Preview("ActivityTimeline")
@Preview("ActivityTimeline (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ActivityTimelinePreview() {
    ChessGymTheme {
        ActivityTimeline(
            activityGroups = listOf(
                HomeUiState.ActivityGroup(
                    date = LocalDate.now(),
                    activities = listOf(
                        HomeUiState.ActivityEvent(
                            id = "1",
                            type = HomeUiState.ActivityType.PUZZLE_RUSH,
                            title = "Puzzle Rush",
                            description = "Completed 5 runs with best score 18",
                            timestamp = LocalDateTime.now().minusHours(2),
                            score = 18,
                            count = 5
                        ),
                        HomeUiState.ActivityEvent(
                            id = "2",
                            type = HomeUiState.ActivityType.BOARD_VISUALIZATION,
                            title = "Board Visualization",
                            description = "Completed 2 sessions",
                            timestamp = LocalDateTime.now().minusHours(4),
                            count = 2
                        )
                    )
                ),
                HomeUiState.ActivityGroup(
                    date = LocalDate.now().minusDays(1),
                    activities = listOf(
                        HomeUiState.ActivityEvent(
                            id = "3",
                            type = HomeUiState.ActivityType.RATED_PUZZLE,
                            title = "Rated Puzzles",
                            description = "Solved 12 puzzles, rating improved",
                            timestamp = LocalDateTime.now().minusDays(1),
                            count = 12
                        )
                    )
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("ActivityTimeline Empty")
@Composable
private fun ActivityTimelineEmptyPreview() {
    ChessGymTheme {
        ActivityTimeline(
            activityGroups = emptyList(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
