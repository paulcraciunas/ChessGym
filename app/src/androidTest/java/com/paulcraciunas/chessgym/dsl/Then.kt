package com.paulcraciunas.chessgym.dsl

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.paulcraciunas.chessgym.dsl.assertions.AnalysisScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.BlindModeScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.BoardVisDashboardAssertions
import com.paulcraciunas.chessgym.dsl.assertions.ClockScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.FailedPuzzlesScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.FindTheSquareScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.HomeScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.ImportGameScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.KnightPathScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.NavigationAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleDashboardAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleRushScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.PuzzleStreakScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.RatedPuzzleScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.SignInScreenAssertions
import com.paulcraciunas.chessgym.dsl.assertions.ThemeAssertions
import com.paulcraciunas.chessgym.dsl.assertions.ToolsDashboardAssertions

object Then {
    lateinit var compose: ComposeTestRule
    lateinit var theme: ThemeAssertions
    lateinit var navigation: NavigationAssertions
    lateinit var homeScreen: HomeScreenAssertions
    lateinit var puzzleDashboard: PuzzleDashboardAssertions
    lateinit var ratedPuzzle: RatedPuzzleScreenAssertions
    lateinit var puzzleRush: PuzzleRushScreenAssertions
    lateinit var puzzleStreak: PuzzleStreakScreenAssertions
    lateinit var failedPuzzles: FailedPuzzlesScreenAssertions
    lateinit var boardVisDashboard: BoardVisDashboardAssertions
    lateinit var blindMode: BlindModeScreenAssertions
    lateinit var findTheSquare: FindTheSquareScreenAssertions
    lateinit var knightPath: KnightPathScreenAssertions
    lateinit var toolsDashboard: ToolsDashboardAssertions
    lateinit var clockScreen: ClockScreenAssertions
    lateinit var importGame: ImportGameScreenAssertions
    lateinit var analysisScreen: AnalysisScreenAssertions
    lateinit var signInScreen: SignInScreenAssertions

    fun init(rule: ComposeTestRule) {
        compose = rule
        theme = ThemeAssertions(rule)
        navigation = NavigationAssertions(rule)
        homeScreen = HomeScreenAssertions(rule)
        puzzleDashboard = PuzzleDashboardAssertions(rule)
        ratedPuzzle = RatedPuzzleScreenAssertions(rule)
        puzzleRush = PuzzleRushScreenAssertions(rule)
        puzzleStreak = PuzzleStreakScreenAssertions(rule)
        failedPuzzles = FailedPuzzlesScreenAssertions(rule)
        boardVisDashboard = BoardVisDashboardAssertions(rule)
        blindMode = BlindModeScreenAssertions(rule)
        findTheSquare = FindTheSquareScreenAssertions(rule)
        knightPath = KnightPathScreenAssertions(rule)
        toolsDashboard = ToolsDashboardAssertions(rule)
        clockScreen = ClockScreenAssertions(rule)
        importGame = ImportGameScreenAssertions(rule)
        analysisScreen = AnalysisScreenAssertions(rule)
        signInScreen = SignInScreenAssertions(rule)
    }
}
