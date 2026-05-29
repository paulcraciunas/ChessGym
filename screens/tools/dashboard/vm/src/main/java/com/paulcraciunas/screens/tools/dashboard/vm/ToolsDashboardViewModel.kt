package com.paulcraciunas.screens.tools.dashboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * This is actually pointless. I'm keeping it because I sometimes like to go against YAGNI.
 * Keeps the mind sharp.
 */
@HiltViewModel
class ToolsDashboardViewModel @Inject constructor() : ViewModel() {
    val uiState: StateFlow<ToolsDashboardUiState> = flow {
        emit(ToolsDashboardUiState(isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ToolsDashboardUiState(isLoading = true)
    )
}
