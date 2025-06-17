package com.paulcraciunas.settings.user.di

import com.paulcraciunas.settings.user.DataStoreUserStatsRepository
import com.paulcraciunas.settings.user.UserStatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UserSettingsModule {
    @Binds
    @Singleton
    abstract fun userStatsRepository(impl: DataStoreUserStatsRepository): UserStatsRepository
}
