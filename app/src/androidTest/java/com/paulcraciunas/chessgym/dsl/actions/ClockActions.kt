package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.chessgym.di.TestClockTimersModule
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class ClockActions(private val rule: ComposeTestRule) {
    fun advanceTimeBy(millis: Long): ClockActions = apply {
        TestClockTimersModule.clock.advanceBy(millis)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        rule.waitForIdle()
    }

    fun advanceSeconds(seconds: Int): ClockActions = apply {
        repeat(seconds) { // This is important. If we just bulk advance at once, the events won't properly drain
            advanceTimeBy(1000L)
        }
    }

    fun advanceMinutes(minutes: Int): ClockActions = apply {
        repeat(minutes) {
            advanceTimeBy(60 * 1000L)
        }
    }
}
