package com.paulcraciunas.chessgym.di

import com.paulcraciunas.user.api.AuthService
import com.paulcraciunas.user.api.FakeAuthService
import com.paulcraciunas.user.api.FakeSyncScheduler
import com.paulcraciunas.user.api.FakeSyncState
import com.paulcraciunas.user.api.FakeTokenProvider
import com.paulcraciunas.user.api.SyncScheduler
import com.paulcraciunas.user.api.SyncState
import com.paulcraciunas.user.api.TokenProvider
import com.paulcraciunas.user.di.network.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
internal object TestNetworkModule {
    val tokenProvider = FakeTokenProvider()
    val authService = FakeAuthService()
    val syncState = FakeSyncState()
    val syncScheduler = FakeSyncScheduler()

    @Provides
    @Singleton
    fun provideTokenProvider(): TokenProvider = tokenProvider

    @Provides
    @Singleton
    fun provideAuthService(): AuthService = authService

    @Provides
    @Singleton
    fun provideSyncState(): SyncState = syncState

    @Provides
    @Singleton
    fun provideSyncScheduler(): SyncScheduler = syncScheduler
}
