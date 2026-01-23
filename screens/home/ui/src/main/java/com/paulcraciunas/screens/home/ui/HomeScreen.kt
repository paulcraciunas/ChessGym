package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.AppBarAlignment
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppBar(
                titleAlign = AppBarAlignment.Center,
                navButton = { Home(onClick = onDrawerToggle) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(Modifier.padding(innerPadding))
            else -> HomeContent(uiState = state, modifier = Modifier.padding(innerPadding))
        }
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
            item { ActivityTimeline(history = uiState.history) }
        }
    }
}

@Composable
private fun StatsSection(
    stats: HomeUiState.Stats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                isLoading = false
            )
        )
    }
}
