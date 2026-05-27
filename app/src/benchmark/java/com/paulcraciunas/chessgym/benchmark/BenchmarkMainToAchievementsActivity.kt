package com.paulcraciunas.chessgym.benchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.screens.achievements.ui.AchievementsScreen
import com.paulcraciunas.screens.achievements.vm.AchievementCategory
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.CategoryGroup
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.TrophyCaseSummary
import com.paulcraciunas.screens.achievements.vm.category
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.screens.home.ui.HomeScreen
import com.paulcraciunas.screens.home.vm.HomeUiState
import java.time.LocalDate

/**
 * Benchmark activity that hosts a Home → Achievements navigation flow.
 * Avoids Hilt by providing hardcoded UI state directly to stateless composables.
 */
class BenchmarkMainToAchievementsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessGymTheme {
                BenchmarkHomeToAchievements(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true },
                )
            }
        }
    }
}

@Composable
private fun BenchmarkHomeToAchievements(modifier: Modifier = Modifier) {
    var showAchievements by remember { mutableStateOf(false) }

    if (showAchievements) {
        AchievementsScreen(
            state = benchmarkAchievementsState(),
            onScreenVisible = {},
            onAchievementClicked = {},
            onDismissDetail = {},
            onBack = { showAchievements = false },
            modifier = modifier.testTag { ACHIEVEMENTS_SCREEN_TAG },
        )
    } else {
        HomeScreen(
            state = benchmarkHomeState(),
            onAchievements = { showAchievements = true },
            modifier = modifier,
        )
    }
}

@Composable
private fun benchmarkHomeState(): HomeUiState = remember {
    HomeUiState(
        isLoading = false,
        userProfile = HomeUiState.UserProfile(
            name = "BenchmarkPlayer",
            currentRating = 1547,
            totalActivities = 142,
            joinDate = LocalDate.of(2024, 3, 15),
        ),
        userStats = HomeUiState.Stats(
            puzzlesPlayed = 350,
            puzzlesSolved = 280,
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
                    HomeUiState.HistoryEvent.RatedPuzzleEvent(ratingChange = 42, count = 12),
                ),
            ),
            HomeUiState.HistoryGroup(
                label = "Yesterday",
                events = listOf(
                    HomeUiState.HistoryEvent.BoardVizEvent(runs = 4),
                    HomeUiState.HistoryEvent.PuzzleStreakEvent(finalStreakCount = 12),
                ),
            ),
        ),
        unseenAchievementCount = 3,
        ribbons = listOf(
            HomeUiState.Ribbon(Achievement.RATED_PUZZLES_SOLVED, Achievement.Tier.THREE),
            HomeUiState.Ribbon(Achievement.PUZZLE_RUSH_SESSIONS, Achievement.Tier.TWO),
        ),
    )
}

@Composable
private fun benchmarkAchievementsState(): AchievementsUiState = remember {
    val categories = AchievementCategory.entries.map { category ->
        val achievements = Achievement.entries
            .filter { it.category == category }
            .mapIndexed { idx, achievement -> fakeAchievementState(achievement, idx) }
        CategoryGroup(
            category = category,
            earnedCount = achievements.count { it.currentTier != null },
            totalCount = achievements.size,
            achievements = achievements,
        )
    }
    val totalEarned = categories.sumOf { it.earnedCount }
    val totalCount = categories.sumOf { it.totalCount }

    AchievementsUiState(
        isLoading = false,
        summary = TrophyCaseSummary(
            totalEarned = totalEarned,
            totalAchievements = totalCount * Achievement.Tier.entries.size,
            inProgress = 8,
            locked = 4,
        ),
        categories = categories,
    )
}

private fun fakeAchievementState(
    achievement: Achievement,
    index: Int,
): AchievementState = when (index % 3) {
    0 -> AchievementState.Earned(
        achievement = achievement,
        unseen = index == 0,
        currentTier = Achievement.Tier.TWO,
        currentProgress = 50L,
        nextThreshold = 100L,
    )
    1 -> AchievementState.Unearned(
        achievement = achievement,
        currentProgress = 3L,
        nextThreshold = 5L,
    )
    else -> AchievementState.Complete(
        achievement = achievement,
        unseen = false,
    )
}

internal const val ACHIEVEMENTS_SCREEN_TAG = "benchmark_achievements_screen"
