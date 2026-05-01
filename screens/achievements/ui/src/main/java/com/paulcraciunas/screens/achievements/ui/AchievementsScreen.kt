package com.paulcraciunas.screens.achievements.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementsInteractor
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.achievements.vm.StubAchievementsInteractor
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    state: AchievementsUiState,
    interactions: AchievementsInteractor,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.achievement_screen_title),
                navButton = { Back(onClick = onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            state.isError -> FailedContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            else -> {
                AchievementsContent(
                    achievements = state.achievements,
                    interactions = interactions,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}

@Composable
private fun AchievementsContent(
    achievements: List<AchievementState>,
    interactions: AchievementsInteractor,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        interactions.onScreenVisible()
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(achievements, key = { it.achievement.name }) { achievement ->
            AchievementCard(item = achievement)
        }
    }
}

@Preview("Achievements")
@Preview("Achievements (dark)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AchievementsScreenPreview() {
    ChessGymTheme {
        AchievementsScreen(
            state = AchievementsUiState(
                isLoading = false,
                achievements = listOf(
                    AchievementState.Complete(
                        achievement = Achievement.RATING_CLIMBER,
                        unseen = false,
                    ),
                    AchievementState.Earned(
                        achievement = Achievement.RATED_PUZZLES_SOLVED,
                        unseen = true,
                        currentTier = Achievement.Tier.THREE,
                        currentProgress = 147,
                        nextThreshold = 250,
                    ),
                    AchievementState.Unearned(
                        achievement = Achievement.PUZZLE_RUSH_SESSIONS,
                        currentProgress = 2,
                        nextThreshold = 5,
                    ),
                    AchievementState.Unearned(
                        achievement = Achievement.BLIND_MODE_WINS,
                        currentProgress = 0,
                        nextThreshold = 1,
                    ),
                )
            ),
            interactions = StubAchievementsInteractor(),
        )
    }
}
