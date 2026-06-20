package com.paulcraciunas.puzzles.di

import com.paulcraciunas.puzzles.api.PuzzleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object BenchmarkRepositoryModule {
    @Provides
    @Singleton
    fun provideRepository(): PuzzleRepository = BenchmarkPuzzleRepository()
}
