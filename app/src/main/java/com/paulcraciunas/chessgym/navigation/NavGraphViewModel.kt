package com.paulcraciunas.chessgym.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.chessgym.startup.AppReadinessCoordinator
import com.paulcraciunas.screens.common.UiSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NavGraphUiState(
    val appSettings: UiSettings = UiSettings.default(),
    val puzzlesDownloaded: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    appReadinessCoordinator: AppReadinessCoordinator,
) : ViewModel() {

    val uiState: StateFlow<NavGraphUiState> = appReadinessCoordinator.uiState
        .map {
            NavGraphUiState(
                appSettings = it.appSettings,
                puzzlesDownloaded = it.puzzlesDownloaded,
                isLoading = !it.isReady,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = NavGraphUiState(isLoading = true)
        )
}
