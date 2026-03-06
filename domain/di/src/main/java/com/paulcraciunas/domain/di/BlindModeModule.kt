package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.blindmode.BlindModeOrchestrator
import com.paulcraciunas.domain.impl.blindmode.BlindModeOrchestratorImpl
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal class BlindModeModule {
    @Provides
    fun provideBlindModeOrchestrator(
        gameFactory: GameFactory,
        chessEngine: ChessEngine,
        @SerializerFen serializer: Serializer,
    ): BlindModeOrchestrator = BlindModeOrchestratorImpl(
        gameFactory = gameFactory,
        chessEngine = chessEngine,
        serializer = serializer,
    )
}
