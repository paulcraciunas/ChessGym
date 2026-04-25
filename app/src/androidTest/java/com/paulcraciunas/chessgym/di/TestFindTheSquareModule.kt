package com.paulcraciunas.chessgym.di

import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareModule
import com.paulcraciunas.screens.boardvis.squares.vm.GameDuration
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FindTheSquareModule::class]
)
internal object TestFindTheSquareModule {
    const val TEST_DURATION_SECONDS = 3

    @Provides
    fun provideGameDuration(): GameDuration = object : GameDuration {
        override val seconds: Int = TEST_DURATION_SECONDS
    }
}
