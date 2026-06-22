package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.actions.AnalysisScreenActions
import com.paulcraciunas.chessgym.dsl.actions.AppActions
import com.paulcraciunas.chessgym.dsl.actions.BoardVisDashboardActions
import com.paulcraciunas.chessgym.dsl.actions.ClockActions
import com.paulcraciunas.chessgym.dsl.actions.ClockScreenActions
import com.paulcraciunas.chessgym.dsl.actions.FailedPuzzlesScreenActions
import com.paulcraciunas.chessgym.dsl.actions.FindTheSquareScreenActions
import com.paulcraciunas.chessgym.dsl.actions.HomeScreenActions
import com.paulcraciunas.chessgym.dsl.actions.NavigationActions
import com.paulcraciunas.chessgym.dsl.actions.PuzzleDashboardActions
import com.paulcraciunas.chessgym.dsl.actions.PuzzleStreakScreenActions
import com.paulcraciunas.chessgym.dsl.actions.RatedPuzzleScreenActions
import com.paulcraciunas.chessgym.dsl.actions.SignInScreenActions
import com.paulcraciunas.chessgym.dsl.actions.ToolsDashboardActions

class When(rule: ComposeTestRule) {
    val app: AppActions = AppActions(rule)
    val clock: ClockActions = ClockActions(rule)
    val navigation: NavigationActions = NavigationActions(rule)
    val homeScreen: HomeScreenActions = HomeScreenActions(rule)
    val puzzleDashboard: PuzzleDashboardActions = PuzzleDashboardActions(rule)
    val ratedPuzzle: RatedPuzzleScreenActions = RatedPuzzleScreenActions(rule)
    val failedPuzzles: FailedPuzzlesScreenActions = FailedPuzzlesScreenActions(rule)
    val puzzleStreak: PuzzleStreakScreenActions = PuzzleStreakScreenActions(rule)
    val boardVisDashboard: BoardVisDashboardActions = BoardVisDashboardActions(rule)
    val findTheSquare: FindTheSquareScreenActions = FindTheSquareScreenActions(rule)
    val toolsDashboard: ToolsDashboardActions = ToolsDashboardActions(rule)
    val clockScreen: ClockScreenActions = ClockScreenActions(rule)
    val analysisScreen: AnalysisScreenActions = AnalysisScreenActions(rule)
    val signInScreen: SignInScreenActions = SignInScreenActions(rule)
}
