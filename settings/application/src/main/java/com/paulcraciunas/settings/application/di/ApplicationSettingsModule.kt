package com.paulcraciunas.settings.application.di

import com.paulcraciunas.settings.application.AppSettingsRepository
import com.paulcraciunas.settings.application.DataStoreAppSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ApplicationSettingsModule {
    @Binds
    @Singleton
    abstract fun appSettingsRepository(repository: DataStoreAppSettingsRepository): AppSettingsRepository
}
