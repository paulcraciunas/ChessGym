package com.paulcraciunas.chessgym.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.paulcraciunas.chessgym.navigation.Screen

fun AnimatedContentTransitionScope<NavBackStackEntry>.exit() = slideOutHorizontally(
    animationSpec = tween(300),
    targetOffsetX = { if (isNavigatingToHigherIndex(targetState, initialState)) -it else it }
)

fun AnimatedContentTransitionScope<NavBackStackEntry>.enter() = slideInHorizontally(
    animationSpec = tween(300),
    initialOffsetX = { if (isNavigatingToHigherIndex(targetState, initialState)) it else -it }
)

private fun isNavigatingToHigherIndex(targetState: NavBackStackEntry, initialState: NavBackStackEntry): Boolean {
    val targetIndex = targetState.destination.getTabIndex()
    val initialIndex = initialState.destination.getTabIndex()
    return targetIndex > initialIndex
}

private fun NavDestination.getTabIndex(): Int = when {
    hasRoute<Screen.Home>() -> 0
    hasRoute<Screen.PuzzleDashboard>() -> 1
    hasRoute<Screen.BoardVisualization>() -> 2
    hasRoute<Screen.BlindMode>() -> 3
    hasRoute<Screen.ToolsDashboard>() -> 4
    hasRoute<Screen.Settings>() -> 5
    hasRoute<Screen.About>() -> 6
    hasRoute<Screen.AboutDetail>() -> 7
    hasRoute<Screen.SignUp>() -> 8
    else -> -1
}
