package com.paulcraciunas.chessgym

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paulcraciunas.chessgym.navigation.BottomNavigationBar
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.chessgym.ui.screens.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onDrawerScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                onSignIn = { onDrawerScreen(Screen.SignUp) },
                onHome = {
                    scope.launch {
                        tabNavController.navigate(Screen.Home) { // Navigate to Home tab in bottom navigation
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                },
                onSettings = { onDrawerScreen(Screen.Settings) },
                onAbout = { onDrawerScreen(Screen.About) },
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                AppBar(
                    onClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    }
                ) {
                    Home()
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    currentDestination = currentDestination,
                    onItemSelected = { item ->
                        tabNavController.navigate(item.screen) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(tabNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = tabNavController,
                startDestination = Screen.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<Screen.Home>(
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() }
                ) {
                    HomeScreen()
                }
                composable<Screen.PuzzleDashboard>(
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() }
                ) {
                    UnderConstruction(title = "Puzzle Dashboard", innerPadding = innerPadding)
                }
                composable<Screen.BoardVisualization>(
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() }
                ) {
                    UnderConstruction(title = "Board Visualisation", innerPadding = innerPadding)
                }
                composable<Screen.BlindMode>(
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() }
                ) {
                    UnderConstruction(title = "Blind Mode", innerPadding = innerPadding)
                }
            }
        }
    }
}

@Composable
private fun UnderConstruction(
    title: String,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(innerPadding)) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Under Construction",
            color = Color.Yellow,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() = slideOutHorizontally(
    animationSpec = tween(300),
    targetOffsetX = { if (isNavigatingToHigherIndex(targetState, initialState)) -it else it }
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() = slideInHorizontally(
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
