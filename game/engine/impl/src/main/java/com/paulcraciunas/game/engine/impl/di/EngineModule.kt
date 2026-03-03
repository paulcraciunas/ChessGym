package com.paulcraciunas.game.engine.impl.di

import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.impl.StockfishUciFacade
import com.paulcraciunas.game.engine.impl.UciChessEngine
import com.paulcraciunas.game.engine.impl.UciFacade
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class EngineModule {
    @Binds
    @Singleton
    abstract fun bindStockfish(impl: StockfishUciFacade): UciFacade

    @Binds
    @Singleton
    abstract fun bindChessEngine(impl: UciChessEngine): ChessEngine
}
