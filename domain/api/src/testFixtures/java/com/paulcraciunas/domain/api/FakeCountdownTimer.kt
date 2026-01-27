package com.paulcraciunas.domain.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCountdownTimer : CountdownTimer {
    private val _remainingSeconds = MutableStateFlow(0)
    override val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    override val isExpired: Boolean
        get() = _remainingSeconds.value <= 0
    var isRunning = false
        private set

    private var durationSeconds: Int = 0
    private var elapsedSeconds: Int = 0

    override fun set(durationSeconds: Int) {
        if (!isRunning) {
            this.durationSeconds = durationSeconds
            _remainingSeconds.value = durationSeconds
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

    fun advanceTimeBy(seconds: Int) {
        if (isRunning) {
            elapsedSeconds += seconds
            _remainingSeconds.value = maxOf(0, durationSeconds - elapsedSeconds)
        }
        if (_remainingSeconds.value == 0) {
            stop()
        }
    }

    fun advanceUntilIdle() {
        advanceTimeBy(durationSeconds)
    }
}
