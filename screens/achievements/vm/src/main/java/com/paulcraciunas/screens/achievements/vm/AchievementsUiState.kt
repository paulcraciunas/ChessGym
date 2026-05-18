package com.paulcraciunas.screens.achievements.vm

import com.paulcraciunas.domain.api.achievements.Achievement

data class AchievementsUiState(
    val summary: TrophyCaseSummary = TrophyCaseSummary(),
    val categories: List<CategoryGroup> = emptyList(),
    val selectedAchievement: AchievementState? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
) {
    data class TrophyCaseSummary(
        val totalEarned: Int = 0,
        val totalAchievements: Int = 0,
        val inProgress: Int = 0,
        val locked: Int = 0,
    )

    data class CategoryGroup(
        val category: AchievementCategory,
        val earnedCount: Int,
        val totalCount: Int,
        val achievements: List<AchievementState>,
    )

    sealed class AchievementState {
        abstract val achievement: Achievement
        abstract val unseen: Boolean
        abstract val currentTier: Achievement.Tier?
        abstract val currentProgress: Long

        abstract fun hasProgress(): Boolean
        abstract fun progress(): Float
        abstract fun displayTier(): Achievement.Tier

        fun isCompleted(): Boolean = this is Complete

        sealed class Incomplete : AchievementState() {
            abstract val nextThreshold: Long

            override fun progress(): Float =
                (currentProgress.toFloat() / nextThreshold).coerceIn(0f, 1f)

            override fun hasProgress(): Boolean = currentTier != null || currentProgress > 0
        }

        data class Unearned(
            override val achievement: Achievement,
            override val currentProgress: Long,
            override val nextThreshold: Long,
        ) : Incomplete() {
            override val unseen: Boolean = false
            override val currentTier: Achievement.Tier? = null

            init {
                require(nextThreshold != 0L)
            }

            override fun displayTier(): Achievement.Tier = Achievement.Tier.ONE
        }

        data class Earned(
            override val achievement: Achievement,
            override val unseen: Boolean,
            override val currentTier: Achievement.Tier,
            override val currentProgress: Long,
            override val nextThreshold: Long,
        ) : Incomplete() {
            init {
                require(nextThreshold != 0L)
            }

            override fun displayTier(): Achievement.Tier = currentTier
        }

        data class Complete(
            override val achievement: Achievement,
            override val unseen: Boolean,
        ) : AchievementState() {
            override val currentTier: Achievement.Tier = Achievement.Tier.maxTier()
            override val currentProgress: Long = achievement.completeProgress()

            override fun progress(): Float = 1f
            override fun hasProgress(): Boolean = true
            override fun displayTier(): Achievement.Tier = currentTier
        }
    }

}
