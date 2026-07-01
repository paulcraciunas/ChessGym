package com.paulcraciunas.chessgym.repositories

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspends the current coroutine until the [ApiFuture] completes, without blocking a thread.
 * Uses [ApiFutures.addCallback] with a direct executor so the continuation resumes on the
 * callback thread (typically the gRPC executor), which is fine when the caller is already
 * inside a dispatcher-shifted context.
 */
internal suspend fun <T> ApiFuture<T>.await(): T = suspendCancellableCoroutine { cont ->
    ApiFutures.addCallback(
        this,
        object : ApiFutureCallback<T> {
            override fun onSuccess(result: T) {
                cont.resume(result)
            }

            override fun onFailure(t: Throwable) {
                cont.resumeWithException(t)
            }
        },
        MoreExecutors.directExecutor(),
    )
    cont.invokeOnCancellation { cancel(false) }
}
