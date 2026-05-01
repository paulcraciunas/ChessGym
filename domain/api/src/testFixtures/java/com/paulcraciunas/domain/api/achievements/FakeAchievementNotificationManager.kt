package com.paulcraciunas.domain.api.achievements

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeAchievementNotificationManager : AchievementNotificationManager {
    private val _notifications = MutableSharedFlow<AchievementNotification>(extraBufferCapacity = 10)
    override val notifications: SharedFlow<AchievementNotification> = _notifications.asSharedFlow()

    val emitted = mutableListOf<AchievementNotification>()

    override suspend fun emit(notification: AchievementNotification) {
        emitted.add(notification)
        _notifications.emit(notification)
    }
}
