package com.paulcraciunas.settings.application.api

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val appSettings: Flow<AppSettings>

    suspend fun updatePuzzlesDownloaded(downloaded: Boolean)
    suspend fun updateTotalPuzzleCount(count: Int)
    suspend fun updateMaxPuzzleRating(maxRating: Int)
    suspend fun updateMinPuzzleRating(minRating: Int)
    suspend fun updatePlaySound(enabled: Boolean)
    suspend fun updateLightMode(mode: AppSettings.LightMode)
    suspend fun updateAutoPromote(enabled: Boolean)
    suspend fun updateAutoNextPuzzle(enabled: Boolean)
    suspend fun updateShowBorders(enabled: Boolean)
    suspend fun updateEnableVibrations(enabled: Boolean)
    suspend fun updateHighlightLegalMoves(enabled: Boolean)
    suspend fun updateEnableAnimations(enabled: Boolean)
    suspend fun updateCrashReportingConsent(enabled: Boolean)
    suspend fun updateHasRatedApp(enabled: Boolean)
}