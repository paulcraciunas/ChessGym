package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.CountdownTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of [CountdownTimer] that emits remaining seconds via a [StateFlow].
 *
 * Uses [Instant] for wall-clock time tracking to ensure accurate timing
 * even when the device sleeps or the app is backgrounded.
 */
class RealCountdownTimer @Inject constructor() : CountdownTimer {
    private val _remainingSeconds = MutableStateFlow(0)
    override val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    override val isExpired: Boolean
        get() = _remainingSeconds.value <= 0

    private var startInstant: Instant = Instant.now()
    private var durationSeconds: Int = 0
    private var countdownJob: Job? = null

    override fun start(scope: CoroutineScope, durationSeconds: Int) {
        stop()
        this.durationSeconds = durationSeconds
        this.startInstant = Instant.now()
        _remainingSeconds.value = durationSeconds

        countdownJob = scope.launch {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000)
                val elapsed = elapsed().seconds.toInt()
                _remainingSeconds.value = maxOf(0, durationSeconds - elapsed)
            }
        }
    }

    override fun stop() {
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun elapsedMillis(): Long = elapsed().toMillis()

    private fun elapsed(): Duration = Duration.between(startInstant, Instant.now())
}
