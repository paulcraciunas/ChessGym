package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.chessgym.navigation.navigateToDashChild
import com.paulcraciunas.screens.boardvis.dashboard.ui.BoardVisDashboardScreen
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisDashboardViewModel
import com.paulcraciunas.screens.boardvis.pieces.ui.KnightPathScreen
import com.paulcraciunas.screens.boardvis.pieces.vm.KnightPathViewModel
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
        onModeSelected = { tabNavController.navigateToDashChild(it) },
    )
}

@Composable
internal fun FindTheSquare(tabNavController: NavHostController) {
    val vm: FindTheSquareViewModel = hiltViewModel()
    val findSquareState by vm.uiState.collectAsStateWithLifecycle()

    FindTheSquareScreen(
        uiState = findSquareState,
        onNavigateBack = { tabNavController.popBackStack(Screen.BoardVisualization, inclusive = false) },
        onSideSelected = vm::onSideSelected,
        onPlayClicked = vm::onPlayClicked,
        onSquareClicked = vm::onSquareClicked,
        onPlayAgain = vm::onPlayAgain,
        onErrorShown = vm::onErrorShown,
    )
}

@Composable
internal fun KnightPath(tabNavController: NavHostController) {
    val vm: KnightPathViewModel = hiltViewModel()
    val knightPathState by vm.uiState.collectAsStateWithLifecycle()

    KnightPathScreen(
        uiState = knightPathState,
        onNavigateBack = { tabNavController.popBackStack(Screen.BoardVisualization, inclusive = false) },
        onPlayClicked = vm::onPlayClicked,
        onSquareClicked = vm::onSquareClicked,
        onPlayAgain = vm::onPlayAgain,
    )
}
