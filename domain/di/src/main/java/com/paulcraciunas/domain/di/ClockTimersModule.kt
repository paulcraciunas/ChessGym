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
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClockTimersModule {
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    @WhiteTimer
    fun whiteCountdownTimer(clock: Clock): CountdownTimer = RealCountdownTimer(clock)

    @Provides
    @BlackTimer
    fun blackCountdownTimer(clock: Clock): CountdownTimer = RealCountdownTimer(clock)

    @Provides
    @DefaultTimer
    fun defaultCountdownTimer(clock: Clock): CountdownTimer = RealCountdownTimer(clock)
}
