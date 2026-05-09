package com.paulcraciunas.user.api

class FakeSyncScheduler : SyncScheduler {
    var scheduleCount: Int = 0
        private set

    var cancelCount: Int = 0
        private set

    override fun schedule() {
        scheduleCount++
    }

    override fun cancel() {
        scheduleCount--
        cancelCount++
    }
}
