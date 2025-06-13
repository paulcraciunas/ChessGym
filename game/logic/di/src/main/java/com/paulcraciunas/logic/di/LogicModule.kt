package com.paulcraciunas.logic.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LogicModule {
    @Provides
    fun provideGameFactory(): GameFactory = GameFactory()

    // TODO Paul: Implement me
}