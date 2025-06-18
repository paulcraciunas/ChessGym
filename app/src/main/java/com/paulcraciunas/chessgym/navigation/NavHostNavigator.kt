package com.paulcraciunas.chessgym.navigation

import androidx.navigation.NavHostController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NavHostNavigator @Inject constructor(
    private val navController: NavHostController
) : Navigator {
    override fun navigateTo(screen: Screen) {
        navController.navigate(screen.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}
