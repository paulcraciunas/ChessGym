package com.paulcraciunas.chessgym.startup

import com.paulcraciunas.global.qualifiers.ApplicationScope
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

data class ReadinessState(
    val appSettings: UiSettings = UiSettings.default(),
    val isReady: Boolean = false,
    val puzzlesDownloaded: Boolean = false,
)

@Singleton
class AppReadinessCoordinator @Inject constructor(
    userRepository: UserRepository,
    resourcePreWarmer: ResourcePreWarmer,
    settingsProvisioning: AppSettingsProvisioning,
    @ApplicationScope scope: CoroutineScope,
) {
    val uiState: StateFlow<ReadinessState> = combine(
        settingsProvisioning(),
        userRepository.userUpdates().map { true },
        resourcePreWarmer.isComplete,
    ) { appSettings, _, preWarmed ->
        ReadinessState(
            appSettings = appSettings.uiSettings(),
            isReady = preWarmed,
            puzzlesDownloaded = appSettings.puzzlesDownloaded,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ReadinessState()
    )
}

private fun AppSettings.uiSettings(): UiSettings = UiSettings(
    lightMode = when (this.lightMode) {
        AppSettings.LightMode.System -> UiSettings.Mode.System
        AppSettings.LightMode.Light -> UiSettings.Mode.Light
        AppSettings.LightMode.Dark -> UiSettings.Mode.Dark
    },
    autoPromote = this.autoPromote,
    autoNextPuzzle = this.autoNextPuzzle,
    showBorders = this.showBorders,
    enableVibrations = this.enableVibrations,
    highlightLegalMoves = this.highlightLegalMoves,
    enableAnimations = this.enableAnimations,
    playSounds = this.playSounds,
)
