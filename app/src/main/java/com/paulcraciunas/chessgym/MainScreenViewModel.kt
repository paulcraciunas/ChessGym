package com.paulcraciunas.chessgym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.settings.application.AppSettings
import com.paulcraciunas.settings.application.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenUiState(
    val appSettings: AppSettings? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { appSettings ->
                _uiState.value = MainScreenUiState(
                    appSettings = appSettings,
                    isLoading = false
                )
            }
        }
    }
}
