package com.paulcraciunas.puzzles.impl.di

import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.puzzles.impl.impl.PuzzleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun provideRepository(impl: PuzzleRepositoryImpl): PuzzleRepository
}
