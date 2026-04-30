package com.paulcraciunas.domain.api.achievements

import kotlinx.coroutines.flow.SharedFlow

/**
 * Emits notifications when achievements are newly unlocked.
 * Observed by the UI layer to show a celebration banner.
 */
interface AchievementNotificationManager {
    val notifications: SharedFlow<AchievementNotification>

    suspend fun emit(notification: AchievementNotification)
}

data class AchievementNotification(
    val achievement: Achievement,
    val tier: Achievement.Tier,
)
