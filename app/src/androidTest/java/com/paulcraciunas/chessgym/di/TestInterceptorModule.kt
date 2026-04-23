package com.paulcraciunas.chessgym.di

import com.paulcraciunas.puzzles.api.PuzzleInterceptor
import com.paulcraciunas.puzzles.di.PuzzleInterceptorModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dagger.multibindings.IntoSet

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PuzzleInterceptorModule::class]
)
internal object TestInterceptorModule {
    @Provides
    @IntoSet
    fun bindTestInterceptor(impl: TestPuzzleInterceptor): PuzzleInterceptor = impl
}
