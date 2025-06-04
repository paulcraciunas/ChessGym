package com.paulcraciunas.game.io.di

import com.paulcraciunas.game.io.api.PuzzleReader
import com.paulcraciunas.game.io.api.PuzzleWriter
import com.paulcraciunas.game.io.binary.BinaryPuzzleReader
import com.paulcraciunas.game.io.binary.BinaryPuzzleWriter
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
