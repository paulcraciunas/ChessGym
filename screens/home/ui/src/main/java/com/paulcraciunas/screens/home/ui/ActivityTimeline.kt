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
import java.time.format.DateTimeFormatter

@Composable
internal fun ActivityTimeline(
    history: List<HomeUiState.HistoryGroup>,
    modifier: Modifier = Modifier
) {
    val innerPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
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
            Text(
                text = stringResource(R.string.home_timeline_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            if (history.isEmpty()) {
                EmptyTimelineContent()
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    history.forEachIndexed { index, group ->
                        ActivityGroupItem(
                            group = group,
                            isLast = index == history.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityGroupItem(
    group: HomeUiState.HistoryGroup,
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
        group.events.forEachIndexed { index, event ->
            TimelineEventItem(
                event = event,
                isLast = index == group.events.lastIndex && isLast
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
            history = listOf(
                HomeUiState.HistoryGroup(
                    date = LocalDate.now(),
                    events = listOf(
                        HomeUiState.HistoryEvent.PuzzleRushEvent(highScore = 18, runs = 5),
                        HomeUiState.HistoryEvent.BoardVizEvent(runs = 2)
                    )
                ),
                HomeUiState.HistoryGroup(
                    date = LocalDate.now().minusDays(1),
                    events = listOf(
                        HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = 42, count = 12),
                        HomeUiState.HistoryEvent.BlindModeEvent(ratingChange = -15, gamesPlayed = 3)
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
            history = emptyList(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
