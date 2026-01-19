package com.paulcraciunas.chessgym.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Loading : Screen()

    @Serializable
    data object Main : Screen()

    @Serializable
    data object Home : Screen()

    @Serializable
    data object PuzzleDashboard : Screen()

    @Serializable
    data object RatedPuzzle : Screen()

// TODO Paul: Integrate this in the Puzzle Dashboard screen
//    @Serializable
//    data object PuzzleRush : Screen()
//
    @Serializable
    data object BoardVisualization : Screen()

    @Serializable
    data object BlindMode : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object SignUp : Screen()

    @Serializable
    data object About : Screen()
}
