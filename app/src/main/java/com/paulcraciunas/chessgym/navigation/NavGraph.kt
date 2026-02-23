package com.paulcraciunas.chessgym.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paulcraciunas.chessgym.MainScreen
import com.paulcraciunas.chessgym.UnderConstruction
import com.paulcraciunas.chessgym.screens.Settings
import com.paulcraciunas.screens.loading.ui.LoadingScreen
import com.paulcraciunas.screens.loading.vm.LoadingViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    viewModel: NavGraphViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Splash screen handles the loading state, so we wait until it's ready
    if (uiState.isLoading) {
        // Return early - splash screen is still showing
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
            val vm: LoadingViewModel = hiltViewModel()
            val loadingState by vm.uiState.collectAsStateWithLifecycle()
            LoadingScreen(
                onComplete = { navController.navigateTo(Screen.Main) },
                onDownload = vm::onDownload,
                onDownloadConfirmation = vm::onDownloadConfirmation,
                onPermissionReceived = vm::onPermissionReceived,
                uiState = loadingState
            )
        }
        composable<Screen.Main> {
            MainScreen(
                onDrawerScreen = { screen -> navController.navigate(screen) },
            )
        }
        composable<Screen.Settings> {
            Settings(onNavigateBack = navController::popBackStack)
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
