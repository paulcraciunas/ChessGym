package com.paulcraciunas.domain.di

import com.paulcraciunas.domain.api.general.BlackTimer
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.DefaultTimer
import com.paulcraciunas.domain.api.general.WhiteTimer
import com.paulcraciunas.domain.impl.general.RealCountdownTimer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ClockTimersModule {
    @Provides
    @WhiteTimer
    fun whiteCountdownTimer(): CountdownTimer = RealCountdownTimer()

    @Provides
    @BlackTimer
    fun blackCountdownTimer(): CountdownTimer = RealCountdownTimer()

    @Provides
    @DefaultTimer
    fun defaultCountdownTimer(): CountdownTimer = RealCountdownTimer()
}
