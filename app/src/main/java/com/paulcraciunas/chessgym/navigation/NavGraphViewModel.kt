package com.paulcraciunas.chessgym.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.chessgym.startup.AppReadinessCoordinator
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NavGraphUiState(
    val appSettings: UiSettings = UiSettings.default(),
    val puzzlesDownloaded: Boolean = false,
    val isLoading: Boolean = true,
    val lightMode: AppSettings.LightMode = AppSettings.LightMode.System,
)

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    appReadinessCoordinator: AppReadinessCoordinator,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<NavGraphUiState> = combine(
        appReadinessCoordinator.uiState,
        appSettingsRepository.appSettings,
    ) { readiness, appSettings ->
        NavGraphUiState(
            appSettings = appSettings.uiSettings(),
            puzzlesDownloaded = readiness.puzzlesDownloaded,
            isLoading = !readiness.isReady,
            lightMode = readiness.lightMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NavGraphUiState(isLoading = true)
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
