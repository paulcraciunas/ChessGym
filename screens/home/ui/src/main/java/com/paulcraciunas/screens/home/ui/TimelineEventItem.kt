package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun TimelineEventItem(
    event: HomeUiState.ActivityEvent,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimelineConnector(
            activityType = event.type,
            isLast = isLast
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Event header with title and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = event.timestamp.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Event stats (if available)
            if (event.score != null || event.count != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    event.score?.let { score ->
                        StatChip(
                            label = "Score",
                            value = score.toString()
                        )
                    }
                    event.count?.let { count ->
                        StatChip(
                            label = if (count == 1) "Run" else "Runs",
                            value = count.toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineConnector(
    activityType: HomeUiState.ActivityType,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Activity icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(activityType.iconRes()),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Vertical line (if not last)
        if (!isLast) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@DrawableRes
private fun HomeUiState.ActivityType.iconRes(): Int = when (this) {
    HomeUiState.ActivityType.PUZZLE_RUSH -> R.drawable.puzzle_rush_icon
    HomeUiState.ActivityType.BOARD_VISUALIZATION -> R.drawable.board_visualization_icon
    HomeUiState.ActivityType.BLIND_MODE -> R.drawable.blind_mode_icon
    HomeUiState.ActivityType.RATED_PUZZLE -> R.drawable.puzzle_icon
    HomeUiState.ActivityType.TRAINING_SESSION -> R.drawable.puzzle_icon
}

private val formatter = DateTimeFormatter.ofPattern("h:mm a")

@Preview("TimelineEventItem")
@Preview("TimelineEventItem (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TimelineEventItemPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimelineEventItem(
                event = HomeUiState.ActivityEvent(
                    id = "1",
                    type = HomeUiState.ActivityType.PUZZLE_RUSH,
                    title = "Puzzle Rush",
                    description = "Completed 5 runs with best score 18",
                    timestamp = LocalDateTime.now().minusHours(2),
                    score = 18,
                    count = 5
                ),
                isLast = false
            )

            TimelineEventItem(
                event = HomeUiState.ActivityEvent(
                    id = "2",
                    type = HomeUiState.ActivityType.BOARD_VISUALIZATION,
                    title = "Board Visualization",
                    description = "Completed 2 sessions",
                    timestamp = LocalDateTime.now().minusHours(4),
                    count = 2
                ),
                isLast = true
            )
        }
    }
} 