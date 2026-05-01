package com.paulcraciunas.domain.impl.achievements

import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.domain.api.achievements.AchievementNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class AchievementNotificationManagerImpl @Inject constructor() : AchievementNotificationManager {
    private val _notifications = MutableSharedFlow<AchievementNotification>(extraBufferCapacity = 5)

    override val notifications: SharedFlow<AchievementNotification> = _notifications.asSharedFlow()

    override suspend fun emit(notification: AchievementNotification) {
        _notifications.emit(notification)
    }
}
