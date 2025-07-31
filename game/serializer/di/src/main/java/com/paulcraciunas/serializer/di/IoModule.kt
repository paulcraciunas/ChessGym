package com.paulcraciunas.serializer.di

import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.serializer.api.PuzzleReader
import com.paulcraciunas.serializer.api.PuzzleWriter
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.impl.binary.BinaryAdapter
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleReader
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class IoModule {
    @Provides
    @Singleton
    fun provideBinaryAdapter() = BinaryAdapter()

    @Provides
    @Singleton
    fun provideReader(
        gameFactory: GameFactory,
        adapter: BinaryAdapter
    ): PuzzleReader = BinaryPuzzleReader(gameFactory, adapter)

    @Provides
    @Singleton
    fun provideWriter(
        @SerializerFen serializer: Serializer,
        adapter: BinaryAdapter
    ): PuzzleWriter = BinaryPuzzleWriter(serializer, adapter)
}
