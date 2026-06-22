package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.assertions.AnalysisScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.BlindModeScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.BoardVisDashboardAssertions
import com.paulcraciunas.chessgym.dsl.assertions.ClockScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.FailedPuzzlesScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.FindTheSquareScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.HomeScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.KnightPathScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.NavigationAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleDashboardAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleRushScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleStreakScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.RatedPuzzleScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.SignInScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.ThemeAssertions
import com.paulcraciunas.chessgym.dsl.assertions.ToolsDashboardAssertions

class Then(rule: ComposeTestRule) {
    val theme: ThemeAssertions = ThemeAssertions(rule)
    val navigation: NavigationAssertions = NavigationAssertions(rule)
    val homeScreen: HomeScreenAssertions = HomeScreenAssertions(rule)
    val puzzleDashboard: PuzzleDashboardAssertions = PuzzleDashboardAssertions(rule)
    val ratedPuzzle: RatedPuzzleScreenAssertions = RatedPuzzleScreenAssertions(rule)
    val puzzleRush: PuzzleRushScreenAssertions = PuzzleRushScreenAssertions(rule)
    val puzzleStreak: PuzzleStreakScreenAssertions = PuzzleStreakScreenAssertions(rule)
    val failedPuzzles: FailedPuzzlesScreenAssertions = FailedPuzzlesScreenAssertions(rule)
    val boardVisDashboard: BoardVisDashboardAssertions = BoardVisDashboardAssertions(rule)
    val blindMode: BlindModeScreenAssertions = BlindModeScreenAssertions(rule)
    val findTheSquare: FindTheSquareScreenAssertions = FindTheSquareScreenAssertions(rule)
    val knightPath: KnightPathScreenAssertions = KnightPathScreenAssertions(rule)
    val toolsDashboard: ToolsDashboardAssertions = ToolsDashboardAssertions(rule)
    val clockScreen: ClockScreenAssertions = ClockScreenAssertions(rule)
    val analysisScreen: AnalysisScreenAssertions = AnalysisScreenAssertions(rule)
    val signInScreen: SignInScreenAssertions = SignInScreenAssertions(rule)
}
