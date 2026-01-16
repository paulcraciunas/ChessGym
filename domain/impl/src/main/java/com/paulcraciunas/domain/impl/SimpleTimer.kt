package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.Timer
import java.time.Duration
import java.time.LocalDateTime

class SimpleTimer : Timer {
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
