package com.paulcraciunas.user.api

class FakeSyncState : SyncState {
    private var dirty: Boolean = false
    private var cleared: Boolean = false
    var stale: Boolean = false

    override suspend fun isDirty(): Boolean = dirty

    override suspend fun isStale(): Boolean = stale

    override suspend fun markDirty() {
        dirty = true
        cleared = false
    }

    override suspend fun markClean() {
        dirty = false
    }

    override suspend fun clear() {
        dirty = false
        cleared = true
    }

    fun isCleared(): Boolean = cleared
}
