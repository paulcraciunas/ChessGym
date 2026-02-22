package com.paulcraciunas.domain.api.general

class FakeTimer : Timer {
    private var currentTime = 0L
    private val intervals = mutableListOf<Long>()
    private var isPaused = true

    override fun start() {
        intervals.clear()
        resume()
    }

    override fun pause() {
        intervals.add(currentTime)
        isPaused = true
    }

    override fun resume() {
        currentTime = 0L
        isPaused = false
    }

    override fun elapsed(): Long =
        intervals.sum() + if (isPaused) 0 else currentTime

    fun isRunning() = !isPaused

    fun advanceTimeBy(millis: Long) {
        if (!isPaused) {
            currentTime += millis
        }
    }
}
