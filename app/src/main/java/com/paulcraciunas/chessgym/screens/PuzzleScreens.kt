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
import com.paulcraciunas.screens.puzzles.failed.ui.FailedPuzzlesScreen
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesViewModel
import com.paulcraciunas.screens.puzzles.rush.ui.PuzzleRushScreen
import com.paulcraciunas.screens.puzzles.rush.vm.PuzzleRushViewModel
import com.paulcraciunas.screens.puzzles.streak.ui.PuzzleStreakScreen
import com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakViewModel

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
                    PuzzleMode.PuzzleRush -> {
                        tabNavController.navigate(Screen.PuzzleRush)
                    }
                    PuzzleMode.FailedPuzzles -> {
                        tabNavController.navigate(Screen.FailedPuzzles)
                    }
                    PuzzleMode.PuzzleStreak -> {
                        tabNavController.navigate(Screen.PuzzleStreak)
                    }
                }
            }
        },
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

@Composable
internal fun PuzzleRush(
    tabNavController: NavHostController,
    showBorders: Boolean,
) {
    val vm: PuzzleRushViewModel = hiltViewModel()
    val puzzleRushState by vm.uiState.collectAsStateWithLifecycle()

    PuzzleRushScreen(
        uiState = puzzleRushState,
        showBorders = showBorders,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
        },
        interactions = vm,
    )
}

@Composable
internal fun FailedPuzzles(
    tabNavController: NavHostController,
    showBorders: Boolean,
) {
    val vm: FailedPuzzlesViewModel = hiltViewModel()
    val failedPuzzlesState by vm.uiState.collectAsStateWithLifecycle()

    FailedPuzzlesScreen(
        uiState = failedPuzzlesState,
        showBorders = showBorders,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
        },
        interactions = vm,
    )
}

@Composable
internal fun PuzzleStreak(
    tabNavController: NavHostController,
    showBorders: Boolean,
) {
    val vm: PuzzleStreakViewModel = hiltViewModel()
    val puzzleStreakState by vm.uiState.collectAsStateWithLifecycle()

    PuzzleStreakScreen(
        uiState = puzzleStreakState,
        showBorders = showBorders,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
        },
        interactions = vm,
    )
}
