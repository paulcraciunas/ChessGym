package com.paulcraciunas.global.device.di

import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.global.device.impl.usecases.GetFreeDiskSpaceImpl
import com.paulcraciunas.global.device.impl.usecases.GetNetworkStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DeviceModule {
    
    @Binds
    @Singleton
    abstract fun getNetworkState(impl: GetNetworkStateImpl): GetNetworkState
    
    @Binds
    @Singleton
    abstract fun getFreeDiskSpace(impl: GetFreeDiskSpaceImpl): GetFreeDiskSpace
}
