package com.paulcraciunas.chessgym

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.paulcraciunas.chessgym.animations.enter
import com.paulcraciunas.chessgym.animations.exit
import com.paulcraciunas.chessgym.debug.DebugMenuProvider
import com.paulcraciunas.chessgym.error_reporting.NavigationLogger
import com.paulcraciunas.chessgym.navigation.BottomNavigationBar
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.chessgym.navigation.navigateToTopLevel
import com.paulcraciunas.chessgym.screens.AnalysisBoard
import com.paulcraciunas.chessgym.screens.BlindMode
import com.paulcraciunas.chessgym.screens.BoardVisDashboard
import com.paulcraciunas.chessgym.screens.ChessClock
import com.paulcraciunas.chessgym.screens.FailedPuzzles
import com.paulcraciunas.chessgym.screens.FindTheSquare
import com.paulcraciunas.chessgym.screens.ImportGame
import com.paulcraciunas.chessgym.screens.MoveThePiece
import com.paulcraciunas.chessgym.screens.PuzzleDashboard
import com.paulcraciunas.chessgym.screens.PuzzleRush
import com.paulcraciunas.chessgym.screens.PuzzleStreak
import com.paulcraciunas.chessgym.screens.RatedPuzzle
import com.paulcraciunas.chessgym.screens.ToolsDashboard
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.screens.achievements.ui.AchievementBannerHost
import com.paulcraciunas.screens.achievements.ui.AchievementsScreen
import com.paulcraciunas.screens.achievements.vm.AchievementsViewModel
import com.paulcraciunas.screens.common.AppDrawer
import com.paulcraciunas.screens.common.LoadingContent
import com.paulcraciunas.screens.common.dialogs.DeleteAccountConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.SignOutConfirmationDialog
import com.paulcraciunas.screens.home.ui.HomeScreen
import com.paulcraciunas.screens.home.vm.HomeViewModel
import kotlinx.coroutines.launch
import com.paulcraciunas.global.resources.R as GlobalR

