package com.paulcraciunas.chessgym.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NavGraphUiState(
    val puzzlesDownloaded: Boolean = false,
    val isLoading: Boolean = true,
    val lightMode: AppSettings.LightMode = AppSettings.LightMode.System,
)

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    appSettingsRepository: AppSettingsRepository,
    val achievementNotificationManager: AchievementNotificationManager,
) : ViewModel() {

    val uiState: StateFlow<NavGraphUiState> = appSettingsRepository.appSettings
        .map { appSettings ->
            NavGraphUiState(
                puzzlesDownloaded = appSettings.puzzlesDownloaded,
                isLoading = false,
                lightMode = appSettings.lightMode,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NavGraphUiState(isLoading = true)
        )
}
