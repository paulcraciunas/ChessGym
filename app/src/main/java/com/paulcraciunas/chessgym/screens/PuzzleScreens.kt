package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardScreen
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleDashboardViewModel
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleMode
import com.paulcraciunas.screens.puzzles.rated.ui.RatedPuzzleScreen
import com.paulcraciunas.screens.puzzles.rated.vm.RatedPuzzleViewModel

@Composable
internal fun PuzzleDashboard(
    tabNavController: NavHostController,
) {
    val vm: PuzzleDashboardViewModel = hiltViewModel()
    val puzzleDashboardState by vm.uiState.collectAsState()
    PuzzleDashboardScreen(
        state = puzzleDashboardState,
        onPuzzleModeSelected = { mode ->
            vm.onPuzzleModeSelected(mode) { puzzleMode ->
                // Navigate to specific puzzle screens based on mode
                when (puzzleMode) {
                    PuzzleMode.RatedPuzzle -> {
                        tabNavController.navigate(Screen.RatedPuzzle)
                    }
                    else -> {
                        // TODO: Handle other puzzle modes when implemented
                    }
                }
            }
        },
        onPuzzleRushTimeChanged = vm::onPuzzleRushTimeChanged,
        onPuzzleRushMistakesChanged = vm::onPuzzleRushMistakesChanged
    )
}

@Composable
internal fun RatedPuzzle(
    tabNavController: NavHostController,
    showBorders: Boolean,
) {
    val vm: RatedPuzzleViewModel = hiltViewModel()
    val ratedPuzzleState by vm.uiState.collectAsState()
    RatedPuzzleScreen(
        uiState = ratedPuzzleState,
        showBorders = showBorders,
        onNavigateBack = {
            if (!vm.onNavigateBackPressed()) {
                tabNavController.popBackStack()
            }
        },
        interactions = vm,
    )
}
