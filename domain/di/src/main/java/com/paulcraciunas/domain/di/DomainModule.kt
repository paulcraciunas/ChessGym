package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.general.CalculateElo
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.GenerateMoveThePieceBoard
import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.api.MoveThePieceGameEngine
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.domain.api.puzzles.GetPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.domain.api.OnFindSquareComplete
import com.paulcraciunas.domain.api.OnMoveThePieceComplete
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.impl.general.CalculateEloImpl
import com.paulcraciunas.domain.impl.GenerateMoveThePieceBoardImpl
import com.paulcraciunas.domain.impl.GenerateRandomLociImpl
import com.paulcraciunas.domain.impl.MoveThePieceGameEngineImpl
import com.paulcraciunas.domain.impl.puzzles.GetBufferedPuzzleSeriesImpl
import com.paulcraciunas.domain.impl.puzzles.GetFailedPuzzlesImpl
import com.paulcraciunas.domain.impl.puzzles.GetPuzzleByRatingImpl
import com.paulcraciunas.domain.impl.puzzles.GetPuzzleSeriesImpl
import com.paulcraciunas.domain.impl.puzzles.GetRatedPuzzleImpl
import com.paulcraciunas.domain.impl.puzzles.GetStreakPuzzleImpl
import com.paulcraciunas.domain.impl.puzzles.OnFailedPuzzleCompleteImpl
import com.paulcraciunas.domain.impl.OnFindSquareCompleteImpl
import com.paulcraciunas.domain.impl.OnMoveThePieceCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnPuzzleCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnPuzzleRushCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnStreakCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnStreakPuzzleCompleteImpl
import com.paulcraciunas.domain.impl.general.RealCountdownTimer
import com.paulcraciunas.domain.impl.general.SimpleTimer
import com.paulcraciunas.domain.impl.general.TLRandomFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused") // Used by Hilt
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
    abstract fun bindGetBufferedPuzzleSeries(impl: GetBufferedPuzzleSeriesImpl): GetBufferedPuzzleSeries

    @Binds
    @Singleton
    abstract fun bindOnPuzzleCompleteUseCase(impl: OnPuzzleCompleteImpl): OnPuzzleComplete

    @Binds
    @Singleton
    abstract fun bindOnPuzzleRushCompleteUseCase(impl: OnPuzzleRushCompleteImpl): OnPuzzleRushComplete

    @Binds
    abstract fun bindGetFailedPuzzles(impl: GetFailedPuzzlesImpl): GetFailedPuzzles

    @Binds
    @Singleton
    abstract fun bindOnFailedPuzzleComplete(impl: OnFailedPuzzleCompleteImpl): OnFailedPuzzleComplete

    @Binds
    @Singleton
    abstract fun bindGetPuzzleByRatingUseCase(impl: GetPuzzleByRatingImpl): GetPuzzleByRating

    @Binds
    @Singleton
    abstract fun bindGetRatedPuzzleUseCase(impl: GetRatedPuzzleImpl): GetRatedPuzzle

    @Binds
    abstract fun bindTimer(impl: SimpleTimer): Timer

    @Binds
    abstract fun bindCountdownTimer(impl: RealCountdownTimer): CountdownTimer

    @Binds
    @Singleton
    abstract fun bindGetStreakPuzzle(impl: GetStreakPuzzleImpl): GetStreakPuzzle

    @Binds
    @Singleton
    abstract fun bindOnStreakPuzzleComplete(impl: OnStreakPuzzleCompleteImpl): OnStreakPuzzleComplete

    @Binds
    @Singleton
    abstract fun bindOnStreakComplete(impl: OnStreakCompleteImpl): OnStreakComplete

    @Binds
    @Singleton
    abstract fun bindGenerateRandomLoci(impl: GenerateRandomLociImpl): GenerateRandomLoci

    @Binds
    @Singleton
    abstract fun bindOnFindSquareComplete(impl: OnFindSquareCompleteImpl): OnFindSquareComplete

    @Binds
    @Singleton
    abstract fun bindGenerateMoveThePieceBoard(impl: GenerateMoveThePieceBoardImpl): GenerateMoveThePieceBoard

    @Binds
    @Singleton
    abstract fun bindOnMoveThePieceComplete(impl: OnMoveThePieceCompleteImpl): OnMoveThePieceComplete

    @Binds
    abstract fun bindMoveThePieceGameEngine(impl: MoveThePieceGameEngineImpl): MoveThePieceGameEngine
}
