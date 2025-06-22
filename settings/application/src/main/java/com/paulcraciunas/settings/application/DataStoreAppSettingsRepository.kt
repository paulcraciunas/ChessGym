package com.paulcraciunas.settings.application

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.appSettings: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

internal class DataStoreAppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AppSettingsRepository {
    private val dataStore = context.appSettings

    override val appSettings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            puzzlesDownloaded = preferences[PUZZLES_DOWNLOADED] ?: false,
            totalPuzzleCount = preferences[TOTAL_PUZZLE_COUNT] ?: 0,
            maxPuzzleRating = preferences[PUZZLES_MAX_RATING] ?: 0,
            playSoundOnMove = preferences[PLAY_SOUND_ON_MOVE] ?: true,
            preferredTheme = preferences[PREFERRED_THEME]?.let { AppSettings.Theme.valueOf(it) } ?: AppSettings.Theme.Wood,
            lightMode = preferences[LIGHT_MODE]?.let { AppSettings.LightMode.valueOf(it) } ?: AppSettings.LightMode.System,
            autoPromote = preferences[AUTO_PROMOTE] ?: true
        )
    }

    override suspend fun updatePuzzlesDownloaded(downloaded: Boolean) {
        dataStore.edit { preferences ->
            preferences[PUZZLES_DOWNLOADED] = downloaded
        }
    }

    override suspend fun updateTotalPuzzleCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[TOTAL_PUZZLE_COUNT] = count
        }
    }

    override suspend fun updateMaxPuzzleRating(maxRating: Int) {
        PUZZLES_MAX_RATING
        dataStore.edit { preferences ->
            preferences[PUZZLES_MAX_RATING] = maxRating
        }
    }

    override suspend fun updatePlaySoundOnMove(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PLAY_SOUND_ON_MOVE] = enabled
        }
    }

    override suspend fun updatePreferredTheme(theme: AppSettings.Theme) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_THEME] = theme.name
        }
    }

    override suspend fun updateLightMode(mode: AppSettings.LightMode) {
        dataStore.edit { preferences ->
            preferences[LIGHT_MODE] = mode.name
        }
    }

    override suspend fun updateAutoPromote(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PROMOTE] = enabled
        }
    }

    companion object {
        private val PUZZLES_DOWNLOADED = booleanPreferencesKey("puzzles_downloaded")
        private val TOTAL_PUZZLE_COUNT = intPreferencesKey("total_puzzle_count")
        private val PUZZLES_MAX_RATING = intPreferencesKey("max_puzzle_rating")
        private val PLAY_SOUND_ON_MOVE = booleanPreferencesKey("play_sound_on_move")
        private val PREFERRED_THEME = stringPreferencesKey("preferred_theme")
        private val LIGHT_MODE = stringPreferencesKey("light_mode")
        private val AUTO_PROMOTE = booleanPreferencesKey("auto_promote")
    }
}
