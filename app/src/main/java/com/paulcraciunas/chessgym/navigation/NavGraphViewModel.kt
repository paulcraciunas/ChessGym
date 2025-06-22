package com.paulcraciunas.chessgym.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.settings.application.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NavGraphUiState(
    val puzzlesDownloaded: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavGraphUiState())
    val uiState: StateFlow<NavGraphUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { appSettings ->
                _uiState.value = NavGraphUiState(
                    puzzlesDownloaded = appSettings.puzzlesDownloaded,
                    isLoading = false
                )
            }
        }
    }
} 