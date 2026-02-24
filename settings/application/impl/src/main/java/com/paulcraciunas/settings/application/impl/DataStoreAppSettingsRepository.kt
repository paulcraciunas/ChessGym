package com.paulcraciunas.settings.application.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
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
            minPuzzleRating = preferences[PUZZLES_MIN_RATING] ?: 0,
            playSoundOnMove = preferences[PLAY_SOUND_ON_MOVE] ?: true,
            preferredTheme = preferences[PREFERRED_THEME]?.let { AppSettings.Theme.valueOf(it) }
                ?: AppSettings.Theme.Wood,
            lightMode = preferences[LIGHT_MODE]?.let { AppSettings.LightMode.valueOf(it) }
                ?: AppSettings.LightMode.System,
            autoPromote = preferences[AUTO_PROMOTE] ?: true,
            showBorders = preferences[SHOW_BORDERS] ?: true,
            enableVibrations = preferences[ENABLE_VIBRATIONS] ?: true,
            highlightLegalMoves = preferences[HIGHLIGHT_LEGAL_MOVES] ?: true,
            enableAnimations = preferences[ENABLE_ANIMATIONS] ?: true
        )
    }

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

    private suspend fun <T> DataStore<Preferences>.update(key: Preferences.Key<T>, with: T) {
        edit { preferences ->
            preferences[key] = with
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
    }
}
