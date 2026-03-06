package com.paulcraciunas.serializer.di

import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.serializer.impl.PgnSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class SerializerFen

@Qualifier
annotation class SerializerPgn

@Module
@InstallIn(SingletonComponent::class)
internal class SerializerModule {
    @SerializerFen
    @Provides
    @Singleton
    fun provideFenSerializer(gameFactory: GameFactory): Serializer = FenSerializer(gameFactory)

    @SerializerPgn
    @Provides
    @Singleton
    fun providePgnSerializer(gameFactory: GameFactory): Serializer = PgnSerializer(gameFactory)
}
