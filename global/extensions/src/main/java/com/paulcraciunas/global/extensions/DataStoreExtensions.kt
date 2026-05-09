package com.paulcraciunas.global.extensions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import java.io.IOException

/**
 * Catches [IOException] when reading from DataStore and emits [emptyPreferences] instead.
 * Other exceptions are re-thrown.
 */
fun Flow<Preferences>.catchIO(
    tag: String,
    message: String = "Error reading from DataStore",
): Flow<Preferences> = this.catch { exception ->
    if (exception is IOException) {
        Timber.w(exception, "[$tag] $message")
        emit(emptyPreferences())
    } else {
        throw exception
    }
}

/**
 * Safely updates the DataStore, catching and logging any [IOException].
 */
suspend fun DataStore<Preferences>.safeUpdate(
    tag: String,
    message: String = "Failed to update preferences",
    transform: suspend (MutablePreferences) -> Unit,
) {
    try {
        edit { preferences ->
            transform(preferences)
        }
    } catch (e: IOException) {
        Timber.w(e, "[$tag] $message")
    }
}

// Helper for safe enum parsing
inline fun <reified T : Enum<T>> String?.toEnumOrDefault(defaultValue: T): T {
    if (this == null) return defaultValue
    return try {
        enumValueOf<T>(this)
    } catch (e: IllegalArgumentException) {
        Timber.w(e, "Failed to parse enum value: %s", this)
        defaultValue
    }
}
