package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun TimelineEventItem(
    event: HomeUiState.HistoryEvent,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimelineConnector(
            event = event,
            isLast = isLast
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(event.titleRes()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            // Event stats
            EventStats(event = event)
        }
    }
}

@Composable
private fun TimelineConnector(
    event: HomeUiState.HistoryEvent,
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
                painter = painterResource(event.iconRes()),
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

@DrawableRes
private fun HomeUiState.HistoryEvent.iconRes(): Int = when (this) {
    is HomeUiState.HistoryEvent.PuzzleRushEvent -> R.drawable.puzzle_rush_icon
    is HomeUiState.HistoryEvent.BoardVizEvent -> R.drawable.board_visualization_icon
    is HomeUiState.HistoryEvent.BlindModeEvent -> R.drawable.blind_mode_icon
    is HomeUiState.HistoryEvent.BlindModeTrainingEvent -> R.drawable.blind_mode_icon
    is HomeUiState.HistoryEvent.RatedPuzzleEvent -> R.drawable.puzzle_icon
}

@StringRes
private fun HomeUiState.HistoryEvent.titleRes(): Int = when (this) {
    is HomeUiState.HistoryEvent.PuzzleRushEvent -> R.string.home_title_puzzle_rush
    is HomeUiState.HistoryEvent.BoardVizEvent -> R.string.home_title_board_visualization
    is HomeUiState.HistoryEvent.BlindModeEvent -> R.string.home_title_blind_mode
    is HomeUiState.HistoryEvent.BlindModeTrainingEvent -> R.string.home_title_blind_mode_training
    is HomeUiState.HistoryEvent.RatedPuzzleEvent -> R.string.home_title_rated_puzzle
}

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
                event = HomeUiState.HistoryEvent.PuzzleRushEvent(highScore = 18, runs = 5),
                isLast = false
            )

            TimelineEventItem(
                event = HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = 42, count = 12),
                isLast = false
            )

            TimelineEventItem(
                event = HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = -15, count = 3),
                isLast = false
            )

            TimelineEventItem(
                event = HomeUiState.HistoryEvent.BlindModeEvent(ratingChange = -15, gamesPlayed = 3),
                isLast = false
            )

            TimelineEventItem(
                event = HomeUiState.HistoryEvent.BoardVizEvent(runs = 2),
                isLast = true
            )
        }
    }
}
