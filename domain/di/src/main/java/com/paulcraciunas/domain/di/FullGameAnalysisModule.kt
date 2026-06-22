package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.analysis.AnalyzeFullGame
import com.paulcraciunas.domain.impl.analysis.AnalyzeFullGameImpl
import com.paulcraciunas.game.engine.api.ChessEngine
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
internal object FullGameAnalysisModule {
    @Provides
    fun provideAnalyzeFullGame(
        engine: ChessEngine,
        @SerializerFen serializer: Serializer,
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): AnalyzeFullGame = AnalyzeFullGameImpl(
        engine = engine,
        serializer = serializer,
        dispatcher = dispatcher,
    )
}
