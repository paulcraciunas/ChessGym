package com.paulcraciunas.chessgym.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paulcraciunas.chessgym.error_reporting.NavigationLogger
import com.paulcraciunas.chessgym.navigation.BottomNavItem
import com.paulcraciunas.chessgym.navigation.BottomNavItemState
import com.paulcraciunas.chessgym.navigation.BottomNavigationBar
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.chessgym.navigation.mainNavGraph
import com.paulcraciunas.chessgym.navigation.navigateToTopLevel
import com.paulcraciunas.chessgym.sounds.GameSoundEffects
import com.paulcraciunas.global.navigation.NavigationDispatcher
import com.paulcraciunas.screens.achievements.ui.AchievementBannerHost
import com.paulcraciunas.screens.common.dialogs.DeleteAccountConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.SignOutConfirmationDialog
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.paulcraciunas.global.resources.R as GlobalR

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val vm: MainScreenViewModel = hiltViewModel()
    val mainScreenState by vm.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, MainScreenEntryPoint::class.java)
    }
    val debugMenuProvider = remember { entryPoint.debugMenuProvider() }
    val navigationDispatcher = remember { entryPoint.navigationDispatcher() }
    val soundManager = remember { entryPoint.soundManager() }
    val soundCoordinator = remember { entryPoint.soundCoordinator() }
    val notificationManager = remember { entryPoint.achievementNotificationManager() }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomBarItems = remember(currentDestination) {
        BottomNavItem.entries.map { navItem ->
            BottomNavItemState(
                item = navItem,
                isSelected = currentDestination?.hasRoute(navItem.screen::class) == true
            )
        }
    }
    val isTopLevelScreen by remember(currentDestination) {
        derivedStateOf { currentDestination?.isTopLevelRoute() ?: true }
    }

    DisposableEffect(navController) {
        navController.addOnDestinationChangedListener(NavigationLogger)
        onDispose { navController.removeOnDestinationChangedListener(NavigationLogger) }
    }

    LaunchedEffect(Unit) {
        navigationDispatcher.navigationEvents.collect { event ->
            when (event) {
                is NavigationDispatcher.Destination.Analysis ->
                    navController.navigate(Screen.Analysis(puzzleId = event.puzzleId))
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val onDrawerToggle: () -> Unit = { scope.launch { drawerState.toggle() } }
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    val snackbarHostState = remember { SnackbarHostState() }

    HandleAccountEvents(events = { vm.accountEvent }, snackbarHostState = snackbarHostState)
    HandleDialogs(
        activeDialog = mainScreenState.activeDialog,
        dismissDialog = vm::dismissDialog,
        signOut = vm::signOut,
        deleteAccount = vm::deleteAccount,
    )

    GameSoundEffects(soundManager = soundManager, soundEvents = soundCoordinator)

    Box {
        MainDrawer(
            drawerState = drawerState,
            gesturesEnabled = isTopLevelScreen,
            isSignedIn = mainScreenState.isSignedIn,
            onSignIn = { closeDrawer(); navController.navigate(Screen.SignUp) },
            onSignOut = vm::showSignOutDialog,
            onHome = {
                scope.launch {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                }
            },
            onSettings = { closeDrawer(); navController.navigate(Screen.Settings) },
            onAbout = { closeDrawer(); navController.navigate(Screen.About) },
            onDeleteAccount = vm::showDeleteAccountDialog,
            closeDrawer = closeDrawer,
            trailingContent = {
                with(debugMenuProvider) {
                    DrawerContent(closeDrawer = closeDrawer, onNavigate = navController::navigate)
                }
            },
        ) {
            Scaffold(
                modifier = modifier,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    AnimatedVisibility(
                        visible = isTopLevelScreen,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it },
                    ) {
                        BottomNavigationBar(
                            items = bottomBarItems,
                            onItemSelected = { item -> navController.navigateToTopLevel(item.screen) }
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home,
                    modifier = if (isTopLevelScreen) {
                        Modifier
                            .padding(bottom = innerPadding.calculateBottomPadding())
                            .consumeWindowInsets(WindowInsets.navigationBars)
                    } else {
                        Modifier
                    }
                ) {
                    mainNavGraph(
                        navController = navController,
                        onDrawerToggle = onDrawerToggle,
                        debugMenuProvider = debugMenuProvider,
                    )
                }
            }
        }

        AchievementBannerHost(
            notifications = { notificationManager.notifications },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        )
    }
}

@Composable
private fun HandleDialogs(
    activeDialog: MainScreenDialog?,
    dismissDialog: () -> Unit,
    signOut: () -> Unit,
    deleteAccount: () -> Unit,
) {
    when (activeDialog) {
        MainScreenDialog.SignOutConfirmation -> SignOutConfirmationDialog(
            onConfirm = { dismissDialog(); signOut() },
            onDismiss = dismissDialog,
        )
        MainScreenDialog.DeleteAccountConfirmation -> DeleteAccountConfirmationDialog(
            onConfirm = { dismissDialog(); deleteAccount() },
            onDismiss = dismissDialog,
        )
        null -> {}
    }
}

@Composable
private fun HandleAccountEvents(
    events: () -> Flow<AccountEvent>,
    snackbarHostState: SnackbarHostState,
) {
    val deleteErrorNoNetwork = stringResource(GlobalR.string.delete_account_error_no_network)
    val deleteErrorFailed = stringResource(GlobalR.string.delete_account_error_failed)
    val signOutError = stringResource(GlobalR.string.sign_out_error_failed)

    LaunchedEffect(events) {
        events().collect { event ->
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
}

private fun NavDestination.isTopLevelRoute(): Boolean =
    BottomNavItem.entries.any { hasRoute(it.screen::class) }

private suspend fun DrawerState.toggle() {
    if (isClosed) open() else close()
}
