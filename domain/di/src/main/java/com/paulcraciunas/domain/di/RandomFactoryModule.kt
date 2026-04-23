package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.impl.general.TLRandomFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dedicated Hilt module for the [RandomFactory] binding.
 *
 * Kept separate from [DomainModule] so that instrumentation tests can swap in a deterministic
 * implementation (e.g. `FixedRandomFactory`) without having to re-provide the entire domain graph.
 */
@Suppress("unused") // Used by Hilt
@Module
@InstallIn(SingletonComponent::class)
abstract class RandomFactoryModule {

    @Binds
    @Singleton
    abstract fun bindRandomFactory(impl: TLRandomFactory): RandomFactory
}
