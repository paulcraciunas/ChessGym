package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.screens.tools.analysis.ui.AnalysisScreen
import com.paulcraciunas.screens.tools.analysis.vm.AnalysisViewModel
import com.paulcraciunas.screens.tools.clock.ui.ClockScreen
import com.paulcraciunas.screens.tools.clock.vm.ClockViewModel
import com.paulcraciunas.screens.tools.dashboard.ui.ToolsDashboardScreen
import com.paulcraciunas.screens.tools.dashboard.vm.ToolsDashboardViewModel
import com.paulcraciunas.screens.tools.dashboard.vm.ToolsMode
import com.paulcraciunas.screens.tools.importgame.ui.ImportGameScreen
import com.paulcraciunas.screens.tools.importgame.vm.ImportGameViewModel

@Composable
internal fun ToolsDashboard(
    tabNavController: NavHostController,
    onDrawerToggle: () -> Unit,
) {
    val vm: ToolsDashboardViewModel = hiltViewModel()
    val dashboardState by vm.uiState.collectAsStateWithLifecycle()
    ToolsDashboardScreen(
        state = dashboardState,
        onDrawerToggle = onDrawerToggle,
        onModeSelected = { mode ->
            vm.onModeSelected(mode) { toolsMode ->
                when (toolsMode) {
                    ToolsMode.Clock -> {
                        tabNavController.navigate(Screen.Clock)
                    }
                    ToolsMode.Analysis -> {
                        tabNavController.navigate(Screen.Analysis())
                    }
                    ToolsMode.ImportGame -> {
                        tabNavController.navigate(Screen.ImportGame)
                    }
                }
            }
        },
    )
}

@Composable
internal fun ChessClock(
    tabNavController: NavHostController,
) {
    val vm: ClockViewModel = hiltViewModel()
    val clockState by vm.uiState.collectAsStateWithLifecycle()

    ClockScreen(
        uiState = clockState,
        onNavigateBack = { tabNavController.popBackStack(Screen.ToolsDashboard, inclusive = false) },
        onWhiteTapped = vm::onWhiteTapped,
        onBlackTapped = vm::onBlackTapped,
        onStop = vm::onStop,
        onNewGame = vm::onNewGame,
        onTimeSelected = vm::onTimeSelected,
        onIncrementSelected = vm::onIncrementSelected,
    )
}

@Composable
internal fun AnalysisBoard(
    tabNavController: NavHostController,
    fen: String?,
    firstMove: String? = null,
) {
    val vm: AnalysisViewModel = hiltViewModel()
    val analysisState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadPosition(fen, firstMove)
    }

    AnalysisScreen(
        uiState = analysisState,
        onNavigateBack = { tabNavController.popBackStack(Screen.ToolsDashboard, inclusive = false) },
        onSquareClicked = vm::onSquareClicked,
        onPromote = vm::onPromote,
        onJumpToStart = vm::onJumpToStart,
        onPreviousMove = vm::onPreviousMove,
        onNextMove = vm::onNextMove,
        onJumpToEnd = vm::onJumpToEnd,
    )
}

@Composable
internal fun ImportGame(tabNavController: NavHostController) {
    val vm: ImportGameViewModel = hiltViewModel()
    val importState by vm.uiState.collectAsStateWithLifecycle()

    ImportGameScreen(
        uiState = importState,
        onNavigateBack = { tabNavController.popBackStack(Screen.ToolsDashboard, inclusive = false) },
        onFenClicked = vm::onFenClicked,
        onPgnClicked = vm::onPgnClicked,
        onImport = vm::onImport,
        onDismissDialog = vm::onDismissDialog,
        onSquareClicked = vm::onSquareClicked,
        onPromote = vm::onPromote,
        onJumpToStart = vm::onJumpToStart,
        onPreviousMove = vm::onPreviousMove,
        onNextMove = vm::onNextMove,
        onJumpToEnd = vm::onJumpToEnd,
    )
}
