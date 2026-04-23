package com.paulcraciunas.puzzles.di

import com.paulcraciunas.puzzles.api.PuzzleInterceptor
import com.paulcraciunas.puzzles.impl.impl.LoggingPuzzleInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object PuzzleInterceptorModule {
    @Provides
    @IntoSet
    fun provideLoggingInterceptor(impl: LoggingPuzzleInterceptor): PuzzleInterceptor = impl
}
