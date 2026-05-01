package com.paulcraciunas.domain.api.achievements

import com.paulcraciunas.user.api.User

/**
 * Use case for updating achievement progress after any game activity completes.
 *
 * Recalculates achievement tiers, tracks daily streaks, and emits notifications
 * for newly unlocked achievements.
 */
interface UpdateAchievementProgress {
    suspend operator fun invoke(user: User): User
}
