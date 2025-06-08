package com.paulcraciunas.serializer.impl.di

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
internal annotation class SerializerFen

@Qualifier
internal annotation class SerializerPgn

@Module
@InstallIn(SingletonComponent::class)
internal object SerializerModule {
    @SerializerFen
    @Provides
    @Singleton
    fun provideFenSerializer(): Serializer = FenSerializer

    @SerializerPgn
    @Provides
    @Singleton
    fun providePgnSerializer(): Serializer = PgnSerializer
}
