package com.paulcraciunas.screens.tools.dashboard.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ToolsDashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ToolsDashboardUiState())
    val uiState: StateFlow<ToolsDashboardUiState> = _uiState.asStateFlow()

    fun onModeSelected(mode: ToolsMode, onNavigate: (ToolsMode) -> Unit) {
        onNavigate(mode)
    }
}
