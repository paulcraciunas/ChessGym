package com.paulcraciunas.chessgym.startup

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RestoreModule {
    @Binds
    abstract fun bindDeviceRestore(impl: AppSettingsProvisioningImpl): AppSettingsProvisioning
}
