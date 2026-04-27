package com.paulcraciunas.domain.impl.general

import com.paulcraciunas.domain.api.general.CountdownTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of [CountdownTimer] that emits remaining time via a [StateFlow].
 *
 * Uses [Clock] for time tracking to ensure accurate timing
 * even when the device sleeps or the app is backgrounded.
 */
class RealCountdownTimer @Inject constructor(
    private val clock: Clock = Clock.systemUTC(),
) : CountdownTimer {
    private val _remaining = MutableStateFlow(CountdownTimer.Remainder(seconds = 0, millis = 0))
    override val remaining: StateFlow<CountdownTimer.Remainder> = _remaining.asStateFlow()

    override val isExpired: Boolean
        get() = !_remaining.value.isPositive()

    private var interval: Long = DEFAULT_INTERVAL
    private var lastTickInstant: Instant = clock.instant()
    private var startInstant: Instant = clock.instant()
    private var countdownJob: Job? = null

    override fun setInterval(intervalMillis: Int) {
        interval = intervalMillis.toLong().coerceIn(MIN_INTERVAL, MAX_INTERVAL)
    }

    override fun set(durationSeconds: Int) {
        if (countdownJob == null) {
            _remaining.value = CountdownTimer.Remainder(seconds = durationSeconds, millis = 0)
        }
    }

    override fun set(remainder: CountdownTimer.Remainder) {
        if (countdownJob == null) {
            _remaining.value = remainder
        }
    }

    override fun start(scope: CoroutineScope) {
        stop()
        val now = clock.instant()
        this.startInstant = now
        this.lastTickInstant = now

        countdownJob = scope.launch {
            while (isActive && _remaining.value.isPositive()) {
                delay(interval)
                val now = clock.instant()
                val delta = Duration.between(lastTickInstant, now)
                lastTickInstant = now
                val deltaSeconds = delta.seconds.toInt()
                val deltaMillis = (delta.toMillis() % 1000).toInt()
                _remaining.value -= CountdownTimer.Remainder(seconds = deltaSeconds, millis = deltaMillis)
            }
        }
    }

    override fun stop() {
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun elapsedMillis(): Long = Duration.between(startInstant, clock.instant()).toMillis()

    companion object {
        private const val DEFAULT_INTERVAL = 1000L
        private const val MIN_INTERVAL = 1L
        private const val MAX_INTERVAL = 10_000L
    }
}
