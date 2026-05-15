package com.paulcraciunas.chessgym.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.global.resources.R

enum class BottomNavItem(val screen: Screen) {
    Home(Screen.Home),
    PuzzleDashboard(Screen.PuzzleDashboard),
    BoardVisualization(Screen.BoardVisualization),
    BlindMode(Screen.BlindMode),
    ToolsDashboard(Screen.ToolsDashboard)
}

@Composable
fun BottomNavItem.iconPainter() = when (this) {
    BottomNavItem.Home -> rememberVectorPainter(Icons.Default.Home)
    BottomNavItem.PuzzleDashboard -> painterResource(R.drawable.puzzle_icon)
    BottomNavItem.BoardVisualization -> painterResource(R.drawable.board_visualization_icon)
    BottomNavItem.BlindMode -> painterResource(R.drawable.blind_mode_icon)
    BottomNavItem.ToolsDashboard -> painterResource(R.drawable.tools_icon)
}

@Composable
fun BottomNavItem.label() = when (this) {
    BottomNavItem.Home -> stringResource(R.string.navigation_home_label)
    BottomNavItem.PuzzleDashboard -> stringResource(R.string.navigation_puzzle_dashboard_label)
    BottomNavItem.BoardVisualization -> stringResource(R.string.navigation_board_viz_label)
    BottomNavItem.BlindMode -> stringResource(R.string.navigation_blind_mode_label)
    BottomNavItem.ToolsDashboard -> stringResource(R.string.navigation_tools_label)
}

@Composable
fun BottomNavItem.contentDescription() = when (this) {
    BottomNavItem.Home -> stringResource(R.string.navigation_home_screen_description)
    BottomNavItem.PuzzleDashboard -> stringResource(R.string.navigation_puzzle_dashboard_content_description)
    BottomNavItem.BoardVisualization -> stringResource(R.string.navigation_board_viz_content_description)
    BottomNavItem.BlindMode -> stringResource(R.string.navigation_blind_mode_content_description)
    BottomNavItem.ToolsDashboard -> stringResource(R.string.navigation_tools_content_description)
}
