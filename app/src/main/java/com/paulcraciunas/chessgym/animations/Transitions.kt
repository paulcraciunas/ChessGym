package com.paulcraciunas.chessgym.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

fun AnimatedContentTransitionScope<NavBackStackEntry>.exit() = slideOutHorizontally(
    animationSpec = tween(300),
    targetOffsetX = { if (isNavigatingToHigherIndex(targetState, initialState)) -it else it }
)

fun AnimatedContentTransitionScope<NavBackStackEntry>.enter() = slideInHorizontally(
    animationSpec = tween(300),
    initialOffsetX = { if (isNavigatingToHigherIndex(targetState, initialState)) it else -it }
)

private fun isNavigatingToHigherIndex(targetState: NavBackStackEntry, initialState: NavBackStackEntry): Boolean {
    val targetIndex = getTabIndexFromRoute(targetState.destination.route)
    val initialIndex = getTabIndexFromRoute(initialState.destination.route)
    return targetIndex > initialIndex
}

private fun getTabIndexFromRoute(route: String?): Int = when {
    route?.contains("Home") == true -> 0
    route?.contains("PuzzleDashboard") == true -> 1
    route?.contains("BoardVisualization") == true -> 2
    route?.contains("BlindMode") == true -> 3
    else -> -1
}
