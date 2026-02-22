package com.paulcraciunas.domain.impl.general

import com.paulcraciunas.domain.api.general.Timer
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class SimpleTimer @Inject constructor() : Timer {
    private var startTime = LocalDateTime.now()
    private val intervals = mutableListOf<Long>()
    private var isPaused = true

    override fun start() {
        intervals.clear()
        resume()
    }

    override fun pause() {
        intervals.add(current())
        isPaused = true
    }

    override fun resume() {
        startTime = LocalDateTime.now()
        isPaused = false
    }

    override fun elapsed(): Long =
        intervals.sum() + if (isPaused) 0 else current()

    private fun current(): Long = Duration.between(startTime, LocalDateTime.now()).toMillis()
}
