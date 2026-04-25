package com.paulcraciunas.screens.boardvis.squares.vm

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class FindTheSquareModule {

    @Binds
    abstract fun bindGameDuration(impl: DefaultGameDuration): GameDuration
}
