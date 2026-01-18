package com.paulcraciunas.chessgym

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val mainScreenState by mainScreenViewModel.uiState.collectAsState()

    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelScreen = currentDestination?.route?.isTopLevelRoute() ?: true

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
        drawerState = drawerState,
        gesturesEnabled = isTopLevelScreen
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                if (isTopLevelScreen) {
                    AppBar(titleAlign = AppBarAlignment.Center) {
                        Home(onClick = { scope.launch { drawerState.toggle() } })
                    }
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
                            // Avoid multiple copies of the same destination when re-selecting the same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            },
        ) { innerPadding ->
            val contentPadding = if (isTopLevelScreen) {
                innerPadding
            } else {
                PaddingValues(bottom = innerPadding.calculateBottomPadding())
            }
            NavHost(
                navController = tabNavController,
                startDestination = Screen.Home,
                modifier = Modifier.padding(contentPadding)
            ) {
                animatedComposable<Screen.Home> {
                    val vm: HomeViewModel = hiltViewModel()
                    val homeState by vm.uiState.collectAsState()
                    HomeScreen(state = homeState)
                }
                animatedComposable<Screen.PuzzleDashboard> {
                    PuzzleDashboard(tabNavController)
                }
                animatedComposable<Screen.RatedPuzzle> {
                    RatedPuzzle(
                        tabNavController = tabNavController,
                        showBorders = mainScreenState.appSettings?.showBorders ?: true,
                    )
                }
                animatedComposable<Screen.BoardVisualization> {
                    UnderConstruction(title = "Board Visualisation", innerPadding = innerPadding)
                }
                animatedComposable<Screen.BlindMode> {
                    UnderConstruction(title = "Blind Mode", innerPadding = innerPadding)
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
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
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