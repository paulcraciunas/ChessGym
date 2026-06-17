package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.impl.GenerateRandomLociImpl
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
 * implementation.
 */
@Suppress("unused") // Used by Hilt
@Module
@InstallIn(SingletonComponent::class)
abstract class RandomLociModule {

    @Binds
    @Singleton
    abstract fun bindGenerateRandomLoci(impl: GenerateRandomLociImpl): GenerateRandomLoci
}
