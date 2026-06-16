package com.paulcraciunas.screens.boardvis.dashboard.vm

data class BoardVisDashboardUiState(
    val findSquareHighScore: Int = 0,
    val isLoading: Boolean = true
)

sealed class BoardVisMode {
    data object FindTheSquare : BoardVisMode()
    data object KnightPath : BoardVisMode()
}
