package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.screens.puzzles.dashboard.ui.PuzzleDashboardScreen
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleDashboardViewModel
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleMode
import com.paulcraciunas.screens.puzzles.failed.ui.FailedPuzzlesScreen
import com.paulcraciunas.screens.puzzles.failed.vm.FailedPuzzlesViewModel
import com.paulcraciunas.screens.puzzles.rated.ui.RatedPuzzleScreen
import com.paulcraciunas.screens.puzzles.rated.vm.RatedPuzzleViewModel
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
    val vm: RatedPuzzleViewModel = hiltViewModel()
    val ratedPuzzleState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    RatedPuzzleScreen(
        uiState = ratedPuzzleState,
        onNavigateBack = {
            if (!vm.onNavigateBackPressed()) {
                tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false)
            }
        },
        onSquareClicked = vm::onSquareClicked,
        onPromote = vm::onPromote,
        onHintRequested = vm::onHintRequested,
        onAbandon = vm::onAbandon,
        onAbandonConfirmed = vm::onAbandonConfirmed,
        onAbandonDismissed = vm::onAbandonDismissed,
        onNextPuzzle = vm::onNextPuzzle,
    )
}

@Composable
internal fun PuzzleRush(tabNavController: NavHostController) {
    val vm: PuzzleRushViewModel = hiltViewModel()
    val puzzleRushState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.navigateToAnalysis.collect { data ->
            tabNavController.navigate(Screen.Analysis(fen = data.fen, firstMove = data.firstMove))
        }
    }

    PuzzleRushScreen(
        uiState = puzzleRushState,
        onSquareClicked = vm::onSquareClicked,
        onPromote = vm::onPromote,
        onPlayAgain = vm::onPlayAgain,
        onDismissSummary = vm::onDismissSummary,
        onAnalyzeFailedPuzzle = vm::onAnalyzeFailedPuzzle,
        onNavigateBack = { tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false) },
    )
}

@Composable
internal fun FailedPuzzles(tabNavController: NavHostController) {
    val vm: FailedPuzzlesViewModel = hiltViewModel()
    val failedPuzzlesState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    LaunchedEffect(Unit) {
        vm.navigateToAnalysis.collect { data ->
            tabNavController.navigate(Screen.Analysis(fen = data.fen, firstMove = data.firstMove))
        }
    }

    FailedPuzzlesScreen(
        uiState = failedPuzzlesState,
        onNavigateBack = { tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false) },
        onSquareClicked = vm::onSquareClicked,
        onPromote = vm::onPromote,
        onDismissCompletion = vm::onDismissCompletion,
        onAnalyzeFailedPuzzle = vm::onAnalyzeFailedPuzzle,
    )
}

@Composable
internal fun PuzzleStreak(tabNavController: NavHostController) {
    val vm: PuzzleStreakViewModel = hiltViewModel()
    val puzzleStreakState by vm.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onStart() }

    PuzzleStreakScreen(
        uiState = puzzleStreakState,
        onNavigateBack = { tabNavController.popBackStack(Screen.PuzzleDashboard, inclusive = false) },
        onSquareClicked = vm::onSquareClicked,
        onPromote = vm::onPromote,
        onHintRequested = vm::onHintRequested,
        onAbandon = vm::onAbandon,
        onAbandonConfirmed = vm::onAbandonConfirmed,
        onAbandonDismissed = vm::onAbandonDismissed,
        onNewStreak = vm::onNewStreak,
        onNextPuzzle = vm::onNextPuzzle,
        onDismissSummary = vm::onDismissSummary,
    )
}
