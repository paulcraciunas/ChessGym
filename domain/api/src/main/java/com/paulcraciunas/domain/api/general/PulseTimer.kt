package com.paulcraciunas.domain.api.general

import kotlinx.coroutines.flow.Flow

interface PulseTimer {
    /**
     * Emits a pulse containing the exact milliseconds passed since the previous tick.
     * Purely cold: stops automatically when cancelled. Prevents scheduling drift.
     */
    fun start(intervalMillis: Long = 100L): Flow<Long>
}
