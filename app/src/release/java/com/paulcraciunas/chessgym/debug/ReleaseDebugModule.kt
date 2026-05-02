package com.paulcraciunas.chessgym.debug

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReleaseDebugModule {
    @Binds
    abstract fun bindDebugMenuProvider(impl: NoOpDebugMenuProvider): DebugMenuProvider
}
