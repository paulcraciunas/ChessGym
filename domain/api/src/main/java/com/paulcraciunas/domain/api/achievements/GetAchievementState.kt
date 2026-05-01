package com.paulcraciunas.domain.api.achievements

/**
 * Produces a UI-ready snapshot of all achievements with their current
 * progress, unlocked tier, and unseen status.
 */
interface GetAchievementState {
    suspend operator fun invoke(): List<AchievementState>
}

data class AchievementState(
    val achievement: Achievement,
    val currentTier: Achievement.Tier?,
    val currentProgress: Long,
    val nextThreshold: Long?,
    val isUnseen: Boolean,
)
