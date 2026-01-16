package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.CalculateElo
import com.paulcraciunas.domain.api.GetPuzzleByRating
import com.paulcraciunas.domain.api.GetPuzzleSeries
import com.paulcraciunas.domain.api.OnPuzzleComplete
import com.paulcraciunas.domain.api.RandomFactory
import com.paulcraciunas.domain.api.Timer
import com.paulcraciunas.domain.impl.CalculateEloImpl
import com.paulcraciunas.domain.impl.GetPuzzleByRatingImpl
import com.paulcraciunas.domain.impl.GetPuzzleSeriesImpl
import com.paulcraciunas.domain.impl.OnPuzzleCompleteImpl
import com.paulcraciunas.domain.impl.SimpleTimer
import com.paulcraciunas.domain.impl.TLRandomFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindCalculateEloUseCase(impl: CalculateEloImpl): CalculateElo

    @Binds
    @Singleton
    abstract fun bindRandomFactory(impl: TLRandomFactory): RandomFactory

    @Binds
    @Singleton
    abstract fun bindGetPuzzleSeries(impl: GetPuzzleSeriesImpl): GetPuzzleSeries

    @Binds
    @Singleton
    abstract fun bindOnPuzzleCompleteUseCase(impl: OnPuzzleCompleteImpl): OnPuzzleComplete

    @Binds
    @Singleton
    abstract fun bindGetPuzzleByRatingUseCase(impl: GetPuzzleByRatingImpl): GetPuzzleByRating

    @Binds
    abstract fun bindTimer(impl: SimpleTimer): Timer
}
