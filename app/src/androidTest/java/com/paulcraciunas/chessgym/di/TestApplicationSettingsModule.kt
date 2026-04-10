package com.paulcraciunas.chessgym.di

import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.settings.application.impl.ApplicationSettingsModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ApplicationSettingsModule::class]
)
internal object TestApplicationSettingsModule {
    val appSettingsRepository = FakeAppSettingsRepository.default()

    @Provides
    @Singleton
    fun provideFakeAppSettingsRepository(): FakeAppSettingsRepository =
        appSettingsRepository

    @Provides
    @Singleton
    fun provideAppSettingsRepository(): AppSettingsRepository = appSettingsRepository
}
