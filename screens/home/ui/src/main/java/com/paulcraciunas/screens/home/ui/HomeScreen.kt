package com.paulcraciunas.screens.home.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.TopLevelAppBar
import com.paulcraciunas.screens.common.design.theme.Design
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
            TopLevelAppBar(
                onHome = onDrawerToggle,
                actions = { AchievementsBadge(unseenCount = state.unseenAchievementCount, onClick = onAchievements) }
            )
        },
        containerColor = Design.colors.bg,
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
                    .padding(top = innerPadding.calculateTopPadding())
                    .testTag { HomeScreenTags.SCREEN }
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = Design.colors.primarySoft
    LazyColumn(
        contentPadding = PaddingValues(Design.dimensions.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        item { UserProfileCard(userProfile = uiState.userProfile, ribbons = uiState.ribbons) }
        item {
            UserStatsCard(
                title = stringResource(R.string.user_stats_title),
                stats = uiState.userStats
            )
        }
        item {
            HighScoresCard(
                title = stringResource(R.string.user_stats_high_score_title),
                stats = uiState.userStats
            )
        }

        item {
            Text(
                text = stringResource(R.string.home_timeline_title),
                style = Design.typography.titleSmall,
                color = Design.colors.ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag { HomeScreenTags.Timeline.ROOT },
            )
        }

        if (uiState.history.isEmpty()) {
            item {
                EmptyTimelineContent()
            }
        } else {
            uiState.history.forEach {
                item {
                    ActivityGroupItem(group = it)
                }
            }
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
        val achievementsDescription = stringResource(R.string.achievement_screen_title)
        if (unseenCount > 0) {
            BadgedBox(
                badge = {
                    Badge { Text(text = unseenCount.toString()) }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = achievementsDescription,
                    modifier = Modifier.size(Design.dimensions.spacing.xgut),
                    tint = Design.colors.primary,
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = achievementsDescription,
                modifier = Modifier.size(Design.dimensions.spacing.xgut),
                tint = Design.colors.primary,
            )
        }
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
