package com.paulcraciunas.chessgym.navigation

import androidx.navigation.NavHostController
import com.paulcraciunas.screens.boardvis.dashboard.vm.BoardVisMode
import com.paulcraciunas.screens.puzzles.dashboard.vm.PuzzleMode
import com.paulcraciunas.screens.tools.dashboard.vm.ToolsMode

internal fun NavHostController.navigateToTopLevel(screen: Screen) {
    navigate(screen) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        popUpTo(graph.startDestinationId) { saveState = true }
        // Avoid multiple copies of the same destination when re-selecting the same item
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item
        restoreState = true
    }
}

internal inline fun <reified T> NavHostController.navigateToDashChild(child: T) {
    navigate(
        when (child) {
            is PuzzleMode.RatedPuzzle -> Screen.RatedPuzzle
            is PuzzleMode.PuzzleRush -> Screen.PuzzleRush
            is PuzzleMode.FailedPuzzles -> Screen.FailedPuzzles
            is PuzzleMode.PuzzleStreak -> Screen.PuzzleStreak
            is BoardVisMode.FindTheSquare -> Screen.FindTheSquare
            is BoardVisMode.KnightPath -> Screen.KnightPath
            is ToolsMode.Clock -> Screen.Clock
            is ToolsMode.Analysis -> Screen.Analysis()
            is ToolsMode.ImportGame -> Screen.ImportGame
            else -> Screen.Home
        }
    )
}
