package com.paulcraciunas.serializer.di

import com.paulcraciunas.logic.builders.Builders
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
    fun provideFenSerializer(): Serializer = FenSerializer(gameFactory = Builders.gameFactory())

    @SerializerPgn
    @Provides
    @Singleton
    fun providePgnSerializer(): Serializer = PgnSerializer(gameFactory = Builders.gameFactory())
}
