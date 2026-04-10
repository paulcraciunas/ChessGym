package com.paulcraciunas.settings.application.impl

import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationSettingsModule {
    @Binds
    @Singleton
    abstract fun appSettingsRepository(repository: DataStoreAppSettingsRepository): AppSettingsRepository
}
