package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.PulseTimer
import com.paulcraciunas.domain.impl.general.RealCountdownTimer
import com.paulcraciunas.domain.impl.general.RealPulseTimer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClockTimersModule {
    @Binds
    abstract fun bindCountdownTimer(impl: RealCountdownTimer): CountdownTimer

    @Binds
    abstract fun bindPulseTimer(impl: RealPulseTimer): PulseTimer
}
