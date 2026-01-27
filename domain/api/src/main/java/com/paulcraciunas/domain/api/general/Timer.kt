package com.paulcraciunas.domain.api.general

interface Timer {
    /**
     * Resets the timer. If a previous call to start was made, it will be overridden.
     */
    fun start()

    /**
     * Pauses the timer. Can be used when e.g. user puts the application in bg
     */
    fun pause()

    /**
     * Complement to [pause]. Resumes the timer.
     */
    fun resume()

    /**
     * Returns milliseconds elapsed since [start] was called.
     *
     * Note: Intervals which lie in between calls to [pause] and [resume] are not counted
     */
    fun elapsed(): Long
}
