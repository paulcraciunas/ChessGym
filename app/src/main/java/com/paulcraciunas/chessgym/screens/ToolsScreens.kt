package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.LocalAppSettings
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
        interactions = vm,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.ToolsDashboard, inclusive = false)
        },
    )
}

@Composable
internal fun AnalysisBoard(
    tabNavController: NavHostController,
    fen: String?,
) {
    val settings = LocalAppSettings.current
    val vm: AnalysisViewModel = hiltViewModel()
    val analysisState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadPosition(fen)
    }

    AnalysisScreen(
        uiState = analysisState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.ToolsDashboard, inclusive = false)
        },
        interactions = vm,
    )
}

@Composable
internal fun ImportGame(tabNavController: NavHostController) {
    val settings = LocalAppSettings.current
    val vm: ImportGameViewModel = hiltViewModel()
    val importState by vm.uiState.collectAsStateWithLifecycle()

    ImportGameScreen(
        uiState = importState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.ToolsDashboard, inclusive = false)
        },
        interactions = vm,
    )
}
