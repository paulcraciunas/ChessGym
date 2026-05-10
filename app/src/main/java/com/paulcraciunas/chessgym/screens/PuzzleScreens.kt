package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.LocalAppSettings
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
internal fun RatedPuzzle(tabNavController: NavHostController) {
    val settings = LocalAppSettings.current
    val vm: RatedPuzzleViewModel = hiltViewModel()
    val ratedPuzzleState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    RatedPuzzleScreen(
        uiState = ratedPuzzleState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            if (!vm.onNavigateBackPressed()) {
                tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
            }
        },
        interactions = vm,
    )
}

@Composable
internal fun PuzzleRush(tabNavController: NavHostController) {
    val settings = LocalAppSettings.current
    val vm: PuzzleRushViewModel = hiltViewModel()
    val puzzleRushState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.navigateToAnalysis.collect { fen ->
            tabNavController.navigate(Screen.Analysis(fen = fen))
        }
    }

    PuzzleRushScreen(
        uiState = puzzleRushState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
        },
        interactions = vm,
    )
}

@Composable
internal fun FailedPuzzles(tabNavController: NavHostController) {
    val settings = LocalAppSettings.current
    val vm: FailedPuzzlesViewModel = hiltViewModel()
    val failedPuzzlesState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    LaunchedEffect(Unit) {
        vm.navigateToAnalysis.collect { fen ->
            tabNavController.navigate(Screen.Analysis(fen = fen))
        }
    }

    FailedPuzzlesScreen(
        uiState = failedPuzzlesState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
        },
        interactions = vm,
    )
}

@Composable
internal fun PuzzleStreak(tabNavController: NavHostController) {
    val settings = LocalAppSettings.current
    val vm: PuzzleStreakViewModel = hiltViewModel()
    val puzzleStreakState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    PuzzleStreakScreen(
        uiState = puzzleStreakState,
        showBorders = settings.showBorders,
        highlightLegalMoves = settings.highlightLegalMoves,
        enableAnimations = settings.enableAnimations,
        onNavigateBack = {
            tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
        },
        interactions = vm,
    )
}
