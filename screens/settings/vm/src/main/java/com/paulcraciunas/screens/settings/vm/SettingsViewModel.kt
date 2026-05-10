package com.paulcraciunas.screens.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel(), SettingsScreenInteractor {
    val uiState: StateFlow<SettingsUiState> = appSettingsRepository.appSettings
        .map { mapToUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    override fun onHapticFeedbackToggled(isEnabled: Boolean) = update { updateEnableVibrations(isEnabled) }
    override fun onAutoPromoteToggled(isEnabled: Boolean) = update { updateAutoPromote(isEnabled) }
    override fun onShowBordersToggled(isEnabled: Boolean) = update { updateShowBorders(isEnabled) }
    override fun onHighlightLegalMovesToggled(isEnabled: Boolean) = update { updateHighlightLegalMoves(isEnabled) }
    override fun onLightModeSelected(mode: AppSettings.LightMode) = update { updateLightMode(mode) }
    override fun onAnimationsToggled(isEnabled: Boolean) = update { updateEnableAnimations(isEnabled) }
    override fun onCrashReportingToggled(isEnabled: Boolean) = update { updateCrashReportingConsent(isEnabled) }

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
        isCrashReportingEnabled = settings.crashReportingConsent,
        isLoading = false,
    )
}
