package com.paulcraciunas.chessgym.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.chessgym.R

@Composable
fun BottomNavigationBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = item.iconPainter(),
                        contentDescription = item.contentDescription()
                    )
                },
                label = {
                    Text(text = item.label())
                },
                selected = selectedItem == item,
                onClick = { onItemSelected(item) }
            )
        }
    }
}

@Composable
private fun BottomNavItem.iconPainter() = when (this) {
    BottomNavItem.Home -> rememberVectorPainter(Icons.Default.Home)
    BottomNavItem.RatedPuzzle -> painterResource(R.drawable.puzzle_icon)
    BottomNavItem.PuzzleRush -> painterResource(R.drawable.puzzle_rush_icon)
    BottomNavItem.BoardVisualization -> painterResource(R.drawable.board_viz_icon)
    BottomNavItem.BlindMode -> painterResource(R.drawable.blind_mode_icon)
}

@Composable
private fun BottomNavItem.label() = when (this) {
    BottomNavItem.Home -> stringResource(R.string.navigation_home_label)
    BottomNavItem.RatedPuzzle -> stringResource(R.string.navigation_rated_puzzle_label)
    BottomNavItem.PuzzleRush -> stringResource(R.string.navigation_puzzle_rush_label)
    BottomNavItem.BoardVisualization -> stringResource(R.string.navigation_board_viz_label)
    BottomNavItem.BlindMode -> stringResource(R.string.navigation_blind_mode_label)
}

@Composable
private fun BottomNavItem.contentDescription() = when (this) {
    BottomNavItem.Home -> stringResource(R.string.navigation_home_screen_description)
    BottomNavItem.RatedPuzzle -> stringResource(R.string.navigation_rated_puzzle_content_description)
    BottomNavItem.PuzzleRush -> stringResource(R.string.navigation_puzzle_rush_content_description)
    BottomNavItem.BoardVisualization -> stringResource(R.string.navigation_board_viz_content_description)
    BottomNavItem.BlindMode -> stringResource(R.string.navigation_blind_mode_content_description)
}
