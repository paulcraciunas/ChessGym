package com.paulcraciunas.screens.settings.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.settings.application.api.AppSettings

@Immutable
data class SettingsUiState(
    val isSoundEnabled: Boolean = true,
    val isHapticFeedbackEnabled: Boolean = true,
    val isAutoPromoteEnabled: Boolean = true,
    val isAutoNextPuzzleEnabled: Boolean = false,
    val isShowBordersEnabled: Boolean = true,
    val isHighlightLegalMovesEnabled: Boolean = true,
    val lightMode: AppSettings.LightMode = AppSettings.LightMode.System,
    val isAnimationsEnabled: Boolean = true,
    val isCrashReportingEnabled: Boolean = false,
    val isLoading: Boolean = true,
)
