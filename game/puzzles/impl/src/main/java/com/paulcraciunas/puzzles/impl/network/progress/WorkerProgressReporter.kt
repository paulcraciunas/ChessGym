package com.paulcraciunas.puzzles.impl.network.progress

import javax.inject.Inject


class WorkerProgressReporter @Inject constructor() : ProgressReporter {
    private lateinit var report: Reporter

    private var total: Int = 0
    private var current: Int = 0

    private var progress: Int = 0

    override fun init(reporter: Reporter) {
        report = reporter
    }

    override fun onBegin(total: Long) {
        progress = 0
        current = 0
        this.total = if (total != 0L) total.toInt() else 1
    }

    override suspend fun onCompleted(amount: Int) {
        current += amount

        if (current >= total) {
            current = total
            update(100)
        } else {
            update(((current.toFloat() / total) * 100).toInt())
        }
    }

    private suspend fun update(newProgress: Int) {
        if (newProgress != progress) {
            progress = newProgress
            report(progress)
        }
    }
}