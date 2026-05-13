package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.design.components.ChessGymChip
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState

@Composable
internal fun EventStats(
    event: HomeUiState.HistoryEvent,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm)
    ) {
        when (event) {
            is HomeUiState.HistoryEvent.PuzzleRushEvent -> {
                ChessGymChip(text = pluralStringResource(R.plurals.home_timeline_stat_run, event.runs, event.runs))
                ChessGymChip(text = stringResource(R.string.home_timeline_stat_score, event.highScore))
            }
            is HomeUiState.HistoryEvent.BoardVizEvent -> {
                ChessGymChip(text = pluralStringResource(R.plurals.home_timeline_stat_session, event.runs, event.runs))
            }
            is HomeUiState.HistoryEvent.RatedPuzzleEvent -> {
                ChessGymChip(ratingChange = event.ratingChange)
                ChessGymChip(text = pluralStringResource(R.plurals.home_timeline_stat_puzzle, event.count, event.count))
            }
            is HomeUiState.HistoryEvent.BlindModeEvent -> {
                ChessGymChip(ratingChange = event.ratingChange)
                ChessGymChip(text = pluralStringResource(R.plurals.home_timeline_stat_game, event.gamesPlayed, event.gamesPlayed))
            }
            is HomeUiState.HistoryEvent.BlindModeTrainingEvent -> {
                ChessGymChip(text = stringResource(R.string.home_timeline_stat_best_moves, event.mostMovesCompleted))
                ChessGymChip(text = pluralStringResource(R.plurals.home_timeline_stat_session, event.runs, event.runs))
            }
            is HomeUiState.HistoryEvent.PuzzleStreakEvent -> {
                ChessGymChip(text = stringResource(R.string.home_timeline_stat_streak, event.finalStreakCount))
            }
            is HomeUiState.HistoryEvent.FailedPuzzleEvent -> {
                ChessGymChip(text = stringResource(R.string.home_timeline_stat_solved, event.puzzlesSolved))
            }
        }
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
