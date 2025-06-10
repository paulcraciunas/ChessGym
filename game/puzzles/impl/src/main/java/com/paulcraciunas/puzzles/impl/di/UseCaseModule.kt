package com.paulcraciunas.puzzles.impl.di

import com.paulcraciunas.puzzles.api.usecases.GetPuzzleSeries
import com.paulcraciunas.puzzles.impl.impl.RandomFactory
import com.paulcraciunas.puzzles.impl.impl.TLRandomFactory
import com.paulcraciunas.puzzles.impl.usecases.GetPuzzleSeriesImpl
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
    abstract fun randomFactory(impl: TLRandomFactory): RandomFactory

    @Binds
    @Singleton
    abstract fun getPuzzleSeries(impl: GetPuzzleSeriesImpl): GetPuzzleSeries
}
