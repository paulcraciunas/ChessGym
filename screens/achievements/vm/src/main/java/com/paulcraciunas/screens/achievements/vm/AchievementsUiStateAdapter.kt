package com.paulcraciunas.screens.achievements.vm

import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementState
import javax.inject.Inject

class AchievementsUiStateAdapter @Inject constructor() {

    fun adapt(states: List<AchievementState>): AchievementsUiState {
        val items = states.map { adaptItem(it) }
        return AchievementsUiState(
            achievements = items.sortedWith(achievementComparator),
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
        private val achievementComparator: Comparator<AchievementsUiState.AchievementState> =
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
    }
}
