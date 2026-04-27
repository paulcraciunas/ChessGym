package com.paulcraciunas.chessgym

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paulcraciunas.chessgym.animations.enter
import com.paulcraciunas.chessgym.animations.exit
import com.paulcraciunas.chessgym.navigation.BottomNavigationBar
import com.paulcraciunas.chessgym.navigation.Screen
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
import com.paulcraciunas.screens.common.AppDrawer
import com.paulcraciunas.screens.home.ui.HomeScreen
import com.paulcraciunas.screens.home.vm.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onDrawerScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainScreenViewModel: MainScreenViewModel = hiltViewModel()
    val mainScreenState by mainScreenViewModel.uiState.collectAsStateWithLifecycle()

    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelScreen = currentDestination?.route?.isTopLevelRoute() ?: true

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val onDrawerToggle: () -> Unit = { scope.launch { drawerState.toggle() } }

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
        drawerState = drawerState,
        gesturesEnabled = isTopLevelScreen
    ) {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (isTopLevelScreen) {
                    BottomNavigationBar(
                        currentDestination = currentDestination,
                        onItemSelected = { item ->
                            tabNavController.navigate(item.screen) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(tabNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when re-selecting the same item
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = tabNavController,
                startDestination = Screen.Home,
                modifier = if (isTopLevelScreen) {
                    Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                } else {
                    Modifier
                }
            ) {
                animatedComposable<Screen.Home> {
                    val vm: HomeViewModel = hiltViewModel()
                    val homeState by vm.uiState.collectAsStateWithLifecycle()
                    HomeScreen(state = homeState, onDrawerToggle = onDrawerToggle)
                }
                animatedComposable<Screen.PuzzleDashboard> {
                    PuzzleDashboard(tabNavController, onDrawerToggle = onDrawerToggle)
                }
                animatedComposable<Screen.RatedPuzzle> {
                    RatedPuzzle(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        highlightLegalMoves = mainScreenState.appSettings?.highlightLegalMoves ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                    )
                }
                animatedComposable<Screen.PuzzleRush> {
                    PuzzleRush(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        highlightLegalMoves = mainScreenState.appSettings?.highlightLegalMoves ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                    )
                }
                animatedComposable<Screen.FailedPuzzles> {
                    FailedPuzzles(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        highlightLegalMoves = mainScreenState.appSettings?.highlightLegalMoves ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                    )
                }
                animatedComposable<Screen.PuzzleStreak> {
                    PuzzleStreak(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        highlightLegalMoves = mainScreenState.appSettings?.highlightLegalMoves ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                    )
                }
                animatedComposable<Screen.BoardVisualization> {
                    BoardVisDashboard(tabNavController, onDrawerToggle = onDrawerToggle)
                }
                animatedComposable<Screen.FindTheSquare> {
                    FindTheSquare(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        enableVibrations = mainScreenState.appSettings?.enableVibrations ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                    )
                }
                animatedComposable<Screen.MoveThePiece> {
                    MoveThePiece(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        highlightLegalMoves = mainScreenState.appSettings?.highlightLegalMoves ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                    )
                }
                animatedComposable<Screen.BlindMode> {
                    BlindMode(
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        highlightLegalMoves = mainScreenState.appSettings?.highlightLegalMoves ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                        onDrawerToggle = onDrawerToggle,
                    )
                }
                animatedComposable<Screen.ToolsDashboard> {
                    ToolsDashboard(tabNavController, onDrawerToggle = onDrawerToggle)
                }
                animatedComposable<Screen.Clock> {
                    ChessClock(tabNavController = tabNavController)
                }
                animatedComposable<Screen.ImportGame> {
                    ImportGame(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                        highlightLegalMoves = mainScreenState.appSettings?.highlightLegalMoves ?: true,
                        enableAnimations = mainScreenState.appSettings?.enableAnimations ?: true,
                    )
                }
            }
        }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    noinline content: (@Composable () -> Unit) = {},
) {
    composable<T>(
        enterTransition = { enter() },
        exitTransition = { exit() }
    ) {
        content()
    }
}

private val topLevelRoutes = setOf(
    Screen.Home::class.qualifiedName,
    Screen.PuzzleDashboard::class.qualifiedName,
    Screen.BoardVisualization::class.qualifiedName,
    Screen.BlindMode::class.qualifiedName,
    Screen.ToolsDashboard::class.qualifiedName
)

private fun String.isTopLevelRoute(): Boolean = topLevelRoutes.contains(this)

private suspend fun DrawerState.toggle() {
    if (isClosed) {
        open()
    } else {
        close()
    }
}