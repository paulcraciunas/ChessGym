package com.paulcraciunas.domain.api.general

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * Test fake for [CountdownTimer] that gives full control over emissions.
 *
 * Usage:
 * ```
 * val timer = FakeCountdownTimer()
 * val flow = timer.start(durationMs = 5000)
 * // In a coroutine collecting the flow:
 * timer.emit(CountdownTimer.Remainder(4, 0))
 * timer.emit(CountdownTimer.Remainder(3, 0))
 * timer.complete() // terminates the flow
 * ```
 */
class FakeCountdownTimer : CountdownTimer {
    private var channel = Channel<CountdownTimer.Remainder>(Channel.UNLIMITED)

    var lastDurationMs: Long = 0L
        private set
    var lastIntervalMillis: Long = 0L
        private set
    var startCount: Int = 0
        private set
    var isRunning: Boolean = false
        private set

    override fun start(durationMs: Long, intervalMillis: Long): Flow<CountdownTimer.Remainder> {
        channel.cancel()
        channel = Channel(Channel.UNLIMITED)
        lastDurationMs = durationMs
        lastIntervalMillis = intervalMillis
        startCount++
        isRunning = true
        return channel.consumeAsFlow()
    }

    suspend fun emit(remainder: CountdownTimer.Remainder) {
        channel.send(remainder)
    }

    fun advanceUntilIdle() {
        complete()
    }

    fun complete() {
        isRunning = false
        channel.close()
    }
}
