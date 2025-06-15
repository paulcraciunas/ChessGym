package com.paulcraciunas.puzzles.impl.network.fakes

import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.puzzles.impl.network.progress.Reporter

internal class FakeProgressReporter : ProgressReporter {
    var total: Long = 0
    val progressUpdates = mutableListOf<Int>()

    override fun init(reporter: Reporter) {
    }

    override fun onBegin(total: Long) {
        this.total = total
    }

    override suspend fun onCompleted(amount: Int) {
        progressUpdates.add(amount)
    }
}
