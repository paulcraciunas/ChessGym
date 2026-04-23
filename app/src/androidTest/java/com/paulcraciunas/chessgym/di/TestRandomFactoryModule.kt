package com.paulcraciunas.chessgym.di

import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.di.RandomFactoryModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces the production [RandomFactoryModule] with a [FixedRandomFactory] so that any
 * UseCase that relies on [RandomFactory] (notably `GetRatedPuzzle`) can be made deterministic
 * during instrumentation tests by setting [FixedRandomFactory.returnValue].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RandomFactoryModule::class]
)
internal object TestRandomFactoryModule {
    val randomFactory: FixedRandomFactory = FixedRandomFactory()

    @Provides
    @Singleton
    fun provideFixedRandomFactory(): FixedRandomFactory = randomFactory

    @Provides
    @Singleton
    fun provideRandomFactory(): RandomFactory = randomFactory
}
