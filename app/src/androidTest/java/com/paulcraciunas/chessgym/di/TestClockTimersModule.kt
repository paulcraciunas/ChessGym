package com.paulcraciunas.chessgym.di

import com.paulcraciunas.domain.api.general.BlackTimer
import com.paulcraciunas.domain.api.general.CountdownTimer
import com.paulcraciunas.domain.api.general.DefaultTimer
import com.paulcraciunas.domain.api.general.FakeCountdownTimer
import com.paulcraciunas.domain.api.general.WhiteTimer
import com.paulcraciunas.domain.di.ClockTimersModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ClockTimersModule::class]
)
internal object TestClockTimersModule {
    val whiteTimer = FakeCountdownTimer()
    val blackTimer = FakeCountdownTimer()

    @Provides
    @WhiteTimer
    fun provideWhiteTimer(): CountdownTimer = whiteTimer

    @Provides
    @BlackTimer
    fun provideBlackTimer(): CountdownTimer = blackTimer

    @Provides
    @DefaultTimer
    fun provideDefaultTimer(): CountdownTimer = FakeCountdownTimer()

    fun reset() {
        whiteTimer.stop()
        blackTimer.stop()
    }
}
