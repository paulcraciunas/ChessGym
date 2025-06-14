package com.paulcraciunas.logic.di

import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LogicModule {
    @Provides
    @Singleton
    fun providePlyFactory(): PlyFactory = PlyFactory()
}
