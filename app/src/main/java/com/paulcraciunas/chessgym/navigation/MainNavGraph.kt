package com.paulcraciunas.chessgym.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.paulcraciunas.chessgym.animations.enter
import com.paulcraciunas.chessgym.animations.exit
import com.paulcraciunas.chessgym.debug.DebugMenuProvider
import com.paulcraciunas.chessgym.screens.About
import com.paulcraciunas.chessgym.screens.AboutDetail
import com.paulcraciunas.chessgym.screens.Achievements
import com.paulcraciunas.chessgym.screens.AnalysisBoard
import com.paulcraciunas.chessgym.screens.BlindMode
import com.paulcraciunas.chessgym.screens.BoardVisDashboard
import com.paulcraciunas.chessgym.screens.ChessClock
import com.paulcraciunas.chessgym.screens.FailedPuzzles
import com.paulcraciunas.chessgym.screens.FindTheSquare
import com.paulcraciunas.chessgym.screens.Home
import com.paulcraciunas.chessgym.screens.ImportGame
import com.paulcraciunas.chessgym.screens.KnightPath
import com.paulcraciunas.chessgym.screens.PuzzleDashboard
import com.paulcraciunas.chessgym.screens.PuzzleRush
import com.paulcraciunas.chessgym.screens.PuzzleStreak
import com.paulcraciunas.chessgym.screens.RatedPuzzle
import com.paulcraciunas.chessgym.screens.Settings
import com.paulcraciunas.chessgym.screens.SignIn
import com.paulcraciunas.chessgym.screens.ToolsDashboard
import com.paulcraciunas.screens.about.vm.AboutSection

internal fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    onDrawerToggle: () -> Unit,
    debugMenuProvider: DebugMenuProvider,
) {
    animatedComposable<Screen.Home> { Home(tabNavController = navController, onDrawerToggle = onDrawerToggle) }
    animatedComposable<Screen.Achievements> { Achievements(tabNavController = navController) }
    animatedComposable<Screen.PuzzleDashboard> { PuzzleDashboard(tabNavController = navController, onDrawerToggle = onDrawerToggle) }
    animatedComposable<Screen.RatedPuzzle> { RatedPuzzle(tabNavController = navController) }
    animatedComposable<Screen.PuzzleRush> { PuzzleRush(tabNavController = navController) }
    animatedComposable<Screen.FailedPuzzles> { FailedPuzzles(tabNavController = navController) }
    animatedComposable<Screen.PuzzleStreak> { PuzzleStreak(tabNavController = navController) }
    animatedComposable<Screen.BoardVisualization> { BoardVisDashboard(tabNavController = navController, onDrawerToggle = onDrawerToggle) }
    animatedComposable<Screen.FindTheSquare> { FindTheSquare(tabNavController = navController) }
    animatedComposable<Screen.KnightPath> { KnightPath(tabNavController = navController) }
    animatedComposable<Screen.BlindMode> { BlindMode(onDrawerToggle = onDrawerToggle) }
    animatedComposable<Screen.ToolsDashboard> { ToolsDashboard(tabNavController = navController, onDrawerToggle = onDrawerToggle) }
    animatedComposable<Screen.Clock> { ChessClock(tabNavController = navController) }
    animatedComposable<Screen.Analysis> { AnalysisBoard(tabNavController = navController, puzzleId = it.toRoute<Screen.Analysis>().puzzleId) }
    animatedComposable<Screen.ImportGame> { ImportGame(tabNavController = navController) }
    animatedComposable<Screen.Settings> { Settings(onNavigateBack = navController::popBackStack) }
    animatedComposable<Screen.SignUp> { SignIn(tabNavController = navController) }
    animatedComposable<Screen.About> { About(tabNavController = navController) }
    animatedComposable<Screen.AboutDetail> {
        val section = AboutSection.valueOf(it.toRoute<Screen.AboutDetail>().section)
        AboutDetail(section = section, onNavigateBack = navController::popBackStack)
    }
    with(debugMenuProvider) {
        registerDebugScreens(navController = navController)
    }
}

internal inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    noinline content: @Composable (NavBackStackEntry) -> Unit = {},
) {
    composable<T>(
        enterTransition = { enter() },
        exitTransition = { exit() }
    ) { backStackEntry ->
        content(backStackEntry)
    }
}
