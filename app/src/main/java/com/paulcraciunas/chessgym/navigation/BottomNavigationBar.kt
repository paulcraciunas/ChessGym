package com.paulcraciunas.chessgym.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.paulcraciunas.global.resources.R

@Composable
internal fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = modifier
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = item.iconPainter(),
                        contentDescription = item.contentDescription(),
                        modifier = Modifier.height(24.dp)
                    )
                },
                label = {
                    Text(text = item.label())
                },
                selected = item.screen.isCurrent(currentDestination?.route),
                onClick = {
                    onItemSelected(item)
                },
            )
        }
    }
}

private fun Screen.isCurrent(route: String?): Boolean = when (this) {
    Screen.Home -> route?.contains("Home") == true
    Screen.PuzzleDashboard -> route?.contains("PuzzleDashboard") == true
    Screen.BoardVisualization -> route?.contains("BoardVisualization") == true
    Screen.BlindMode -> route?.contains("BlindMode") == true
    else -> false
}

@Composable
private fun BottomNavItem.iconPainter() = when (this) {
    BottomNavItem.Home -> rememberVectorPainter(Icons.Default.Home)
    BottomNavItem.PuzzleDashboard -> painterResource(R.drawable.puzzle_icon)
    BottomNavItem.BoardVisualization -> painterResource(R.drawable.board_viz_icon)
    BottomNavItem.BlindMode -> painterResource(R.drawable.blind_mode_icon)
}

@Composable
private fun BottomNavItem.label() = when (this) {
    BottomNavItem.Home -> stringResource(R.string.navigation_home_label)
    BottomNavItem.PuzzleDashboard -> stringResource(R.string.navigation_puzzle_dashboard_label)
    BottomNavItem.BoardVisualization -> stringResource(R.string.navigation_board_viz_label)
    BottomNavItem.BlindMode -> stringResource(R.string.navigation_blind_mode_label)
}

@Composable
private fun BottomNavItem.contentDescription() = when (this) {
    BottomNavItem.Home -> stringResource(R.string.navigation_home_screen_description)
    BottomNavItem.PuzzleDashboard -> stringResource(R.string.navigation_puzzle_dashboard_content_description)
    BottomNavItem.BoardVisualization -> stringResource(R.string.navigation_board_viz_content_description)
    BottomNavItem.BlindMode -> stringResource(R.string.navigation_blind_mode_content_description)
}
