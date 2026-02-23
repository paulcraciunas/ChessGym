package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.controls.RatingChangeChip
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun EventStats(
    event: HomeUiState.HistoryEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (event) {
            is HomeUiState.HistoryEvent.PuzzleRushEvent -> {
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_run, event.runs),
                    value = event.runs.toString()
                )
                StatChip(
                    label = stringResource(R.string.home_timeline_stat_score),
                    value = event.highScore.toString()
                )
            }
            is HomeUiState.HistoryEvent.BoardVizEvent -> {
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_session, event.runs),
                    value = event.runs.toString()
                )
            }
            is HomeUiState.HistoryEvent.RatedPuzzleEvent -> {
                RatingChangeChip(ratingChange = event.ratingChange)
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_puzzle, event.count),
                    value = event.count.toString()
                )
            }
            is HomeUiState.HistoryEvent.BlindModeEvent -> {
                RatingChangeChip(ratingChange = event.ratingChange)
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_game, event.gamesPlayed),
                    value = event.gamesPlayed.toString()
                )
            }
            is HomeUiState.HistoryEvent.BlindModeTrainingEvent -> {
                StatChip(
                    label = stringResource(R.string.home_timeline_stat_best_moves),
                    value = event.mostMovesCompleted.toString()
                )
                StatChip(
                    label = pluralStringResource(R.plurals.home_timeline_stat_session, event.runs),
                    value = event.runs.toString()
                )
            }
            is HomeUiState.HistoryEvent.PuzzleStreakEvent -> {
                StatChip(
                    label = stringResource(R.string.home_timeline_stat_streak),
                    value = event.finalStreakCount.toString()
                )
            }
            is HomeUiState.HistoryEvent.FailedPuzzleEvent -> {
                StatChip(
                    label = stringResource(R.string.home_timeline_stat_solved),
                    value = event.puzzlesSolved.toString()
                )
            }
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

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsPuzzleRushPreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.PuzzleRushEvent(highScore = 23, runs = 5)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsBoardVizPreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.BoardVizEvent(runs = 3)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsRatedPuzzlePositivePreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = 42, count = 12)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsRatedPuzzleNegativePreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = -15, count = 8)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsBlindModePreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.BlindModeEvent(ratingChange = -10, gamesPlayed = 3)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsBlindModeTrainingPreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.BlindModeTrainingEvent(mostMovesCompleted = 15, runs = 2)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsPuzzleStreakPreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.PuzzleStreakEvent(finalStreakCount = 8)
        )
    }
}

@Preview(showBackground = true)
@Preview("Dark mode", showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EventStatsFailedPuzzlePreview() {
    ChessGymTheme {
        EventStats(
            event = HomeUiState.HistoryEvent.FailedPuzzleEvent(puzzlesSolved = 3)
        )
    }
}
