package com.paulcraciunas.chessgym.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paulcraciunas.chessgym.MainScreen
import androidx.navigation.toRoute
import com.paulcraciunas.chessgym.error_reporting.NavigationLogger
import com.paulcraciunas.chessgym.screens.About
import com.paulcraciunas.chessgym.screens.AboutDetail
import com.paulcraciunas.chessgym.screens.Settings
import com.paulcraciunas.chessgym.auth.GoogleTokenSource
import com.paulcraciunas.screens.signin.ui.SignInScreen
import com.paulcraciunas.screens.signin.vm.SignInViewModel
import com.paulcraciunas.screens.about.vm.AboutSection
import com.paulcraciunas.screens.loading.ui.LoadingScreen
import com.paulcraciunas.screens.loading.vm.LoadingViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    viewModel: NavGraphViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(navController) {
        navController.addOnDestinationChangedListener(NavigationLogger)
        onDispose { navController.removeOnDestinationChangedListener(NavigationLogger) }
    }

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
                onCrashConsentResponse = vm::onCrashConsentResponse,
                uiState = loadingState
            )
        }
        composable<Screen.Main> {
            MainScreen(
                onDrawerScreen = { screen -> navController.navigate(screen) },
                notificationManager = viewModel.achievementNotificationManager,
            )
        }
        composable<Screen.Settings> {
            Settings(onNavigateBack = navController::popBackStack)
        }
        composable<Screen.SignUp> {
            val vm: SignInViewModel = hiltViewModel()
            val signInState by vm.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val webClientId = remember {
                context.getString(com.paulcraciunas.chessgym.R.string.default_web_client_id)
            }
            SignInScreen(
                uiState = signInState,
                onGoogleSignIn = { vm.onGoogleSignIn(GoogleTokenSource(webClientId, context)) },
                onEmailSignIn = vm::onEmailSignIn,
                onEmailSignUp = vm::onEmailSignUp,
                onNavigateBack = navController::popBackStack,
                onClearError = vm::clearError,
            )
        }
        composable<Screen.About> {
            About(
                onNavigateBack = navController::popBackStack,
                onSectionClicked = { section ->
                    navController.navigate(Screen.AboutDetail(section = section.name))
                },
            )
        }
        composable<Screen.AboutDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AboutDetail>()
            val section = AboutSection.valueOf(route.section)
            AboutDetail(
                section = section,
                onNavigateBack = navController::popBackStack,
            )
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
