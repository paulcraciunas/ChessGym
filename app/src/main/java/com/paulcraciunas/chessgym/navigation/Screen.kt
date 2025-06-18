package com.paulcraciunas.chessgym.navigation

sealed class Screen(val route: String) {
    data object Loading : Screen("loading")
    data object Home : Screen("home")
    data object RatedPuzzle : Screen("rated_puzzle")
    data object PuzzleRush : Screen("puzzle_rush")
    data object BoardVisualization : Screen("board_visualization")
    data object BlindMode : Screen("blind_mode")
}
