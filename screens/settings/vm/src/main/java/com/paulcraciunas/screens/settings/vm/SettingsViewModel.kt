package com.paulcraciunas.screens.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel(), SettingsScreenInteractor {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                _uiState.value = mapToUiState(settings)
            }
        }
    }

    override fun onHapticFeedbackToggled(isEnabled: Boolean) = update { updateEnableVibrations(isEnabled) }
    override fun onAutoPromoteToggled(isEnabled: Boolean) = update { updateAutoPromote(isEnabled) }
    override fun onShowBordersToggled(isEnabled: Boolean) = update { updateShowBorders(isEnabled) }
    override fun onHighlightLegalMovesToggled(isEnabled: Boolean) = update { updateHighlightLegalMoves(isEnabled) }
    override fun onLightModeSelected(mode: AppSettings.LightMode) = update { updateLightMode(mode) }
    override fun onAnimationsToggled(isEnabled: Boolean) = update { updateEnableAnimations(isEnabled) }

    private fun update(block: suspend AppSettingsRepository.() -> Unit) {
        viewModelScope.launch {
            block(appSettingsRepository)
        }
    }

    private fun mapToUiState(settings: AppSettings): SettingsUiState = SettingsUiState(
        isHapticFeedbackEnabled = settings.enableVibrations,
        isAutoPromoteEnabled = settings.autoPromote,
        isShowBordersEnabled = settings.showBorders,
        isHighlightLegalMovesEnabled = settings.highlightLegalMoves,
        lightMode = settings.lightMode,
        isAnimationsEnabled = settings.enableAnimations,
        isLoading = false,
    )
}
