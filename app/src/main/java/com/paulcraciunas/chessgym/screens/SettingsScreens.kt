package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcraciunas.chessgym.BuildConfig
import com.paulcraciunas.screens.settings.ui.SettingsScreen
import com.paulcraciunas.screens.settings.vm.SettingsViewModel

@Composable
internal fun Settings(
    onNavigateBack: () -> Unit,
) {
    val vm: SettingsViewModel = hiltViewModel()
    val settingsState by vm.uiState.collectAsStateWithLifecycle()

    val buildVersion = "Version ${BuildConfig.APP_VERSION}-${BuildConfig.BUILD_NUMBER}"

    SettingsScreen(
        uiState = settingsState,
        buildVersion = buildVersion,
        onNavigateBack = onNavigateBack,
        interactions = vm,
    )
}
