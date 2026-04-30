package com.paulcraciunas.domain.api.achievements

/**
 * Clears the set of unseen achievements so the glow animation is not shown again.
 * Called when the user visits the Achievements screen.
 */
interface MarkAchievementsSeen {
    suspend operator fun invoke()
}
