package com.paulcraciunas.serializer.impl.di

import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.serializer.impl.PgnSerializer
import dagger.Binds
import dagger.Module
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
internal abstract class SerializerModule {
    @SerializerFen
    @Binds
    @Singleton
    abstract fun provideFenSerializer(impl: FenSerializer): Serializer

    @SerializerPgn
    @Binds
    @Singleton
    abstract fun providePgnSerializer(impl: PgnSerializer): Serializer
}
