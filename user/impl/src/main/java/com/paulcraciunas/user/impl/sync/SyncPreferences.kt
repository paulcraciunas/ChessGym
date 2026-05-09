package com.paulcraciunas.user.impl.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paulcraciunas.global.extensions.catchIO
import com.paulcraciunas.global.extensions.safeUpdate
import com.paulcraciunas.user.api.SyncState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPreferences @Inject constructor(
    @param: ApplicationContext private val context: Context,
    private val clock: Clock,
) : SyncState {
    private val dataStore: DataStore<Preferences> = context.syncDataStore

    private val syncData: Flow<SyncData> =
        dataStore.data
        .catchIO(TAG)
        .map { preferences ->
            SyncData(
                isDirty = preferences[KEY_IS_DIRTY] ?: false,
                lastSyncTimestamp = preferences[KEY_LAST_SYNC_TIMESTAMP]?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH
            )
        }

    override suspend fun isDirty(): Boolean = syncData.first().isDirty

    override suspend fun isStale(): Boolean {
        val lastSync = syncData.first().lastSyncTimestamp
        return Duration.between(lastSync, clock.instant()) > STALENESS_THRESHOLD
    }

    override suspend fun markDirty() {
        dataStore.safeUpdate(TAG) { preferences ->
            preferences[KEY_IS_DIRTY] = true
        }
    }

    override suspend fun markClean() {
        dataStore.safeUpdate(TAG) { preferences ->
            preferences[KEY_IS_DIRTY] = false
            preferences[KEY_LAST_SYNC_TIMESTAMP] = clock.instant().toEpochMilli()
        }
    }

    override suspend fun clear() {
        dataStore.safeUpdate(TAG) { it.clear() }
    }

    private data class SyncData(val isDirty: Boolean, val lastSyncTimestamp: Instant)

    companion object {
        private const val TAG = "SyncPreferences"
        private val KEY_IS_DIRTY = booleanPreferencesKey("sync_is_dirty")
        private val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("sync_last_timestamp")
        private val STALENESS_THRESHOLD = Duration.ofHours(1)
    }

}

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_sync_preferences"
)
