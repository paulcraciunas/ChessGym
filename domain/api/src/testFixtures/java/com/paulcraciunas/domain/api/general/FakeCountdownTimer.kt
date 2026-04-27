package com.paulcraciunas.domain.api.general

import com.paulcraciunas.domain.api.general.CountdownTimer.Remainder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCountdownTimer : CountdownTimer {
    private val _remainingSeconds = MutableStateFlow(Remainder(seconds = 0, millis = 0))
    override val remaining: StateFlow<Remainder> = _remainingSeconds.asStateFlow()

    override val isExpired: Boolean
        get() = !_remainingSeconds.value.isPositive()
    var isRunning = false
        private set

    private var interval: Long = 1000L
    private var elapsedSeconds: Int = 0

    override fun setInterval(intervalMillis: Int) {
        interval = intervalMillis.toLong().coerceIn(1, 10_000) // As specified in the interface
    }

    override fun set(durationSeconds: Int) {
        if (!isRunning) {
            _remainingSeconds.value = Remainder(seconds = durationSeconds, millis = 0)
        }
    }

    override fun set(remainder: Remainder) {
        if (!isRunning) {
            _remainingSeconds.value = remainder
        }
    }

    override fun start(scope: CoroutineScope) {
        stop()
        elapsedSeconds = 0
        isRunning = true
    }

    override fun stop() {
        isRunning = false
    }

    override fun elapsedMillis(): Long = elapsedSeconds * 1000L

    fun advanceTimeBy(seconds: Int, millis: Int = 0) {
        if (isRunning) {
            elapsedSeconds += seconds
            _remainingSeconds.value -= Remainder(seconds = seconds, millis = millis)
        }
        if (!_remainingSeconds.value.isPositive()) {
            stop()
        }
    }

    fun advanceUntilIdle() {
        advanceTimeBy(_remainingSeconds.value.seconds, _remainingSeconds.value.millis)
    }
}
