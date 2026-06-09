package com.paulcraciunas.domain.api.general

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * Test fake for [PulseTimer] that gives full control over pulse emissions.
 *
 * Usage:
 * ```
 * val timer = FakePulseTimer()
 * val flow = timer.start(intervalMillis = 100)
 * // In a coroutine collecting the flow:
 * timer.emit(100L) // simulate 100ms elapsed
 * timer.emit(102L) // simulate 102ms elapsed (slight drift)
 * timer.complete() // terminates the flow
 * ```
 */
class FakePulseTimer : PulseTimer {
    private var channel = Channel<Long>(Channel.UNLIMITED)

    var lastIntervalMillis: Long = 0L
        private set
    var startCount: Int = 0
        private set

    override fun start(intervalMillis: Long): Flow<Long> {
        channel.cancel()
        channel = Channel(Channel.UNLIMITED)
        lastIntervalMillis = intervalMillis
        startCount++
        return channel.consumeAsFlow()
    }

    suspend fun emit(elapsedMillis: Long) {
        channel.send(elapsedMillis)
    }

    fun complete() {
        channel.close()
    }

    fun reset() {
        channel.cancel()
        channel = Channel(Channel.UNLIMITED)
        startCount = 0
        lastIntervalMillis = 0L
    }
}
