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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.AppBarAlignment
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.backgroundColor
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit = {},
    onAchievements: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppBar(
                titleAlign = AppBarAlignment.Center,
                navButton = { Home(onClick = onDrawerToggle) },
                actions = {
                    AchievementsBadge(
                        unseenCount = state.unseenAchievementCount,
                        onClick = onAchievements,
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(
                Modifier
                    .padding(innerPadding)
                    .testTag { HomeScreenTags.LOADING }
            )
            else -> HomeContent(
                uiState = state,
                modifier = Modifier
                    .padding(innerPadding)
                    .testTag { HomeScreenTags.SCREEN }
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .semantics { this.backgroundColor = backgroundColor }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            item { UserProfileCard(userProfile = uiState.userProfile, ribbons = uiState.ribbons) }
            item { StatsSection(stats = uiState.userStats) }
            item { ActivityTimeline(history = uiState.history) }
        }
    }
}

@Composable
private fun AchievementsBadge(
    unseenCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (unseenCount > 0) {
            BadgedBox(
                badge = {
                    Badge { Text(text = unseenCount.toString()) }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.award_star_icon),
                    contentDescription = "Achievements",
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.award_star_icon),
                contentDescription = "Achievements",
                modifier = Modifier.size(24.dp),
            )
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
        verticalArrangement = Arrangement.SpaceEvenly
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
                    bestPuzzleStreakScore = 15,
                    bestFindTheSquareScore = 42,
                    bestMoveThePieceScore = 18,
                    bestBlindModeScore = 8,
                ),
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
                    )
                ),
                isLoading = false
            )
        )
    }
}
