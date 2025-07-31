package com.paulcraciunas.settings.application.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
            playSoundOnMove = true,
            preferredTheme = AppSettings.Theme.Wood,
            lightMode = AppSettings.LightMode.System,
            autoPromote = true,
            showBorders = true
        )
    )

    override val appSettings: Flow<AppSettings> = _appSettings.asStateFlow()

    override suspend fun updatePuzzlesDownloaded(downloaded: Boolean) {
        _appSettings.value = _appSettings.value.copy(puzzlesDownloaded = downloaded)
    }

    override suspend fun updateTotalPuzzleCount(count: Int) {
        _appSettings.value = _appSettings.value.copy(totalPuzzleCount = count)
    }

    override suspend fun updateMaxPuzzleRating(maxRating: Int) {
        _appSettings.value = _appSettings.value.copy(maxPuzzleRating = maxRating)
    }

    override suspend fun updatePlaySoundOnMove(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(playSoundOnMove = enabled)
    }

    override suspend fun updatePreferredTheme(theme: AppSettings.Theme) {
        _appSettings.value = _appSettings.value.copy(preferredTheme = theme)
    }

    override suspend fun updateLightMode(mode: AppSettings.LightMode) {
        _appSettings.value = _appSettings.value.copy(lightMode = mode)
    }

    override suspend fun updateAutoPromote(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(autoPromote = enabled)
    }

    override suspend fun updateShowBorders(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(showBorders = enabled)
    }


    /**
     * Resets all settings to their default values.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        _appSettings.value = AppSettings(
            puzzlesDownloaded = false,
            totalPuzzleCount = 0,
            maxPuzzleRating = 0,
            playSoundOnMove = true,
            preferredTheme = AppSettings.Theme.Wood,
            lightMode = AppSettings.LightMode.System,
            autoPromote = true,
            showBorders = true
        )
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

    /**
     * Sets up a scenario where puzzles are already downloaded.
     * Common test setup.
     */
    fun setupPuzzlesDownloaded(count: Int = 1000, maxRating: Int = 2500) {
        _appSettings.value = _appSettings.value.copy(
            puzzlesDownloaded = true,
            totalPuzzleCount = count,
            maxPuzzleRating = maxRating
        )
    }
}
