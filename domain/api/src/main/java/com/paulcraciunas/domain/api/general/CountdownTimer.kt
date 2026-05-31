package com.paulcraciunas.domain.api.general

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Qualifier

@Qualifier
annotation class WhiteTimer
@Qualifier
annotation class BlackTimer
@Qualifier
annotation class DefaultTimer

/**
 * A countdown timer that emits remaining seconds via a [StateFlow].
 *
 * The timer runs continuously once started - it does NOT pause when the app
 * goes to background. This mirrors real-world countdown behavior (like chess clocks).
 *
 * Usage:
 * ```
 * countdownTimer.setInterval(100)
 * countdownTimer.start(scope = viewModelScope, durationSeconds = 180)
 * countdownTimer.remaining.collect { remainder ->
 *     // Update UI
 * }
 * ```
 */
interface CountdownTimer {
    /**
     * Flow of remaining values. Emits every interval set by [setInterval] from durationSeconds down to 0.
     * Collect this flow to receive timer updates.
     */
    val remaining: StateFlow<Remainder>

    /**
     * Returns true when the countdown has reached 0.
     */
    val isExpired: Boolean

    /**
     * Sets the interval between timer updates. By default, it's set at 1000 milliseconds (1 second)
     * Acceptable values are in the range 1...10_000 inclusive. Smaller or larger values will be coerced
     * to the valid range.
     */
    fun setInterval(intervalMillis: Int)

    /**
     * Prepares the timer for a new countdown.
     * If the timer is already running, calling this method has no effect
     */
    fun set(remainder: Remainder)

    /**
     * Prepares the timer for a new countdown.
     * If the timer is already running, calling this method has no effect
     *
     * @param durationSeconds Total duration in seconds
     */
    fun set(durationSeconds: Int)

    /**
     * Starts the countdown with the duration specified by [set].
     * If already running, cancels the previous countdown and starts a new one.
     *
     * @param scope CoroutineScope to run the countdown in (typically viewModelScope)
     */
    fun start(scope: CoroutineScope)

    /**
     * Stops the countdown. The remaining time will freeze at its current value.
     */
    fun stop()

    /**
     * Returns the total elapsed time since [start] was called, in milliseconds.
     */
    fun elapsedMillis(): Long

    data class Remainder(
        val seconds: Int,
        val millis: Int,
    ) {
        fun isPositive(): Boolean = seconds > 0 || (seconds == 0 && millis > 0)

        operator fun minus(other: Remainder): Remainder {
            val thisTotal = seconds * 1000L + millis
            val otherTotal = other.seconds * 1000L + other.millis
            val result = (thisTotal - otherTotal).coerceAtLeast(0)
            return Remainder(
                seconds = (result / 1000).toInt(),
                millis = (result % 1000).toInt(),
            )
        }

        operator fun plus(incrementSeconds: Int): Remainder = Remainder(
            seconds = seconds + incrementSeconds,
            millis = millis,
        )

        fun roundSeconds(): Int = if (millis >= 500) seconds + 1 else seconds
    }
}
