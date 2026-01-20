package com.paulcraciunas.chessgym

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.paulcraciunas.chessgym.screens.PuzzleDashboard
import com.paulcraciunas.chessgym.screens.PuzzleRush
import com.paulcraciunas.chessgym.screens.RatedPuzzle
import com.paulcraciunas.screens.common.AppBar
import com.paulcraciunas.screens.common.AppBarAlignment
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
                    )
                }
                animatedComposable<Screen.PuzzleRush> {
                    PuzzleRush(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                    )
                }
                animatedComposable<Screen.BoardVisualization> {
                    UnderConstruction(title = "Board Visualisation", onDrawerToggle = onDrawerToggle)
                }
                animatedComposable<Screen.BlindMode> {
                    UnderConstruction(title = "Blind Mode", onDrawerToggle = onDrawerToggle)
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

@Composable
private fun UnderConstruction(
    title: String,
    onDrawerToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppBar(titleAlign = AppBarAlignment.Center) {
                Home(onClick = onDrawerToggle)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
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
}

private val topLevelRoutes = setOf(
    Screen.Home::class.qualifiedName,
    Screen.PuzzleDashboard::class.qualifiedName,
    Screen.BoardVisualization::class.qualifiedName,
    Screen.BlindMode::class.qualifiedName
)

private fun String.isTopLevelRoute(): Boolean = topLevelRoutes.contains(this)

private suspend fun DrawerState.toggle() {
    if (isClosed) {
        open()
    } else {
        close()
    }
}