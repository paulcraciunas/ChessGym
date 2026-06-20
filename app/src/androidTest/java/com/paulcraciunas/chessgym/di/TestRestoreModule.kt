package com.paulcraciunas.chessgym.di

import com.paulcraciunas.chessgym.startup.AppSettingsProvisioning
import com.paulcraciunas.chessgym.startup.RestoreModule
import com.paulcraciunas.settings.application.api.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RestoreModule::class]
)
object TestRestoreModule {
    @Provides
    fun provideRestoreCheck(): AppSettingsProvisioning = object : AppSettingsProvisioning {
        override operator fun invoke(): Flow<AppSettings> = TestApplicationSettingsModule.appSettingsRepository.appSettings
    }
}
