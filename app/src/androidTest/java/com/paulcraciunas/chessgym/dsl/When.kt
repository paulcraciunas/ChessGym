package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.actions.BoardVisDashboardActions
import com.paulcraciunas.chessgym.dsl.actions.FailedPuzzlesScreenActions
import com.paulcraciunas.chessgym.dsl.actions.FindTheSquareScreenActions
import com.paulcraciunas.chessgym.dsl.actions.HomeScreenActions
import com.paulcraciunas.chessgym.dsl.actions.NavigationActions
import com.paulcraciunas.chessgym.dsl.actions.PuzzleDashboardActions
import com.paulcraciunas.chessgym.dsl.actions.PuzzleStreakScreenActions
import com.paulcraciunas.chessgym.dsl.actions.RatedPuzzleScreenActions

object When {
    lateinit var compose: ComposeTestRule
    lateinit var activityLauncher: () -> Unit
    lateinit var navigation: NavigationActions
    lateinit var homeScreen: HomeScreenActions
    lateinit var puzzleDashboard: PuzzleDashboardActions
    lateinit var ratedPuzzle: RatedPuzzleScreenActions
    lateinit var failedPuzzles: FailedPuzzlesScreenActions
    lateinit var puzzleStreak: PuzzleStreakScreenActions
    lateinit var boardVisDashboard: BoardVisDashboardActions
    lateinit var findTheSquare: FindTheSquareScreenActions

    fun init(rule: ComposeTestRule, launcher: () -> Unit) {
        compose = rule
        activityLauncher = launcher
        navigation = NavigationActions(rule)
        homeScreen = HomeScreenActions(rule)
        puzzleDashboard = PuzzleDashboardActions(rule)
        ratedPuzzle = RatedPuzzleScreenActions(rule)
        failedPuzzles = FailedPuzzlesScreenActions(rule)
        puzzleStreak = PuzzleStreakScreenActions(rule)
        boardVisDashboard = BoardVisDashboardActions(rule)
        findTheSquare = FindTheSquareScreenActions(rule)
    }

    fun appIsLaunched(): When = apply {
        activityLauncher()
        compose.waitForIdle()
    }
}
