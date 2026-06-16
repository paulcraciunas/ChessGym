package com.paulcraciunas.chessgym.di

import com.paulcraciunas.domain.api.general.ControllableClock
import com.paulcraciunas.domain.di.ClockModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Clock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ClockModule::class]
)
internal object TestClockTimersModule {
    val clock = ControllableClock()

    @Provides
    @Singleton
    fun provideClock(): Clock = clock
}
