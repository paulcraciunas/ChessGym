package com.paulcraciunas.logic.di

import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.GameInteractor
import com.paulcraciunas.game.logic.api.MoveValidator
import com.paulcraciunas.game.logic.api.PuzzleInteractor
import com.paulcraciunas.game.logic.impl.RealGameFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class LogicModule {
    @Provides
    @Singleton
    fun provideGameFactory(): GameFactory = RealGameFactory()

    @Provides
    fun providePuzzleInteractor(gameFactory: GameFactory): PuzzleInteractor = gameFactory.puzzleInteractor()

    @Provides
    fun provideGameInteractor(gameFactory: GameFactory): GameInteractor = gameFactory.gameInteractor()

    @Provides
    fun provideMoveValidator(gameFactory: GameFactory): MoveValidator = gameFactory.moveValidator()
}
