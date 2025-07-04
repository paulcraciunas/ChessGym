package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingContent(modifier)
        else -> HomeContent(uiState = state, modifier = modifier)
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { UserProfileCard(userProfile = uiState.userProfile) }
            item { StatsSection(stats = uiState.userStats) }
            item { ActivityTimeline(activityGroups = uiState.activityHistory) }
        }
    }
}

@Composable
private fun StatsSection(
    stats: HomeUiState.Stats,
) {
    UserStatsCard(
        title = stringResource(R.string.user_stats_title),
        stats = stats
    )
    HighScoresCard(
        title = stringResource(R.string.user_stats_high_score_title),
        stats = stats
    )
}

@Preview("HomeScreen")
@Preview("HomeScreen (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    ChessGymTheme {
        HomeScreen(
            state = HomeUiState(
                userProfile = HomeUiState.UserProfile(
                    name = "John Doe",
                    currentRating = 1547,
                    totalActivities = 142,
                    joinDate = LocalDate.of(2024, 3, 15)
                ),
                userStats = HomeUiState.Stats(
                    puzzlesPlayed = 142,
                    puzzlesSolved = 108,
                    currentRating = 1547,
                    bestRating = 1623,
                    bestPuzzleRushScore = 23,
                    bestBlindModeScore = 8,
                    bestVisualizationScore = 12
                ),
                activityHistory = listOf(
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
                    )
                ),
                isLoading = false
            )
        )
    }
}
