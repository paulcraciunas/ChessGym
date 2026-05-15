package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymCard
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun TimelineEventItem(
    event: HomeUiState.HistoryEvent,
    modifier: Modifier = Modifier,
) {
    ChessGymCard(contentPadding = PaddingValues(Design.dimensions.spacing.lg)) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(Design.shapes.circle)
                    .background(Design.colors.primarySoft)
                    .border(
                        width = 2.dp,
                        color = Design.colors.primary,
                        shape = Design.shapes.circle,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(event.iconRes()),
                    contentDescription = null,
                    tint = Design.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.xs)
            ) {
                Text(
                    text = stringResource(event.titleRes()),
                    style = Design.typography.titleSmall,
                    color = Design.colors.ink,
                )
                EventStats(event = event)
            }
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
    is HomeUiState.HistoryEvent.PuzzleStreakEvent -> R.drawable.puzzle_streak_icon
    is HomeUiState.HistoryEvent.FailedPuzzleEvent -> R.drawable.retry_icon
}

@StringRes
private fun HomeUiState.HistoryEvent.titleRes(): Int = when (this) {
    is HomeUiState.HistoryEvent.PuzzleRushEvent -> R.string.home_title_puzzle_rush
    is HomeUiState.HistoryEvent.BoardVizEvent -> R.string.home_title_board_visualization
    is HomeUiState.HistoryEvent.BlindModeEvent -> R.string.home_title_blind_mode
    is HomeUiState.HistoryEvent.BlindModeTrainingEvent -> R.string.home_title_blind_mode_training
    is HomeUiState.HistoryEvent.RatedPuzzleEvent -> R.string.home_title_rated_puzzle
    is HomeUiState.HistoryEvent.PuzzleStreakEvent -> R.string.home_title_puzzle_streak
    is HomeUiState.HistoryEvent.FailedPuzzleEvent -> R.string.home_title_failed_puzzles
}

@Preview("TimelineEventItem")
@Preview("TimelineEventItem (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TimelineEventItemPreview() {
    ChessGymTheme {
        Column(
            modifier = Modifier.padding(Design.dimensions.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
        ) {
            TimelineEventItem(event = HomeUiState.HistoryEvent.PuzzleRushEvent(highScore = 18, runs = 5))
            TimelineEventItem(event = HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = 42, count = 12))
            TimelineEventItem(event = HomeUiState.HistoryEvent.PuzzleStreakEvent(finalStreakCount = 8))
            TimelineEventItem(event = HomeUiState.HistoryEvent.FailedPuzzleEvent(puzzlesSolved = 3))
            TimelineEventItem(event = HomeUiState.HistoryEvent.BoardVizEvent(runs = 2))
        }
    }
}
