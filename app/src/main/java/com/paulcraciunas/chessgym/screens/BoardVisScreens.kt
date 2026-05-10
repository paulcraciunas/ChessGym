package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.LocalAppSettings
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.screens.boardvis.dashboard.ui.BoardVisDashboardScreen
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisDashboardViewModel
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisMode
import com.paulcraciunas.screens.boardvis.pieces.ui.MoveThePieceScreen
import com.paulcraciunas.screens.boardvis.pieces.vm.MoveThePieceViewModel
import com.paulcraciunas.screens.boardvis.squares.ui.FindTheSquareScreen
import com.paulcraciunas.screens.boardvis.squares.vm.FindTheSquareViewModel

@Composable
internal fun BoardVisDashboard(
    tabNavController: NavHostController,
    onDrawerToggle: () -> Unit,
) {
    val vm: BoardVisDashboardViewModel = hiltViewModel()
    val dashboardState by vm.uiState.collectAsStateWithLifecycle()
    BoardVisDashboardScreen(
        state = dashboardState,
        onDrawerToggle = onDrawerToggle,
        onModeSelected = { mode ->
            vm.onModeSelected(mode) { visMode ->
                when (visMode) {
                    BoardVisMode.FindTheSquare -> {
                        tabNavController.navigate(Screen.FindTheSquare)
                    }
                    BoardVisMode.MoveThePiece -> {
                        tabNavController.navigate(Screen.MoveThePiece)
                    }
                }
            }
        },
    )
}

@Composable
internal fun FindTheSquare(tabNavController: NavHostController) {
    val settings = LocalAppSettings.current
    val vm: FindTheSquareViewModel = hiltViewModel()
    val findSquareState by vm.uiState.collectAsStateWithLifecycle()

    FindTheSquareScreen(
        uiState = findSquareState,
        showBorders = settings.showBorders,
        enableVibrations = settings.enableVibrations,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.BoardVisualization, inclusive = false)
        },
        interactions = vm,
    )
}

@Composable
internal fun MoveThePiece(tabNavController: NavHostController) {
    val settings = LocalAppSettings.current
    val vm: MoveThePieceViewModel = hiltViewModel()
    val moveThePieceState by vm.uiState.collectAsStateWithLifecycle()

    MoveThePieceScreen(
        uiState = moveThePieceState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.BoardVisualization, inclusive = false)
        },
        interactions = vm,
    )
}
