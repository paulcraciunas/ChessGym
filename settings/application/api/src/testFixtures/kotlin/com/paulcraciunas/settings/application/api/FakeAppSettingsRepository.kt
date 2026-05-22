package com.paulcraciunas.settings.application.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake implementation of AppSettingsRepository for testing purposes.
 * Provides in-memory storage and allows easy manipulation of settings for tests.
 */
class FakeAppSettingsRepository : AppSettingsRepository {
    private val _appSettings = MutableStateFlow(
        AppSettings(
            puzzlesDownloaded = false,
            totalPuzzleCount = 0,
            maxPuzzleRating = 0,
            minPuzzleRating = 0,
            playSoundOnMove = true,
            lightMode = AppSettings.LightMode.System,
            autoPromote = true,
            autoNextPuzzle = false,
            showBorders = true,
            enableVibrations = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            crashReportingConsent = false,
            hasRatedApp = false,
        )
    )

    override val appSettings: Flow<AppSettings> = _appSettings

    override suspend fun updatePuzzlesDownloaded(downloaded: Boolean) {
        _appSettings.value = _appSettings.value.copy(puzzlesDownloaded = downloaded)
    }

    override suspend fun updateTotalPuzzleCount(count: Int) {
        _appSettings.value = _appSettings.value.copy(totalPuzzleCount = count)
    }

    override suspend fun updateMaxPuzzleRating(maxRating: Int) {
        _appSettings.value = _appSettings.value.copy(maxPuzzleRating = maxRating)
    }

    override suspend fun updateMinPuzzleRating(minRating: Int) {
        _appSettings.value = _appSettings.value.copy(minPuzzleRating = minRating)
    }

    override suspend fun updatePlaySoundOnMove(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(playSoundOnMove = enabled)
    }

    override suspend fun updateLightMode(mode: AppSettings.LightMode) {
        _appSettings.value = _appSettings.value.copy(lightMode = mode)
    }

    override suspend fun updateAutoPromote(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(autoPromote = enabled)
    }

    override suspend fun updateAutoNextPuzzle(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(autoNextPuzzle = enabled)
    }

    override suspend fun updateShowBorders(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(showBorders = enabled)
    }

    override suspend fun updateEnableVibrations(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(enableVibrations = enabled)
    }

    override suspend fun updateHighlightLegalMoves(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(highlightLegalMoves = enabled)
    }

    override suspend fun updateEnableAnimations(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(enableAnimations = enabled)
    }

    override suspend fun updateCrashReportingConsent(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(crashReportingConsent = enabled)
    }

    override suspend fun updateHasRatedApp(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(hasRatedApp = enabled)
    }

    /**
     * Sets all settings at once.
     * Useful for setting up specific test scenarios.
     */
    fun setAppSettings(settings: AppSettings) {
        _appSettings.value = settings
    }

    /**
     * Gets the current settings synchronously.
     * Useful for assertions in tests.
     */
    fun getCurrentSettings(): AppSettings = _appSettings.value

    companion object {
        fun default() = FakeAppSettingsRepository().apply {
            setAppSettings(defaultSettings())
        }

        fun defaultSettings(): AppSettings = AppSettings(
            puzzlesDownloaded = true,
            totalPuzzleCount = 1000,
            maxPuzzleRating = 2500,
            minPuzzleRating = 400,
            playSoundOnMove = true,
            lightMode = AppSettings.LightMode.System,
            autoPromote = true,
            autoNextPuzzle = false,
            showBorders = true,
            enableVibrations = true,
            highlightLegalMoves = true,
            enableAnimations = true,
            crashReportingConsent = false,
            hasRatedApp = false,
        )
    }
}
