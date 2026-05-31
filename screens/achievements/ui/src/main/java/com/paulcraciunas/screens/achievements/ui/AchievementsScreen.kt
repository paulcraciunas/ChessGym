package com.paulcraciunas.screens.achievements.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.paulcraciunas.screens.achievements.vm.AchievementCategory
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.CategoryGroup
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.TrophyCaseSummary
import com.paulcraciunas.screens.common.ChildAppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.theme.ChessGymTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    state: AchievementsUiState,
    modifier: Modifier = Modifier,
    onScreenVisible: () -> Unit = {},
    onAchievementClicked: (AchievementState) -> Unit = {},
    onDismissDetail: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            ChildAppBar(
                onBack = onBack,
                title = stringResource(R.string.achievement_screen_title),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        val contentsModifier = Modifier
            .fillMaxSize()
            .background(Design.colors.primarySoft)
            .padding(innerPadding)
        when {
            state.isLoading -> LoadingContent(modifier = contentsModifier)
            state.isError -> FailedContent(modifier = contentsModifier)
            else -> {
                AchievementsContent(
                    summary = state.summary,
                    categories = state.categories,
                    onScreenVisible = onScreenVisible,
                    onAchievementClicked = onAchievementClicked,
                    modifier = contentsModifier,
                )
            }
        }
    }

    state.selectedAchievement?.let { achievement ->
        AchievementDetailDialog(
            achievementState = achievement,
            onDismiss = onDismissDetail,
        )
    }
}

@Composable
private fun AchievementsContent(
    summary: TrophyCaseSummary,
    categories: List<CategoryGroup>,
    onScreenVisible: () -> Unit,
    onAchievementClicked: (AchievementState) -> Unit,
    modifier: Modifier = Modifier,
    totalColumns: Int = 3,
) {
    LaunchedEffect(Unit) {
        onScreenVisible()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(totalColumns),
        contentPadding = PaddingValues(
            horizontal = Design.dimensions.spacing.xxl,
            vertical = Design.dimensions.spacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
        modifier = modifier,
    ) {
        // Header
        item(span = { GridItemSpan(totalColumns) }) {
            TrophyCaseHero(summary = summary)
        }

        categories.forEach { group ->
            item(span = { GridItemSpan(totalColumns) }) {
                CategoryHeading(
                    category = group.category,
                    earnedCount = group.earnedCount,
                    totalCount = group.totalCount,
                )
            }
            items(
                count = group.achievements.count(),
                span = { GridItemSpan(1) }
            ) { index ->
                val achievement = group.achievements[index]
                AchievementTile(
                    item = achievement,
                    onClick = { onAchievementClicked(achievement) },
                    modifier = Modifier.height(210.dp),
                )
            }
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
                summary = TrophyCaseSummary(
                    totalEarned = 1,
                    totalAchievements = 20,
                    inProgress = 5,
                    locked = 2,
                ),
                categories = listOf(
                    CategoryGroup(
                        category = AchievementCategory.PUZZLES,
                        earnedCount = 1,
                        totalCount = 4,
                        achievements = listOf(
                            AchievementState.Complete(
                                achievement = Achievement.RATED_PUZZLES_SOLVED,
                                unseen = false,
                            ),
                            AchievementState.Earned(
                                achievement = Achievement.FAILED_PUZZLES_REDEEMED,
                                unseen = false,
                                currentTier = Achievement.Tier.TWO,
                                currentProgress = 250,
                                nextThreshold = 1000,
                            ),
                            AchievementState.Unearned(
                                achievement = Achievement.RATED_WIN_STREAK,
                                currentProgress = 2,
                                nextThreshold = 3,
                            ),
                        ),
                    ),
                    CategoryGroup(
                        category = AchievementCategory.RUSH_AND_STREAK,
                        earnedCount = 0,
                        totalCount = 4,
                        achievements = listOf(
                            AchievementState.Earned(
                                achievement = Achievement.PUZZLE_RUSH_SESSIONS,
                                unseen = true,
                                currentTier = Achievement.Tier.THREE,
                                currentProgress = 100,
                                nextThreshold = 250,
                            ),
                            AchievementState.Unearned(
                                achievement = Achievement.STREAK_SESSIONS,
                                currentProgress = 10,
                                nextThreshold = 25,
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}
