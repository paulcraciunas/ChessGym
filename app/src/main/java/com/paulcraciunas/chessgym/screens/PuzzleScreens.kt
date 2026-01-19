package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onDrawerToggle: () -> Unit,
) {
    val vm: PuzzleDashboardViewModel = hiltViewModel()
    val puzzleDashboardState by vm.uiState.collectAsStateWithLifecycle()
    PuzzleDashboardScreen(
        state = puzzleDashboardState,
        onDrawerToggle = onDrawerToggle,
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
    val ratedPuzzleState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    RatedPuzzleScreen(
        uiState = ratedPuzzleState,
        showBorders = showBorders,
        onNavigateBack = {
            if (!vm.onNavigateBackPressed()) {
                tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
            }
        },
        interactions = vm,
    )
}
