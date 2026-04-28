package com.paulcraciunas.chessgym.dsl.setup

import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository

class SettingsSetup(private val repository: FakeAppSettingsRepository) {

    fun puzzlesDownloaded(): SettingsSetup = apply {
        repository.setAppSettings(
            repository.getCurrentSettings().copy(puzzlesDownloaded = true)
        )
    }

    fun puzzlesNotDownloaded(): SettingsSetup = apply {
        repository.setAppSettings(
            repository.getCurrentSettings().copy(puzzlesDownloaded = false)
        )
    }

    fun noAnimations(): SettingsSetup = apply {
        repository.setAppSettings(
            repository.getCurrentSettings().copy(enableAnimations = false)
        )
    }

    fun noBorders(): SettingsSetup = apply {
        repository.setAppSettings(
            repository.getCurrentSettings().copy(showBorders = false)
        )
    }

    fun hasBorders(): SettingsSetup = apply {
        repository.setAppSettings(
            repository.getCurrentSettings().copy(showBorders = true)
        )
    }

    fun darkMode(): SettingsSetup = apply {
        repository.setAppSettings(
            repository.getCurrentSettings().copy(lightMode = AppSettings.LightMode.Dark)
        )
    }

    fun lightMode(): SettingsSetup = apply {
        repository.setAppSettings(
            repository.getCurrentSettings().copy(lightMode = AppSettings.LightMode.Light)
        )
    }
}
