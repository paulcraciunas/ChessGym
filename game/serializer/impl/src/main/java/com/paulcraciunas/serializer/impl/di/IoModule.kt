package com.paulcraciunas.serializer.impl.di

import com.paulcraciunas.serializer.api.PuzzleReader
import com.paulcraciunas.serializer.api.PuzzleWriter
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleReader
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleWriter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class IoModule {
    @Binds
    @Singleton
    abstract fun bindReader(reader: BinaryPuzzleReader): PuzzleReader

    @Binds
    @Singleton
    abstract fun bindWriter(writer: BinaryPuzzleWriter): PuzzleWriter
}
