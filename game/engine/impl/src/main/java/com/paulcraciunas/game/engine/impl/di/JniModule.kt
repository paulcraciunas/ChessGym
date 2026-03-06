package com.paulcraciunas.game.engine.impl.di

import com.paulcraciunas.game.engine.impl.StockfishBridge
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object JniModule {
    @Provides
    @Singleton
    fun provideStockfishBridge(): StockfishBridge = StockfishBridge()
}
