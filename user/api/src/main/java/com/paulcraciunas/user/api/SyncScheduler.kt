package com.paulcraciunas.user.api

interface SyncScheduler {
    fun schedule()
    fun cancel()
}
