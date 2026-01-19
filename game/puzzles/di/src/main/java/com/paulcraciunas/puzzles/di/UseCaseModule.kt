package com.paulcraciunas.puzzles.di

import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.puzzles.impl.usecases.FetchPuzzleDatabaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UseCaseModule {
    @Binds
    @Singleton
    abstract fun fetchPuzzleDatabase(impl: FetchPuzzleDatabaseImpl): FetchPuzzleDatabase
}
