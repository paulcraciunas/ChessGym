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

    @Serializable
    data object PuzzleRush : Screen()

    @Serializable
    data object FailedPuzzles : Screen()

    @Serializable
    data object PuzzleStreak : Screen()

    @Serializable
    data object BoardVisualization : Screen()

    @Serializable
    data object FindTheSquare : Screen()

    @Serializable
    data object MoveThePiece : Screen()

    @Serializable
    data object BlindMode : Screen()

    @Serializable
    data object ToolsDashboard : Screen()

    @Serializable
    data object Clock : Screen()

    @Serializable
    data class Analysis(val fen: String? = null) : Screen()

    @Serializable
    data object ImportGame : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object SignUp : Screen()

    @Serializable
    data object About : Screen()

    @Serializable
    data class AboutDetail(val section: String) : Screen()
}
