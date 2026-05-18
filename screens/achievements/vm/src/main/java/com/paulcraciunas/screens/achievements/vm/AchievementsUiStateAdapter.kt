package com.paulcraciunas.screens.achievements.vm

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementState
import javax.inject.Inject

class AchievementsUiStateAdapter @Inject constructor() {

    fun adapt(states: List<AchievementState>): AchievementsUiState {
        val items = states.map { adaptItem(it) }
        val sorted = items.sortedWith(achievementComparator)

        val grouped = AchievementCategory.entries.mapNotNull { cat ->
            val categoryItems = sorted.filter { it.achievement.category == cat }
            if (categoryItems.isEmpty()) return@mapNotNull null

            val earned = categoryItems.count { it.currentTier != null }
            AchievementsUiState.CategoryGroup(
                category = cat,
                earnedCount = earned,
                totalCount = categoryItems.size,
                achievements = categoryItems,
            )
        }

        val totalTiers = Achievement.Tier.entries.size
        val totalEarned = sorted.sumOf { it.earnedTierCount() }
        val inProgress = sorted.count { !it.isCompleted() && it.hasProgress() }
        val locked = sorted.count { !it.hasProgress() }

        return AchievementsUiState(
            summary = AchievementsUiState.TrophyCaseSummary(
                totalEarned = totalEarned,
                totalAchievements = sorted.size * totalTiers,
                inProgress = inProgress,
                locked = locked,
            ),
            categories = grouped,
            isLoading = false,
        )
    }

    private fun adaptItem(state: AchievementState): AchievementsUiState.AchievementState =
        if (state.currentTier == Achievement.Tier.maxTier()) {
            AchievementsUiState.AchievementState.Complete(
                achievement = state.achievement,
                unseen = state.isUnseen,
            )
        } else {
            val nextThreshold = state.nextThreshold ?: 1L
            if (state.currentTier != null) {
                AchievementsUiState.AchievementState.Earned(
                    achievement = state.achievement,
                    unseen = state.isUnseen,
                    currentTier = state.currentTier!!,
                    currentProgress = state.currentProgress,
                    nextThreshold = nextThreshold,
                )
            } else AchievementsUiState.AchievementState.Unearned(
                achievement = state.achievement,
                currentProgress = state.currentProgress,
                nextThreshold = nextThreshold,
            )
        }

    companion object {
        /**
         * Sorting: earned (highest tier, then progress%) > in-progress (by progress%) > zero.
         * Within equal buckets, falls back to enum declaration order.
         */
        internal val achievementComparator: Comparator<AchievementsUiState.AchievementState> =
            compareByDescending<AchievementsUiState.AchievementState> { it.sortBucket }
                .thenByDescending { it.currentTier?.ordinal ?: -1 }
                .thenByDescending { it.progress() }
                .thenBy { it.achievement.ordinal }

        private val AchievementsUiState.AchievementState.sortBucket: Int
            get() = when {
                currentTier != null -> 2
                currentProgress > 0 -> 1
                else -> 0
            }

        internal fun AchievementsUiState.AchievementState.earnedTierCount(): Int =
            currentTier?.ordinal?.plus(1) ?: 0
    }
}
