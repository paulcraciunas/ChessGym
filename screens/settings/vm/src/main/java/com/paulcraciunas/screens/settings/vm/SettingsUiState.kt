package com.paulcraciunas.screens.settings.vm

import com.paulcraciunas.settings.application.api.AppSettings

data class SettingsUiState(
    val isHapticFeedbackEnabled: Boolean = true,
    val isAutoPromoteEnabled: Boolean = true,
    val isShowBordersEnabled: Boolean = true,
    val isHighlightLegalMovesEnabled: Boolean = true,
    val lightMode: AppSettings.LightMode = AppSettings.LightMode.System,
    val isAnimationsEnabled: Boolean = true,
    val isLoading: Boolean = true,
)