@Composable
fun MainScreen(
    onDrawerScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    notificationManager: AchievementNotificationManager? = null,
) {
    val mainScreenViewModel: MainScreenViewModel = hiltViewModel()
    val mainScreenState by mainScreenViewModel.uiState.collectAsStateWithLifecycle()
    val debugMenuProvider: DebugMenuProvider = mainScreenViewModel.debugMenuProvider

    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelScreen by remember(currentDestination) {
        derivedStateOf { currentDestination?.isTopLevelRoute() ?: true }
    }

    DisposableEffect(tabNavController) {
        tabNavController.addOnDestinationChangedListener(NavigationLogger)
        onDispose { tabNavController.removeOnDestinationChangedListener(NavigationLogger) }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val onDrawerToggle: () -> Unit = { scope.launch { drawerState.toggle() } }
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    val snackbarHostState = remember { SnackbarHostState() }

    val deleteErrorNoNetwork = stringResource(GlobalR.string.delete_account_error_no_network)
    val deleteErrorFailed = stringResource(GlobalR.string.delete_account_error_failed)
    val signOutError = stringResource(GlobalR.string.sign_out_error_failed)

    LaunchedEffect(Unit) {
        mainScreenViewModel.accountEvent.collect { event ->
            when (event) {
                is AccountEvent.DeleteAccountFailed -> snackbarHostState.showSnackbar(
                    when (event.reason) {
                        AccountEvent.DeleteAccountFailReason.NO_NETWORK -> deleteErrorNoNetwork
                        AccountEvent.DeleteAccountFailReason.UNKNOWN -> deleteErrorFailed
                    }
                )
                is AccountEvent.SignOutFailed -> snackbarHostState.showSnackbar(signOutError)
                else -> {}
            }
        }
    }

    when (mainScreenState.activeDialog) {
        MainScreenDialog.SignOutConfirmation -> {
            SignOutConfirmationDialog(
                onConfirm = {
                    mainScreenViewModel.dismissDialog()
                    mainScreenViewModel.signOut()
                },
                onDismiss = { mainScreenViewModel.dismissDialog() },
            )
        }
        MainScreenDialog.DeleteAccountConfirmation -> {
            DeleteAccountConfirmationDialog(
                onConfirm = {
                    mainScreenViewModel.dismissDialog()
                    mainScreenViewModel.deleteAccount()
                },
                onDismiss = { mainScreenViewModel.dismissDialog() },
            )
        }
        null -> {}
    }

    CompositionLocalProvider(LocalAppSettings provides mainScreenState.appSettings) {
        if (mainScreenState.isLoading) {
            LoadingContent()
        } else {
            Box {
                ModalNavigationDrawer(
                    drawerContent = {
                        AppDrawer(
                            drawerState = drawerState,
                            isSignedIn = mainScreenState.isSignedIn,
                            onSignIn = { onDrawerScreen(Screen.SignUp) },
                            onSignOut = { mainScreenViewModel.showSignOutDialog() },
                            onHome = {
                                scope.launch {
                                    tabNavController.navigate(Screen.Home) {
                                        popUpTo(Screen.Home) { inclusive = true }
                                    }
                                }
                            },
                            onSettings = { onDrawerScreen(Screen.Settings) },
                            onAbout = { onDrawerScreen(Screen.About) },
                            onDeleteAccount = { mainScreenViewModel.showDeleteAccountDialog() },
                            closeDrawer = { closeDrawer() },
                            trailingContent = {
                                with(debugMenuProvider) {
                                    DrawerContent(
                                        closeDrawer = { closeDrawer() },
                                        onNavigate = { screen -> tabNavController.navigate(screen) },
                                    )
                                }
                            },
                        )
                    },
                    drawerState = drawerState,
                    gesturesEnabled = isTopLevelScreen,
                ) {
                    Scaffold(
                        modifier = modifier,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (isTopLevelScreen) {
                                BottomNavigationBar(
                                    currentDestination = currentDestination,
                                    onItemSelected = { item -> tabNavController.navigateToTopLevel(item.screen) }
                                )
                            }
                        },
                    ) { innerPadding ->
                        NavHost(
                            navController = tabNavController,
                            startDestination = Screen.Home,
                            modifier = if (isTopLevelScreen) {
                                Modifier
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                                    .consumeWindowInsets(WindowInsets.navigationBars)
                            } else {
                                Modifier
                            }
                        ) {
                            animatedComposable<Screen.Home> {
                                val vm: HomeViewModel = hiltViewModel()
                                val homeState by vm.uiState.collectAsStateWithLifecycle()
                                HomeScreen(
                                    state = homeState,
                                    onDrawerToggle = onDrawerToggle,
                                    onAchievements = {
                                        tabNavController.navigate(Screen.Achievements)
                                    },
                                )
                            }
                            animatedComposable<Screen.Achievements> {
                                val vm: AchievementsViewModel = hiltViewModel()
                                val achievementsState by vm.uiState.collectAsStateWithLifecycle()
                                AchievementsScreen(
                                    state = achievementsState,
                                    interactions = vm,
                                    onBack = { tabNavController.popBackStack() },
                                )
                            }
                            animatedComposable<Screen.PuzzleDashboard> {
                                PuzzleDashboard(tabNavController, onDrawerToggle = onDrawerToggle)
                            }
                            animatedComposable<Screen.RatedPuzzle> {
                                RatedPuzzle(tabNavController = tabNavController)
                            }
                            animatedComposable<Screen.PuzzleRush> {
                                PuzzleRush(tabNavController = tabNavController)
                            }
                            animatedComposable<Screen.FailedPuzzles> {
                                FailedPuzzles(tabNavController = tabNavController)
                            }
                            animatedComposable<Screen.PuzzleStreak> {
                                PuzzleStreak(tabNavController = tabNavController)
                            }
                            animatedComposable<Screen.BoardVisualization> {
                                BoardVisDashboard(tabNavController, onDrawerToggle = onDrawerToggle)
                            }
                            animatedComposable<Screen.FindTheSquare> {
                                FindTheSquare(tabNavController = tabNavController)
                            }
                            animatedComposable<Screen.MoveThePiece> {
                                MoveThePiece(tabNavController = tabNavController)
                            }
                            animatedComposable<Screen.BlindMode> {
                                BlindMode(onDrawerToggle = onDrawerToggle)
                            }
                            animatedComposable<Screen.ToolsDashboard> {
                                ToolsDashboard(tabNavController, onDrawerToggle = onDrawerToggle)
                            }
                            animatedComposable<Screen.Clock> {
                                ChessClock(tabNavController = tabNavController)
                            }
                            composable<Screen.Analysis>(
                                enterTransition = { enter() },
                                exitTransition = { exit() },
                            ) { backStackEntry ->
                                val route = backStackEntry.toRoute<Screen.Analysis>()
                                AnalysisBoard(
                                    tabNavController = tabNavController,
                                    fen = route.fen,
                                )
                            }
                            animatedComposable<Screen.ImportGame> {
                                ImportGame(tabNavController = tabNavController)
                            }
                            with(debugMenuProvider) {
                                registerDebugScreens(navController = tabNavController)
                            }
                        }
                    }
                }

                notificationManager?.let { manager ->
                    AchievementBannerHost(
                        notificationManager = manager,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding(),
                    )
                }
            }
        }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.animatedComposable(noinline content: (@Composable () -> Unit) = {}) {
    composable<T>(
        enterTransition = { enter() },
        exitTransition = { exit() }
    ) {
        content()
    }
}

private fun NavDestination.isTopLevelRoute(): Boolean = hasRoute<Screen.Home>()
    || hasRoute<Screen.PuzzleDashboard>()
    || hasRoute<Screen.BoardVisualization>()
    || hasRoute<Screen.BlindMode>()
    || hasRoute<Screen.ToolsDashboard>()

private suspend fun DrawerState.toggle() {
    if (isClosed) {
        open()
    } else {
        close()
    }
}

