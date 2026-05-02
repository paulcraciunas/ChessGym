package com.paulcraciunas.settings.application.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

private val Context.appSettings: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class DataStoreAppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AppSettingsRepository {
    private val dataStore = context.appSettings

    override val appSettings: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.w(exception, "Error reading app settings from DataStore")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences.toAppSettings() }

    override suspend fun updatePuzzlesDownloaded(downloaded: Boolean) = dataStore.update(PUZZLES_DOWNLOADED, downloaded)
    override suspend fun updateTotalPuzzleCount(count: Int) = dataStore.update(TOTAL_PUZZLE_COUNT, count)
    override suspend fun updateMaxPuzzleRating(maxRating: Int) = dataStore.update(PUZZLES_MAX_RATING, maxRating)
    override suspend fun updateMinPuzzleRating(minRating: Int) = dataStore.update(PUZZLES_MIN_RATING, minRating)
    override suspend fun updatePlaySoundOnMove(enabled: Boolean) = dataStore.update(PLAY_SOUND_ON_MOVE, enabled)
    override suspend fun updatePreferredTheme(theme: AppSettings.Theme) = dataStore.update(PREFERRED_THEME, theme.name)
    override suspend fun updateLightMode(mode: AppSettings.LightMode) = dataStore.update(LIGHT_MODE, mode.name)
    override suspend fun updateAutoPromote(enabled: Boolean) = dataStore.update(AUTO_PROMOTE, enabled)
    override suspend fun updateShowBorders(enabled: Boolean) = dataStore.update(SHOW_BORDERS, enabled)
    override suspend fun updateEnableVibrations(enabled: Boolean) = dataStore.update(ENABLE_VIBRATIONS, enabled)
    override suspend fun updateHighlightLegalMoves(enabled: Boolean) = dataStore.update(HIGHLIGHT_LEGAL_MOVES, enabled)
    override suspend fun updateEnableAnimations(enabled: Boolean) = dataStore.update(ENABLE_ANIMATIONS, enabled)
    override suspend fun updateCrashReportingConsent(enabled: Boolean) = dataStore.update(CRASH_REPORTING_CONSENT, enabled)

    private suspend fun <T> DataStore<Preferences>.update(key: Preferences.Key<T>, with: T) {
        try {
            edit { preferences ->
                preferences[key] = with
            }
        } catch (e: IOException) {
            Timber.w(e, "Failed to update setting: %s", key.name)
            throw e
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            puzzlesDownloaded = this[PUZZLES_DOWNLOADED] ?: false,
            totalPuzzleCount = this[TOTAL_PUZZLE_COUNT] ?: 0,
            maxPuzzleRating = this[PUZZLES_MAX_RATING] ?: 0,
            minPuzzleRating = this[PUZZLES_MIN_RATING] ?: 0,
            playSoundOnMove = this[PLAY_SOUND_ON_MOVE] ?: true,
            preferredTheme = this[PREFERRED_THEME].toEnumOrDefault(AppSettings.Theme.Wood),
            lightMode = this[LIGHT_MODE].toEnumOrDefault(AppSettings.LightMode.System),
            autoPromote = this[AUTO_PROMOTE] ?: true,
            showBorders = this[SHOW_BORDERS] ?: true,
            enableVibrations = this[ENABLE_VIBRATIONS] ?: true,
            highlightLegalMoves = this[HIGHLIGHT_LEGAL_MOVES] ?: true,
            enableAnimations = this[ENABLE_ANIMATIONS] ?: true,
            crashReportingConsent = this[CRASH_REPORTING_CONSENT] ?: false
        )
    }

    // Helper for safe enum parsing
    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(defaultValue: T): T {
        if (this == null) return defaultValue
        return try {
            enumValueOf<T>(this)
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Failed to parse enum value: %s", this)
            defaultValue
        }
    }

    companion object {
        private val PUZZLES_DOWNLOADED = booleanPreferencesKey("puzzles_downloaded")
        private val TOTAL_PUZZLE_COUNT = intPreferencesKey("total_puzzle_count")
        private val PUZZLES_MAX_RATING = intPreferencesKey("max_puzzle_rating")
        private val PUZZLES_MIN_RATING = intPreferencesKey("min_puzzle_rating")
        private val PLAY_SOUND_ON_MOVE = booleanPreferencesKey("play_sound_on_move")
        private val PREFERRED_THEME = stringPreferencesKey("preferred_theme")
        private val LIGHT_MODE = stringPreferencesKey("light_mode")
        private val AUTO_PROMOTE = booleanPreferencesKey("auto_promote")
        private val SHOW_BORDERS = booleanPreferencesKey("show_borders")
        private val ENABLE_VIBRATIONS = booleanPreferencesKey("enable_vibrations")
        private val HIGHLIGHT_LEGAL_MOVES = booleanPreferencesKey("highlight_legal_moves")
        private val ENABLE_ANIMATIONS = booleanPreferencesKey("enable_animations")
        private val CRASH_REPORTING_CONSENT = booleanPreferencesKey("crash_reporting_consent")
    }
}
