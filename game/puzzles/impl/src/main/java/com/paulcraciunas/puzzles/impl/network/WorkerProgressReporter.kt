package com.paulcraciunas.puzzles.impl.network

import javax.inject.Inject

private typealias Reporter = suspend (Int) -> Unit

class WorkerProgressReporter @Inject constructor() {
    private lateinit var report: Reporter

    private var total: Float = 0.0f
    private var current: Int = 0

    private var progress: Int = 0

    fun init(reporter: Reporter) {
        report = reporter
    }

    fun onBegin(total: Long) {
        progress = 0
        current = 0
        this.total = total.toFloat()
    }

    suspend fun onCompleted(amount: Int) {
        current += amount

        update(((current / total) * 100).toInt())
    }

    private suspend fun update(newProgress: Int) {
        if (newProgress != progress) {
            progress = newProgress
            report(progress)
        }
    }
}
