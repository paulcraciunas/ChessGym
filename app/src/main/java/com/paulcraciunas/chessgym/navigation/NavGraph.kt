package com.paulcraciunas.chessgym.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paulcraciunas.chessgym.MainScreen
import com.paulcraciunas.chessgym.UnderConstruction
import com.paulcraciunas.chessgym.ui.screens.loading.LoadingScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    viewModel: NavGraphViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    // Show loading indicator while checking app settings
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (uiState.puzzlesDownloaded) {
        Screen.Main
    } else {
        Screen.Loading
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<Screen.Loading> {
            LoadingScreen(onComplete = { navController.navigateTo(Screen.Main) })
        }
        composable<Screen.Main> {
            MainScreen(
                onDrawerScreen = { screen -> navController.navigate(screen) },
            )
        }
        composable<Screen.Settings> {
            UnderConstruction(navController)
        }
        composable<Screen.SignUp> {
            UnderConstruction(navController)
        }
        composable<Screen.About> {
            UnderConstruction(navController)
        }
    }
}

fun NavHostController.navigateTo(screen: Screen) {
    navigate(screen) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}
