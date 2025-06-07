package com.paulcraciunas.notifications.di

import com.paulcraciunas.notifications.api.NotificationFactory
import com.paulcraciunas.notifications.impl.DownloadNotificationFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {
    @Provides
    @Singleton
    fun provideDownloadNotificationFactory(): NotificationFactory = DownloadNotificationFactory()
}
