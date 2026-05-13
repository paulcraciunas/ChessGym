package com.paulcraciunas.screens.achievements.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.achievements.vm.AchievementCategory
import com.paulcraciunas.screens.achievements.vm.AchievementsInteractor
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.CategoryGroup
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.TrophyCaseSummary
import com.paulcraciunas.screens.achievements.vm.StubAchievementsInteractor
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.FailedContent
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.design.components.ChessGymSpacer
import com.paulcraciunas.screens.common.design.components.SectionHeader
import com.paulcraciunas.screens.common.design.components.SpacerSize
import com.paulcraciunas.screens.common.design.theme.Design
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
        contentWindowInsets = WindowInsets.navigationBars,
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
                    interactions = interactions,
                    modifier = contentsModifier,
                )
            }
        }
    }
}

@Composable
private fun AchievementsContent(
    summary: TrophyCaseSummary,
    categories: List<CategoryGroup>,
    interactions: AchievementsInteractor,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        interactions.onScreenVisible()
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = Design.dimensions.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.sm),
    ) {
        item(key = "trophy_case") {
            TrophyCaseHero(
                summary = summary,
                modifier = Modifier.padding(horizontal = Design.dimensions.spacing.xxl),
            )
        }

        categories.forEach { group ->
            item(key = "header_${group.category.name}") {
                Column(
                    modifier = Modifier.padding(horizontal = Design.dimensions.spacing.xxl),
                ) {
                    SectionHeader(title = stringResource(group.category.labelRes()))
                    Text(
                        text = stringResource(
                            R.string.achievement_earned_count,
                            group.earnedCount,
                            group.totalCount,
                        ),
                        style = Design.typography.bodySmall,
                        color = Design.colors.inkSoft,
                    )
                    ChessGymSpacer(size = SpacerSize.DEFAULT)
                }
            }

            item(key = "row_${group.category.name}") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Design.dimensions.spacing.lg),
                    contentPadding = PaddingValues(horizontal = Design.dimensions.spacing.xxl),
                ) {
                    items(group.achievements, key = { it.achievement.name }) { achievement ->
                        AchievementTile(item = achievement)
                    }
                }
            }
        }
        item(key = "bottom_spacer") {
            ChessGymSpacer(size = SpacerSize.SECTION)
        }
    }
}

@StringRes
private fun AchievementCategory.labelRes(): Int = when (this) {
    AchievementCategory.PUZZLES -> R.string.achievement_group_puzzles
    AchievementCategory.RUSH_AND_STREAK -> R.string.achievement_group_rush_and_streak
    AchievementCategory.BOARD_VISION -> R.string.achievement_group_board_vision
    AchievementCategory.SKILL_AND_MASTERY -> R.string.achievement_group_skill_and_mastery
    AchievementCategory.DEDICATION -> R.string.achievement_group_dedication
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
            interactions = StubAchievementsInteractor(),
        )
    }
}
