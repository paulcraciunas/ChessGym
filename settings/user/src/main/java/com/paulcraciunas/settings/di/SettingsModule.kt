package com.paulcraciunas.settings.di

import android.content.Context
import com.paulcraciunas.settings.user.UserSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): UserSettings {
        return UserSettings(context)
    }
}
