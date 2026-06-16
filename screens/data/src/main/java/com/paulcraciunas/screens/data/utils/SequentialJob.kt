package com.paulcraciunas.screens.data.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SequentialJob(private val scope: CoroutineScope) {
    private var job: Job? = null

    fun launch(context: CoroutineContext = EmptyCoroutineContext,
               block: suspend CoroutineScope.() -> Unit) {
        job?.cancel()
        job = scope.launch(context = context, block = block)
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
}
