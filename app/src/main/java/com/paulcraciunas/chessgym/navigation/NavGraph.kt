package com.paulcraciunas.chessgym.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paulcraciunas.chessgym.di.NavigationEntryPoint
import com.paulcraciunas.chessgym.ui.screens.home.HomeScreen
import com.paulcraciunas.chessgym.ui.screens.loading.LoadingScreen
import com.paulcraciunas.chessgym.ui.screens.puzzle.RatedPuzzleScreen
import dagger.hilt.android.EntryPointAccessors

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Create a custom Hilt entry point to access the NavigationModule
    val hiltEntryPoint = remember {
        EntryPointAccessors.fromApplication(
            navController.context.applicationContext,
            NavigationEntryPoint::class.java
        )
    }

    // Get the Navigator instance
    val navigator = remember {
        hiltEntryPoint.navigator()
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Loading.route
    ) {
        composable(Screen.Loading.route) {
            LoadingScreen()
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onRatedPuzzleClick = { navigator.navigateTo(Screen.RatedPuzzle) },
                onPuzzleRushClick = { navigator.navigateTo(Screen.PuzzleRush) },
                onBoardVisualizationClick = { navigator.navigateTo(Screen.BoardVisualization) },
                onBlindModeClick = { navigator.navigateTo(Screen.BlindMode) }
            )
        }
        composable(Screen.RatedPuzzle.route) {
            RatedPuzzleScreen(
                onBackClick = { navigator.navigateBack() }
            )
        }
    }
}
