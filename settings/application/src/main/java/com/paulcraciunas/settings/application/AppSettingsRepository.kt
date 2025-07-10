package com.paulcraciunas.settings.application

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val appSettings: Flow<AppSettings>

    suspend fun updatePuzzlesDownloaded(downloaded: Boolean)
    suspend fun updateTotalPuzzleCount(count: Int)
    suspend fun updateMaxPuzzleRating(maxRating: Int)
    suspend fun updatePlaySoundOnMove(enabled: Boolean)
    suspend fun updatePreferredTheme(theme: AppSettings.Theme)
    suspend fun updateLightMode(mode: AppSettings.LightMode)
    suspend fun updateAutoPromote(enabled: Boolean)
    suspend fun updateShowBorders(enabled: Boolean)
}
