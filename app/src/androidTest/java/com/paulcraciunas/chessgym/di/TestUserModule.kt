package com.paulcraciunas.chessgym.di

import com.paulcraciunas.user.api.FakeUserLocalDataSource
import com.paulcraciunas.user.api.FakeUserRemoteDataSource
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.UserLocalDataSource
import com.paulcraciunas.user.api.UserRemoteDataSource
import com.paulcraciunas.user.api.UserRepository
import com.paulcraciunas.user.di.UserModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UserModule::class]
)
internal object TestUserModule {
    val userRepository = FakeUserRepository()
    val userLocalDataSource = FakeUserLocalDataSource()
    val userRemoteDataSource = FakeUserRemoteDataSource()

    @Provides
    @Singleton
    fun provideFakeUserRepository(): FakeUserRepository = userRepository

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = userRepository

    @Provides
    @Singleton
    fun providesUserLocalDataSource(): UserLocalDataSource = userLocalDataSource

    @Provides
    @Singleton
    fun providesUserRemoteDataSource(): UserRemoteDataSource = userRemoteDataSource
}
