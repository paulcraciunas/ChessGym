package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.impl.puzzles.GetPuzzleFenImpl
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal class PuzzleAnalysisModule {
    @Provides
    fun provideGetPuzzleFen(
        puzzleRepository: PuzzleRepository,
        @SerializerFen fenSerializer: Serializer,
    ): GetPuzzleFen = GetPuzzleFenImpl(
        puzzleRepository = puzzleRepository,
        fenSerializer = fenSerializer,
    )
}
