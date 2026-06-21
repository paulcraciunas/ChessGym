package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.domain.api.achievements.GetAchievementState
import com.paulcraciunas.domain.api.achievements.MarkAchievementsSeen
import com.paulcraciunas.domain.api.achievements.UpdateAchievementProgress
import com.paulcraciunas.domain.api.analysis.AnalyzePosition
import com.paulcraciunas.domain.api.auth.AuthenticateUseCase
import com.paulcraciunas.domain.api.auth.DeleteAccountUseCase
import com.paulcraciunas.domain.api.auth.ResetPasswordUseCase
import com.paulcraciunas.domain.api.auth.SignOutUseCase
import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.domain.api.blindmode.OnBlindModeGameComplete
import com.paulcraciunas.domain.api.boardvis.GenerateKnightPathExercise
import com.paulcraciunas.domain.api.boardvis.GetKnightPathBufferedSeries
import com.paulcraciunas.domain.api.boardvis.OnFindSquareComplete
import com.paulcraciunas.domain.api.boardvis.OnKnightPathComplete
import com.paulcraciunas.domain.api.general.CalculateElo
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.domain.api.puzzles.GetBufferedPuzzleSeries
import com.paulcraciunas.domain.api.puzzles.GetFailedPuzzles
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.domain.api.puzzles.OnFailedPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.OnPuzzleComplete
import com.paulcraciunas.domain.api.puzzles.OnPuzzleRushComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakComplete
import com.paulcraciunas.domain.api.puzzles.OnStreakPuzzleComplete
import com.paulcraciunas.domain.impl.achievements.AchievementNotificationManagerImpl
import com.paulcraciunas.domain.impl.achievements.GetAchievementStateImpl
import com.paulcraciunas.domain.impl.achievements.MarkAchievementsSeenImpl
import com.paulcraciunas.domain.impl.achievements.UpdateAchievementProgressImpl
import com.paulcraciunas.domain.impl.analysis.AnalyzePositionImpl
import com.paulcraciunas.domain.impl.auth.AuthenticateUseCaseImpl
import com.paulcraciunas.domain.impl.auth.DeleteAccountUseCaseImpl
import com.paulcraciunas.domain.impl.auth.ResetPasswordUseCaseImpl
import com.paulcraciunas.domain.impl.auth.SignOutUseCaseImpl
import com.paulcraciunas.domain.impl.billing.BillingUseCaseImpl
import com.paulcraciunas.domain.impl.blindmode.OnBlindModeGameCompleteImpl
import com.paulcraciunas.domain.impl.boardvis.GenerateKnightPathExerciseImpl
import com.paulcraciunas.domain.impl.boardvis.GetKnightPathBufferedSeriesImpl
import com.paulcraciunas.domain.impl.boardvis.OnFindSquareCompleteImpl
import com.paulcraciunas.domain.impl.boardvis.OnKnightPathCompleteImpl
import com.paulcraciunas.domain.impl.general.CalculateEloImpl
import com.paulcraciunas.domain.impl.general.SimpleTimer
import com.paulcraciunas.domain.impl.puzzles.GetBufferedPuzzleSeriesImpl
import com.paulcraciunas.domain.impl.puzzles.GetFailedPuzzlesImpl
import com.paulcraciunas.domain.impl.puzzles.GetPuzzleByRatingImpl
import com.paulcraciunas.domain.impl.puzzles.GetRatedPuzzleImpl
import com.paulcraciunas.domain.impl.puzzles.GetStreakPuzzleImpl
import com.paulcraciunas.domain.impl.puzzles.OnFailedPuzzleCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnPuzzleCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnPuzzleRushCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnStreakCompleteImpl
import com.paulcraciunas.domain.impl.puzzles.OnStreakPuzzleCompleteImpl
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
    abstract fun bindAnalyzePosition(impl: AnalyzePositionImpl): AnalyzePosition

    @Binds
    @Singleton
    abstract fun bindCalculateEloUseCase(impl: CalculateEloImpl): CalculateElo

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
    abstract fun bindGetPuzzleByRatingUseCase(impl: GetPuzzleByRatingImpl): GetPuzzleByRating

    @Binds
    @Singleton
    abstract fun bindGetRatedPuzzleUseCase(impl: GetRatedPuzzleImpl): GetRatedPuzzle

    @Binds
    abstract fun bindTimer(impl: SimpleTimer): Timer

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
    abstract fun bindOnFindSquareComplete(impl: OnFindSquareCompleteImpl): OnFindSquareComplete

    @Binds
    @Singleton
    abstract fun bindGenerateKnightPathExercise(impl: GenerateKnightPathExerciseImpl): GenerateKnightPathExercise

    @Binds
    @Singleton
    abstract fun bindGetKnightPathBufferedSeries(impl: GetKnightPathBufferedSeriesImpl): GetKnightPathBufferedSeries

    @Binds
    @Singleton
    abstract fun bindOnKnightPathComplete(impl: OnKnightPathCompleteImpl): OnKnightPathComplete

    @Binds
    @Singleton
    abstract fun bindOnBlindModeGameComplete(impl: OnBlindModeGameCompleteImpl): OnBlindModeGameComplete

    @Binds
    @Singleton
    abstract fun bindUpdateAchievementProgress(impl: UpdateAchievementProgressImpl): UpdateAchievementProgress

    @Binds
    @Singleton
    abstract fun bindGetAchievementState(impl: GetAchievementStateImpl): GetAchievementState

    @Binds
    @Singleton
    abstract fun bindMarkAchievementsSeen(impl: MarkAchievementsSeenImpl): MarkAchievementsSeen

    @Binds
    @Singleton
    abstract fun bindAchievementNotificationManager(impl: AchievementNotificationManagerImpl): AchievementNotificationManager

    @Binds
    @Singleton
    abstract fun bindAuthenticateUseCase(impl: AuthenticateUseCaseImpl): AuthenticateUseCase

    @Binds
    @Singleton
    abstract fun bindSignOutUseCase(impl: SignOutUseCaseImpl): SignOutUseCase

    @Binds
    @Singleton
    abstract fun bindDeleteAccountUseCase(impl: DeleteAccountUseCaseImpl): DeleteAccountUseCase

    @Binds
    @Singleton
    abstract fun bindResetPasswordUseCase(impl: ResetPasswordUseCaseImpl): ResetPasswordUseCase

    @Binds
    @Singleton
    abstract fun bindBillingUseCase(impl: BillingUseCaseImpl): BillingUseCase
}
