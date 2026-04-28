package com.paulcraciunas.chessgym.dsl.setup

import com.paulcraciunas.domain.api.general.FakeCountdownTimer

class ClockSetup(
    private val whiteTimer: FakeCountdownTimer,
    private val blackTimer: FakeCountdownTimer,
) {
    fun expireWhiteTimer(): ClockSetup = apply {
        whiteTimer.advanceUntilIdle()
    }

    fun expireBlackTimer(): ClockSetup = apply {
        blackTimer.advanceUntilIdle()
    }

    fun reset() {
        whiteTimer.stop()
        blackTimer.stop()
    }
}
