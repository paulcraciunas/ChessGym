package com.paulcraciunas.domain.api.general

import kotlinx.coroutines.flow.Flow

/**
 * Emits the remaining time at specified intervals until it hits 0.
 * The timer is purely cold; it starts when collected and stops when cancelled.
 */
interface CountdownTimer {
    fun start(durationMs: Long, intervalMillis: Long = 100L): Flow<Remainder>

    data class Remainder(val seconds: Int, val millis: Int) {
        val isPositive: Boolean
            get() = seconds > 0 || (seconds == 0 && millis > 0)
    }
}
