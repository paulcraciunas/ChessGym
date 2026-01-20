package com.paulcraciunas.domain.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * A countdown timer that emits remaining seconds via a [StateFlow].
 *
 * The timer runs continuously once started - it does NOT pause when the app
 * goes to background. This mirrors real-world countdown behavior (like chess clocks).
 *
 * Usage:
 * ```
 * countdownTimer.start(scope = viewModelScope, durationSeconds = 180)
 * countdownTimer.remainingSeconds.collect { seconds ->
 *     // Update UI
 * }
 * ```
 */
interface CountdownTimer {
    /**
     * Flow of remaining seconds. Emits every second from durationSeconds down to 0.
     * Collect this flow to receive timer updates.
     */
    val remainingSeconds: StateFlow<Int>

    /**
     * Returns true when the countdown has reached 0.
     */
    val isExpired: Boolean

    /**
     * Starts the countdown with the specified duration.
     * If already running, cancels the previous countdown and starts a new one.
     *
     * @param scope CoroutineScope to run the countdown in (typically viewModelScope)
     * @param durationSeconds Total duration in seconds
     */
    fun start(scope: CoroutineScope, durationSeconds: Int)

    /**
     * Stops the countdown. The remaining time will freeze at its current value.
     */
    fun stop()

    /**
     * Returns the total elapsed time since [start] was called, in milliseconds.
     */
    fun elapsedMillis(): Long
}
