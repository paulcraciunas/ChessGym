package com.paulcraciunas.chessgym.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.navigation.Screen
import com.paulcraciunas.screens.achievements.ui.AchievementsScreen
import com.paulcraciunas.screens.achievements.vm.AchievementsViewModel
import com.paulcraciunas.screens.home.ui.HomeScreen
import com.paulcraciunas.screens.home.vm.HomeViewModel

@Composable
internal fun Home(
    tabNavController: NavHostController,
    onDrawerToggle: () -> Unit,
) {
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

@Composable
internal fun Achievements(tabNavController: NavHostController) {
    val vm: AchievementsViewModel = hiltViewModel()
    val achievementsState by vm.uiState.collectAsStateWithLifecycle()
    AchievementsScreen(
        state = achievementsState,
        interactions = vm,
        onBack = { tabNavController.popBackStack() },
    )
}
