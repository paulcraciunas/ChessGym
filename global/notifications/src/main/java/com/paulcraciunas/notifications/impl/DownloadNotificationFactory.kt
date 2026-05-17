package com.paulcraciunas.notifications.impl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.paulcraciunas.notifications.R
import com.paulcraciunas.notifications.api.NotificationFactory
import javax.inject.Inject

internal class DownloadNotificationFactory @Inject constructor() : NotificationFactory {
    override fun createChannel(context: Context) {
        val name = context.getString(R.string.download_channel_name)
        val desc = context.getString(R.string.download_channel_desc)
        val channel = NotificationChannel(
            CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW
        ).apply { description = desc }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun createForegroundNotification(context: Context): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.download_notification_title))
        .setContentText(context.getString(R.string.download_notification_text))
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    companion object {
        private const val CHANNEL_ID = "puzzle_download_channel"
    }
}
