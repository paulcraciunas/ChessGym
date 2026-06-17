package com.paulcraciunas.chessgym.di

import com.paulcraciunas.chessgym.startup.IDeviceRestoreCheck
import com.paulcraciunas.chessgym.startup.RestoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RestoreModule::class]
)
object TestRestoreModule {
    @Provides
    fun provideRestoreCheck(): IDeviceRestoreCheck = object : IDeviceRestoreCheck {
        override suspend fun invoke() {}
    }
}
