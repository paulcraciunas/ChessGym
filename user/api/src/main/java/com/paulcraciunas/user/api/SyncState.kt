package com.paulcraciunas.user.api

interface SyncState {
    suspend fun isDirty(): Boolean
    suspend fun isStale(): Boolean
    suspend fun markDirty()
    suspend fun markClean()
    suspend fun clear()
}
