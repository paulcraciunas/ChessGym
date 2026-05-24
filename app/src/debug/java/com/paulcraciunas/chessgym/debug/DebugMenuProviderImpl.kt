package com.paulcraciunas.chessgym.debug

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.paulcraciunas.chessgym.animations.enter
import com.paulcraciunas.chessgym.animations.exit
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.domain.api.achievements.AchievementNotification
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.LocalAppSettings
import kotlinx.coroutines.launch
import javax.inject.Inject

class DebugMenuProviderImpl @Inject constructor(
    private val notificationManager: AchievementNotificationManager,
) : DebugMenuProvider {

    @Composable
    override fun ColumnScope.DrawerContent(
        closeDrawer: () -> Unit,
        onNavigate: (Any) -> Unit,
    ) {
        val scope = rememberCoroutineScope()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Debug",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        NavigationDrawerItem(
            label = { Text(text = "Load Puzzle") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.puzzle_icon),
                    contentDescription = null,
                    modifier = Modifier.height(24.dp)
                )
            },
            selected = false,
            onClick = {
                onNavigate(DebugScreen.LoadPuzzle)
                closeDrawer()
            },
        )
        NavigationDrawerItem(
            label = { Text(text = "Force Achievement") },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                )
            },
            selected = false,
            onClick = {
                onNavigate(DebugScreen.ForceAchievement)
                closeDrawer()
            },
        )
        NavigationDrawerItem(
            label = { Text(text = "Show Achievement Banner") },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                )
            },
            selected = false,
            onClick = {
                scope.launch {
                    val achievement = Achievement.entries.random()
                    val tier = Achievement.Tier.entries.random()
                    notificationManager.emit(AchievementNotification(achievement, tier))
                }
                closeDrawer()
            },
        )
    }

    override fun NavGraphBuilder.registerDebugScreens(navController: NavHostController) {
        composable<DebugScreen.LoadPuzzle>(
            enterTransition = { enter() },
            exitTransition = { exit() },
        ) {
            val settings = LocalAppSettings.current
            val vm: DebugPuzzleViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            DebugPuzzleScreen(
                uiState = uiState,
                showBorders = settings.showBorders,
                highlightLegalMoves = settings.highlightLegalMoves,
                enableAnimations = settings.enableAnimations,
                onNavigateBack = { navController.popBackStack() },
                onLoadPuzzle = vm::loadPuzzle,
                onSquareClicked = vm::onSquareClicked,
                onPromote = vm::onPromote,
            )
        }
        composable<DebugScreen.ForceAchievement>(
            enterTransition = { enter() },
            exitTransition = { exit() },
        ) {
            val vm: ForceAchievementViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            ForceAchievementScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onForceAchievement = vm::forceAchievement,
            )
        }
    }
}
