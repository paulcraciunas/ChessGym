package com.paulcraciunas.screens.tools.dashboard.vm

data class ToolsDashboardUiState(
    val isLoading: Boolean = false
)

sealed class ToolsMode {
    data object Clock : ToolsMode()
    data object Analysis : ToolsMode()
    data object ImportGame : ToolsMode()
}
