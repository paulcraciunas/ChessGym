package com.paulcraciunas.notifications.api

import android.app.Notification
import android.content.Context

interface NotificationFactory {
    fun createChannel(context: Context)
    fun createForegroundNotification(context: Context): Notification

    companion object Ids {
        const val DOWNLOAD_NOTIFICATION_ID = 101
    }
}
