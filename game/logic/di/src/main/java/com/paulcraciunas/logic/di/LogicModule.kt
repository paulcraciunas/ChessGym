package com.paulcraciunas.logic.di

import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.game.logic.impl.plies.PlyFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class LogicModule {
    @Provides
    @Singleton
    fun providePlyFactory(): PlyFactory = PlyFactory()

    @Provides
    @Singleton
    fun provideGameFactory(plyFactory: PlyFactory): GameFactory = RealGameFactory(plyFactory)
}
