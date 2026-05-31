package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.impl.engine.EngineOrchestratorImpl
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal class EngineModule {
    @Provides
    fun provideEngineOrchestrator(
        chessEngine: ChessEngine,
        @SerializerFen serializer: Serializer,
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): EngineOrchestrator = EngineOrchestratorImpl(
        chessEngine = chessEngine,
        serializer = serializer,
        dispatcher = dispatcher,
    )
}
