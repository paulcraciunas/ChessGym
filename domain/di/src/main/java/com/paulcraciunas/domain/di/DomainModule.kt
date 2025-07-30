package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.CalculateElo
import com.paulcraciunas.domain.api.GetPuzzleByRating
import com.paulcraciunas.domain.api.OnPuzzleComplete
import com.paulcraciunas.domain.impl.CalculateEloImpl
import com.paulcraciunas.domain.impl.GetPuzzleByRatingImpl
import com.paulcraciunas.domain.impl.OnPuzzleCompleteImpl
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
    abstract fun bindOnPuzzleCompleteUseCase(impl: OnPuzzleCompleteImpl): OnPuzzleComplete

    @Binds
    @Singleton
    abstract fun bindGetPuzzleByRatingUseCase(impl: GetPuzzleByRatingImpl): GetPuzzleByRating
}
