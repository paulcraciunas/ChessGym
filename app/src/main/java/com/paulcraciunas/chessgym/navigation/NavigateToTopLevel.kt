package com.paulcraciunas.chessgym.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToTopLevel(screen: Screen) {
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
