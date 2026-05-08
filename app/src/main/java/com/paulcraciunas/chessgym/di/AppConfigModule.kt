package com.paulcraciunas.chessgym.di

import com.paulcraciunas.chessgym.BuildConfig
import com.paulcraciunas.global.qualifiers.BackendUrl
import com.paulcraciunas.global.qualifiers.IsDebug
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Provides
    @BackendUrl
    fun provideBackendUrl(): String = BuildConfig.BACKEND_URL

    @Provides
    @IsDebug
    fun provideIsDebug(): Boolean = BuildConfig.DEBUG
}
