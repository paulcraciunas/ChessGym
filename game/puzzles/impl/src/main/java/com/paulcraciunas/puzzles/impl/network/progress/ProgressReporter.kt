package com.paulcraciunas.puzzles.impl.network.progress

internal typealias Reporter = suspend (Int) -> Unit

interface ProgressReporter {
    fun init(reporter: Reporter)
    fun onBegin(total: Long)
    suspend fun onCompleted(amount: Int)
}
