package com.paulcraciunas.game.io.di

import com.paulcraciunas.game.io.FenSerializer
import com.paulcraciunas.game.io.PgnSerializer
import com.paulcraciunas.game.io.api.Serializer
import com.paulcraciunas.game.io.api.SerializerFen
import com.paulcraciunas.game.io.api.SerializerPgn
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SerializerModule {
    @SerializerFen
    @Provides
    @Singleton
    fun provideFenSerializer(): Serializer = FenSerializer

    @SerializerPgn
    @Provides
    @Singleton
    fun providePgnSerializer(): Serializer = PgnSerializer
}
